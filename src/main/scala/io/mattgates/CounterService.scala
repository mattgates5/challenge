package io.mattgates

import akka.actor.Actor
import akka.event.Logging._
import spray.http.{HttpRequest, HttpResponse}
import spray.routing.HttpService
import spray.routing.directives.{DebuggingDirectives, LogEntry}

import scala.util.Try
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import ckite.CKiteBuilder
import ckite.rpc.FinagleThriftRpc

/**
  * Created by mgates on 10/8/16.
  * Spray HTTP service using CKite for clustering
  */
trait CounterService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  // Initialize key-value store
  var store = new KVStore()

  // Read bootstrap variable
  val bootstrap = Try(System.getenv("BOOTSTRAP").toBoolean).getOrElse(false)

  // Initialize and start a CKite cluster
  val cluster = CKiteBuilder().listenAddress("0.0.0.0:7778")
    .rpc(FinagleThriftRpc)
    .stateMachine(store)
    .bootstrap(bootstrap)
    .build

  cluster.start()

  // REST Routes
  val route =
    // Health check ping
    path("ping") {
      get {
        complete("OK\n")
      }
    } ~
    // POST /config
    path("config") {
      post {
        entity(as[ActorConfig]) { config =>
          complete {
            for(actor <- config.actors) {
              cluster.addMember(s"$actor:7778")
              Thread.sleep(1000)
            }
            s"Config for actors posted: ${ config.actors mkString ", " }\n"
          }
        }
      }
    } ~
    pathPrefix("counter" / Segment ) { counterName =>
      path("value") {
        // GET /counter/:name:/value
        get {
          complete {
            val result = store.applyRead(Get(counterName))
            val value = result match {
              case Some(v) => v
              case None => 0
            }
            s"$value\n"
          }
        }
      } ~
      path("consistent_value") {
        // GET /counter/:name:/consistent_value
        get {
          complete {
            val readFuture: Future[Option[Int]] = cluster.read(Get(counterName))
            val result = Await.result(readFuture, 10.seconds).getOrElse(0)
            s"$result\n"
          }
        }
      } ~
      // POST /counter/:name:
      post {
        entity(as[CounterValue]) { counterValue =>
          cluster.write(Put(counterName, counterValue.value))
          complete(s"Posted a value of ${counterValue.value} to counter $counterName\n")
        }
      }
    }
}

class CounterServiceActor extends Actor with CounterService {
  def actorRefFactory = context

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, InfoLevel))
    case _ => None
  }

  def routeWithLogging = DebuggingDirectives.logRequestResponse(requestMethodAndResponseStatusAsInfo _)(route)
  def receive = runRoute(routeWithLogging)
}

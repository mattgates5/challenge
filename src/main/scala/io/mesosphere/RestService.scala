package io.mesosphere

import akka.actor.{Actor,Props}
import akka.event.Logging._
import akka.pattern.ask
import akka.util.Timeout
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.routing.HttpService
import spray.routing.directives.{DebuggingDirectives,LogEntry}

import scala.concurrent.duration._

/**
  * Created by mgates on 10/8/16.
  */
trait RestService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(1.second)
  val counterActor = actorRefFactory.actorOf(Props[CounterActor])

  val route =
    path("ping") {
      get {
        complete("OK")
      }
    } ~
    path("config") {
      post {
        entity(as[ActorConfig]) { config =>
          complete(s"Actors ${config.actors mkString ", "} posted")
        }
      }
    } ~
    pathPrefix("counter" / Segment ) { counterName =>
      path("value") {
        get {
          complete {
            (counterActor ? Get(counterName)).mapTo[Int].map(value => s"$value")
          }
        }
      } ~
      path("consistent_value") {
        get {
          complete(s"Requested a consistent value for $counterName")
        }
      } ~
      post {
        entity(as[CounterValue]) { counterValue =>
          counterActor ! Set(counterName, counterValue)
          complete(s"Posted a value of ${counterValue.value} to counter $counterName")
        }
      }
    }
}

class RestServiceActor extends Actor with RestService {
  def actorRefFactory = context

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, InfoLevel))
    case _ => None
  }

  def routeWithLogging = DebuggingDirectives.logRequestResponse(requestMethodAndResponseStatusAsInfo _)(route)
  def receive = runRoute(routeWithLogging)
}

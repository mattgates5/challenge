package io.mattgates.counterservice

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import ckite.CKite
import io.mattgates.counterservice.util.ActorSystemProvider
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by mgates on 3/4/17.
  */
final case class ActorConfig(actors: List[String])
final case class CounterValue(value: Int)

trait JsonMarshaller extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val actorConfigFormat = jsonFormat1(ActorConfig)
  implicit val countValueFormat = jsonFormat1(CounterValue)
}

trait CounterServiceAPI extends ActorSystemProvider with JsonMarshaller {
  implicit val executionContext: ExecutionContext

  val store: KVStore
  val cluster: CKite

  val route =
    path("ping") {
      get { complete("OK\n") }
    } ~
    path("config") {
      post {
        entity(as[ActorConfig]) { actorConfig =>
          complete {
            actorConfig.actors.foreach { actor =>
              cluster.addMember(s"$actor:7778")
              Thread.sleep(1000)
            }
            s"Config for actors posted: ${actorConfig.actors.mkString(", ")}\n"
          }
        }
      }
    } ~
    pathPrefix("counter" / Segment) { counter =>
      path("value") {
        get {
          complete {
            val result = Option(store.applyRead(Get(counter)))
            s"${result.getOrElse(0)}\n"
          }
        }
      } ~
      path("consistent_value") {
        get {
          complete {
            val readFuture: Future[Option[Int]] = cluster.read(Get(counter))
            val result = Await.result(readFuture, 10.seconds)
            s"${result.getOrElse(0)}\n"
          }
        }
      } ~
      post {
        entity(as[CounterValue]) { counterValue =>
          cluster.write(Put(counter, counterValue.value))
          complete(s"Posted a value of ${counterValue.value} to counter $counter\n")
        }
      }
    }
}


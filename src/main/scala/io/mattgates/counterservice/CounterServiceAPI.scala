package io.mattgates.counterservice

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import ckite.CKite
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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
            val readFuture: Future[Option[Int]] = cluster.readLocal(Get(counter))
            val value = Await.result(readFuture, 10.seconds).getOrElse(0)
            s"$value\n"
          }
        }
      } ~
      path("consistent_value") {
        get {
          complete {
            val readFuture: Future[Option[Int]] = cluster.read(Get(counter))
            val value = Await.result(readFuture, 10.seconds).get
            s"$value\n"
          }
        }
      } ~
      post {
        entity(as[CounterValue]) { counterValue =>
          cluster.write(Put(counter, counterValue.value))
          complete(s"Posted a value of ${counterValue.value} to $counter\n")
        }
      }
    }
}


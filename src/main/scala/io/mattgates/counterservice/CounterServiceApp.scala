package io.mattgates.counterservice

import akka.actor.ActorSystem
import akka.event.Logging._
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}
import akka.stream.ActorMaterializer
import ckite.CKite
import io.mattgates.counterservice.config.ConfigComponent

import scala.concurrent.ExecutionContext

/**
  * @author Matt Gates
  */
object CounterServiceApp extends ActorSystemProvider
  with ConfigComponent with ClusterProviderComponent with CounterServiceAPI {

  implicit val system = ActorSystem(appConfig.name)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val store: KVStore = new KVStore
  val cluster: CKite = ClusterProvider(ckiteConfig, store)

  def main(args: Array[String]): Unit = {
    // Start CKite cluster
    cluster.start()

    // Start HTTP server
    Http().bindAndHandle(routeWithLogging, appConfig.address, appConfig.port)
  }

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(res) => Some(LogEntry(req.method + ":" + req.uri + ":" + res.status, InfoLevel))
    case _ => None
  }

  def routeWithLogging: server.Route = {
    DebuggingDirectives.logRequestResult(requestMethodAndResponseStatusAsInfo _)(route)
  }
}

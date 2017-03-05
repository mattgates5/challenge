package io.mattgates.counterservice

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.stream.ActorMaterializer
import ckite.CKite
import com.typesafe.config.{Config, ConfigFactory}
import io.mattgates.counterservice.config.{AppConfig, CKiteConfig}
import io.mattgates.counterservice.util.{ActorSystemProvider, ClusterProviderComponent}

import scala.concurrent.ExecutionContext

/**
  * @author Matt Gates
  */
object CounterServiceApp {

  def main(args: Array[String]): Unit = {

    val config: Config = ConfigFactory.load()

    val service = new CounterService(config)
  }
}

/**
  * CounterService object for running service cluster
  * @param config
  */
class CounterService(config: Config) extends ActorSystemProvider
  with ClusterProviderComponent
  with CounterServiceAPI {

  val appConfig: AppConfig = AppConfig(config.getConfig("application"))
  val ckiteConfig: CKiteConfig = CKiteConfig(config.getConfig("ckite"))

  implicit val actorSystem = ActorSystem(appConfig.name)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  val store: KVStore = new KVStore
  val cluster: CKite = ClusterProvider(ckiteConfig, store)

  val bindingFuture = Http().bindAndHandle(route, appConfig.address, appConfig.port)

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())
}



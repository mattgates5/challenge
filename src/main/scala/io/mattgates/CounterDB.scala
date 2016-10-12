package io.mattgates

/**
 * @author Matt Gates
 * Bootstrap the CounterDB Service
 */

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object CounterDB extends App {

  // Host actor system
  implicit val system = ActorSystem("counter-service")

  // Create the restApi service actor
  val counterApi = system.actorOf(Props[CounterServiceActor])

  // Start an HTTP server at 0.0.0.0:7777
  IO(Http) ! Http.Bind(counterApi, interface = "0.0.0.0", port = 7777)
}



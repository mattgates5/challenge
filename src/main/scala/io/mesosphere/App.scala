package io.mesosphere

/**
 * @author Matt Gates
 */

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.util.Timeout
import scala.concurrent.duration._

object CounterService extends App {

  // Host actor system
  implicit val system = ActorSystem("counter-service")

  // Default timeout
  implicit val timeout = Timeout(10.seconds)

  // Create the restApi service actor
  val restApi = system.actorOf(Props[RestServiceActor], "rest-service")

  // Create the counter actor
  val counterActor = system.actorOf(Props[CounterActor])

  // Start an HTTP server at 0.0.0.0:7777
  IO(Http) ! Http.Bind(restApi, interface = "0.0.0.0", port = 7777)
}



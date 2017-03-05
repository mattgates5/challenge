package io.mattgates.counterservice.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * Created by mgates on 3/4/17.
  */
trait ActorSystemProvider {
  implicit val actorSystem: ActorSystem
  implicit val materializer: ActorMaterializer
}

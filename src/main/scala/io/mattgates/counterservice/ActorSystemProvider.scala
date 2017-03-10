package io.mattgates.counterservice

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * Created by mgates on 3/4/17.
  */
trait ActorSystemProvider {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
}

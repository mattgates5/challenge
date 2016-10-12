package io.mattgates

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by mgates on 10/8/16.
  */
case class ActorConfig(actors: List[String])

object ActorConfig extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val actorConfigFormat = jsonFormat1(ActorConfig.apply)
}

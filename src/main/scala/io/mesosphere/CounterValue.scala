package io.mesosphere

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by mgates on 10/8/16.
  */
case class CounterValue(value: Int)

object CounterValue extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val counterValueFormat = jsonFormat1(CounterValue.apply)
}

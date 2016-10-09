package io.mesosphere

import akka.actor.Actor

/**
  * Created by mgates on 10/8/16.
  */
case class Get(counterName: String)
case class Set(counterName: String, value: CounterValue)

class CounterActor extends Actor {
  private val counters = scala.collection.mutable.Map[String, Int]()

  def receive = {
    case Get(counterName) => sender ! counters.getOrElse(counterName, 0)
    case Set(counterName, counterValue) => counters(counterName) = counterValue.value
  }
}

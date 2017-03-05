package io.mattgates.counterservice

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ckite.{CKite, CKiteBuilder}
import ckite.rpc.FinagleThriftRpc
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mgates on 3/4/17.
  */
class CounterServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with CounterServiceAPI {

  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val executionContext = system.dispatcher

  val store = new KVStore
  val cluster: CKite = CKiteBuilder().listenAddress("0.0.0.0:7778")
    .rpc(FinagleThriftRpc)
    .stateMachine(store)
    .bootstrap(true)
    .build

  val clients = ActorConfig(List("1.2.3.4", "1.2.3.5", "1.2.3.6"))
  val counter = "test"
  val number: Int = scala.util.Random.nextInt(100)

  "CounterService" should "return an OK for a health check" in {
    Get("ping") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual "OK\n"
    }
  }

  it should "return zero if a counter doesn't exist" in {
    Get(s"counter/$counter/value") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual "0\n"
    }
  }

  it should "succeed if a value is posted" in {
    Post("counter/$counter", CounterValue(number)) ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"Posted a value of $number to counter $counter\n"
    }
  }

  it should "return the local value of a posted counter" in {
    Get(s"counter/$counter/value") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"$number\n"
    }
  }

  it should "return a consistent value of a posted counter" in {
    Get(s"counter/$counter") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"$number\n"
    }
  }

  it should "behave responsibly with a bootstrap config" in {
    Post("config", clients) ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"Config for actors posted: ${clients.actors.mkString(", ")}\n"
    }
  }
}

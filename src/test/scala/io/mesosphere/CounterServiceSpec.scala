package io.mesosphere

import org.scalatest.FlatSpec
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes

/**
  * Created by mgates on 10/8/16.
  */
class CounterServiceSpec extends FlatSpec with ScalatestRouteTest with CounterService {
  def actorRefFactory = system

  import CounterValue._
  import ActorConfig._

  // Initialize some values for use in the tests
  val clients = new ActorConfig(List("1.2.3.4", "1.2.3.5", "1.2.3.6"))
  val number = scala.util.Random.nextInt(100)

  "The service" should "return a OK for the health check" in {
    Get("/ping") ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == "OK\n")
    }
  }

  it should "return zero if a counter doesn't exist" in {
    Get("/counter/test/value") ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == "0\n")
    }
  }

  it should "succeed if a value is posted" in {
    Post("/counter/test", CounterValue(number)) ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == s"Posted a value of $number to counter test\n")
      Thread.sleep(1000) // Wait for the value to propagate to the store :-(
    }
  }

  it should "return the local value of a posted counter" in {
    Get("/counter/test/value") ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == s"$number\n")
    }
  }

  it should "return a consistent value of a posted counter" in {
    Get("/counter/test/consistent_value") ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == s"$number\n")
    }
  }

  it should "behave responsibly with a bootstrap config" in {
    Post("/config", clients) ~> route ~> check {
      assert(status === StatusCodes.OK)
      val res = responseAs[String]
      assert(res == s"Config for actors posted: ${clients.actors mkString ", "}\n")
    }
  }
}

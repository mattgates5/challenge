package io.mesosphere

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

/**
  * Created by mgates on 10/8/16.
  */
class CounterServiceSpec extends Specification with Specs2RouteTest with CounterService {
  def actorRefFactory = system

  "The service" should {
    "return a health check for GET requests to /ping" in {
      Get("/ping") ~> route ~> check {
        status === Success
        responseAs[String] === "OK"
      }
    }

    "return zero if a counter doesn't exist" in {
      Get("/counter/test/value") ~> route ~> check {
        status === Success
        responseAs[String] === "0"
      }
    }

    "succeed if a counter is posted" in {
      Post("/counter/test", "{\"value\": 1}") ~> route ~> check {
        status === Success
      }
    }

    "return a value for an existing counter" in {
      Get("/counter/test/value") ~> route ~> check {
        status === Success
        responseAs[String] === "1"
      }
    }
  }
}

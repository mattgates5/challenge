package io.mattgates.counterservice

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import io.mattgates.counterservice.config.CKiteConfig
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mgates on 3/4/17.
  */
class CounterServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with ClusterProviderComponent with CounterServiceAPI {

  val store = new KVStore
  val cluster = ClusterProvider(CKiteConfig(ConfigFactory.load("integration").getConfig("ckite")), store)

  val clients = ActorConfig(List("1.2.3.4", "1.2.3.5", "1.2.3.6"))
  val counter = "test"
  val number: Int = scala.util.Random.nextInt(100)

  cluster.start()

  "CounterService" should "return an OK for a health check" in {
    Get("/ping") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual "OK\n"
    }
  }

  it should "return zero if a counter doesn't exist" in {
    Get(s"/counter/$counter/value") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual "0\n"
    }
  }

  it should "succeed if a value is posted" in {
    Post(s"/counter/$counter", CounterValue(number)) ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"Posted a value of $number to $counter\n"
      Thread.sleep(2000)
    }
  }

  it should "return the local value of a posted counter" in {
    Get(s"/counter/$counter/value") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"$number\n"
    }
  }

  it should "return a consistent value of a posted counter" in {
    Get(s"/counter/$counter/consistent_value") ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"$number\n"
    }
  }

  it should "behave responsibly with a bootstrap config" in {
    Post("/config", clients) ~> route ~> check {
      status shouldEqual OK
      responseAs[String] shouldEqual s"Config for actors posted: ${clients.actors.mkString(", ")}\n"
    }
  }
}

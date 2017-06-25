import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest

class CalculatorServiceTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest {
  "The parallel calculator service" should {

    "successfully calculate values expressions" in {
      Post(
        "/evaluate",
        HttpEntity(MediaTypes.`application/json`,
                   """{"expression":"(1-1)*2+3*(1-3+4)+10/2"}""")
      ) ~> Server.route ~> check {
        responseAs[String] shouldEqual """{"result":{"Right":{"value":11.0}}}"""
      }

      Post(
        "/evaluate",
        HttpEntity(MediaTypes.`application/json`,
                   """{"expression":"34*2+3+(1.27-3.45)*0+1000/2"}""")
      ) ~> Server.route ~> check {
        responseAs[String] shouldEqual """{"result":{"Right":{"value":571.0}}}"""
      }
    }

    "return an error on division by zero" in {
      Post(
        "/evaluate",
        HttpEntity(MediaTypes.`application/json`,
                   """{"expression":"1000/(203-203)"}""")
      ) ~> Server.route ~> check {
        responseAs[String] shouldEqual """{"result":{"Left":{"value":"Division by zero"}}}"""
      }
    }

    "return a parse error on invalid expressions" in {
      Post(
        "/evaluate",
        HttpEntity(MediaTypes.`application/json`,
                   """{"expression":"1000/(203-203)-"}""")
      ) ~> Server.route ~> check {
        responseAs[String] shouldEqual """{"result":{"Left":{"value":"Failed to parse at index 15"}}}"""
      }
    }
  }
}

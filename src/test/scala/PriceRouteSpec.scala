import CryptoService.system
import akka.actor.ActorRef
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.{ ErrorAccumulatingCirceSupport, FailFastCirceSupport }
import io.circe.generic.auto._
import model.BitcoinPrice
import org.scalatest.{ Matchers, WordSpec }

class PriceRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with PriceRoutes
    with ErrorAccumulatingCirceSupport {

  val coindeskActor: ActorRef = system.actorOf(CoindeskClientActor.props, "coindeskActor")

  "The service" should {
    "reject a GET requests to the root path" in {
      Get() ~> priceRoutes(coindeskActor) ~> check {
        handled shouldBe false
      }
    }

    "return a 'hello' response for GET requests to /prices" in {
      Get("/prices") ~> priceRoutes(coindeskActor) ~> check {
        responseAs[BitcoinPrice] shouldEqual BitcoinPrice("10.0")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> priceRoutes(coindeskActor) ~> check {
        handled shouldBe false
      }
    }
  }
}

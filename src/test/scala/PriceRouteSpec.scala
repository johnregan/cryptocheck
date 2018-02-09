import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import model._
import org.scalatest.{ Matchers, WordSpec }
import routes.PriceRoutes

import scala.concurrent.duration.DurationInt

class PriceRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with PriceRoutes
    with ErrorAccumulatingCirceSupport {

  implicit val sys: ActorSystem       = ActorSystem("CryptoActorSystem")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  "The service" should {
    "reject a GET requests to the root path" in {
      Get() ~> priceRoutes(sys, mat) ~> check {
        handled shouldBe false
      }
    }

    "return price from coindesk for Bitcoin in USD" in {
      Get("/prices") ~> priceRoutes(sys, mat) ~> check {
        responseAs[List[CryptoPriceResponse]] match {
          case List(CryptoPriceResponse(prices)) =>
            prices.size should be > 1
            prices.map(_.symbol) shouldEqual List("BTC", "ETH")
            prices.map(_.price).filter(_ > 0).size shouldEqual 2
        }
      }
    }

    "return supported cryptocurrency from file" in {
      Get("/supportedCoins") ~> supportedCoins ~> check {
        responseAs[SupportedCoins] match {
          case SupportedCoins(coins) =>
            coins shouldEqual List(CoinInfo("BTC", "Bitcoin"), CoinInfo("ETH", "Ethereum"))
        }
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> priceRoutes(sys, mat) ~> check {
        handled shouldBe false
      }
    }
  }
}

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import model.{ CoinInfo, CurrencyData, SupportedCoins }
import org.scalatest.{ Matchers, WordSpec }
import routes.PriceRoutes

import scala.concurrent.duration.DurationInt

class PriceRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with PriceRoutes
    with ErrorAccumulatingCirceSupport {

  val coindeskActor: ActorRef = system.actorOf(CoindeskClientActor.props, "coindeskActor")
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  "The service" should {
    "reject a GET requests to the root path" in {
      Get() ~> priceRoutes(coindeskActor) ~> check {
        handled shouldBe false
      }
    }

    "return price from coindesk for Bitcoin in USD" in {
      Get("/prices") ~> priceRoutes(coindeskActor) ~> check {
        responseAs[CurrencyData] match {
          case CurrencyData(currency, symbol, price, description, float) =>
            currency shouldEqual "USD"
            symbol shouldEqual "&#36;"
            price.replaceAll(",", "").toDouble should be > 0.0
            description shouldEqual "United States Dollar"
            float should be > 0.0
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
      Get("/kermit") ~> priceRoutes(coindeskActor) ~> check {
        handled shouldBe false
      }
    }
  }
}

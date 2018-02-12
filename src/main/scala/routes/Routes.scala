package routes

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import client._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import singleton.SupportedCoinsConfig

trait PriceRoutes extends Directives with ErrorAccumulatingCirceSupport with PredefinedTimeout {

  def priceRoutes(system: ActorSystem, materializer: ActorMaterializer): Route =
    pathPrefix("prices") {
      get {
        val batchingActor =
          system.actorOf(RequestBatchingActor.props(materializer), "responseAggregatorActor" + UUID.randomUUID())

        onSuccess(batchingActor ? PricesRequest) {
          case ErrorEncountered(errorMsg) => complete(errorMsg)
          case AggregatedPrices(prices)   => complete(prices)
        }
      }
    }

  def supportedCoins: Route =
    pathPrefix("supportedCoins") {
      get {
        complete(SupportedCoinsConfig.supportedCoinsConfig)
      }
    }
}

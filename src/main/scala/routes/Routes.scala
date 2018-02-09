package routes

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import client._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import model.SupportedCoins

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

trait PriceRoutes extends Directives with ErrorAccumulatingCirceSupport with PredefinedTimeout {

  def priceRoutes(system: ActorSystem, materializer: ActorMaterializer): Route =
    pathPrefix("prices") {
      get {
        val aggregator =
          system.actorOf(ResponseAggregatorActor.props(materializer), "responseAggregatorActor" + UUID.randomUUID())

        onSuccess(aggregator ? PricesRequest) {
          case ErrorEncountered(errorMsg) => complete(errorMsg)
          case PricesAggregated(prices)   => complete(prices)
        }
      }
    }

  def supportedCoins: Route =
    pathPrefix("supportedCoins") {
      get {
        onSuccess(Future {
          SupportedCoins(Source.fromResource("supportedCoins.json").mkString)
        }) {
          case Right(supportedCoins) => complete(supportedCoins)
          case Left(error)           => complete(StatusCodes.BadRequest, error.toString)
        }
      }
    }
}

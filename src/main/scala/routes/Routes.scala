package routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import model.{ CurrencyData, RequestPrice, SupportedCoins }

import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

trait PriceRoutes extends Directives with ErrorAccumulatingCirceSupport with PredefinedTimeout {

  def priceRoutes(coindeskClientActor: ActorRef): Route =
    pathPrefix("prices") {
      get {
        onSuccess(coindeskClientActor ? RequestPrice) {
          case Right(cd: CurrencyData) => complete(cd)
          case Left(error) => complete(StatusCodes.BadRequest, error.toString)
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

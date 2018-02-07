import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.ask
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Decoder.Result
import model.{ BitcoinPrice, CurrencyData }
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

trait PriceRoutes extends Directives with ErrorAccumulatingCirceSupport with PredefinedTimeout {

  def priceRoutes(coindeskClientActor: ActorRef): Route =
    pathPrefix("prices") {
      get {
        onSuccess(coindeskClientActor ? RequestPrice) {
          case Right(cd: CurrencyData) => complete(cd)
          case Left(error) => complete(StatusCodes.BadRequest, (error.toString))
        }
      }
    }
}

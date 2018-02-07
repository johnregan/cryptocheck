import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.util.ByteString
import model.{ BitcoinPrice, CurrencyData }
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future

case object RequestPrice

class CoindeskClientActor extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive = {
    case CoindeskResponse(actorRef, HttpResponse(StatusCodes.OK, headers, entity, _)) =>
      entity.dataBytes
        .runFold(ByteString.empty) {
          case (acc, b) => acc ++ b
        }
        .map(s => s.utf8String.stripMargin.replaceAll("rate_float", "decimal"))
        .map(j => CurrencyData(j))
        .pipeTo(actorRef)

    case CoindeskResponse(actorRef, resp @ HttpResponse(code, _, _, _)) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
    case RequestPrice =>
      val msgSender = sender()
      log.info(s"performing request $msgSender")
      http
        .singleRequest(HttpRequest(uri = "https://api.coindesk.com/v1/bpi/currentprice.json"))
        .map(CoindeskResponse(msgSender, _))
        .pipeTo(self)
  }
}

case class CoindeskResponse(actorId: ActorRef, httpResponse: HttpResponse)

object CoindeskClientActor {
  def props: Props = Props[CoindeskClientActor]
}

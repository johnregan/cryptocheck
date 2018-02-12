package client

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.stream.ActorMaterializer
import akka.util.ByteString
import model.ParseUtil

case class PricesRequestMsg(currencies:                 List[String])
case class CryptoCompareHttpActorResponse(httpResponse: HttpResponse, supportedCurrencies: List[String])

class CryptoCompareActor(aggregator: ActorRef, implicit val materializer: ActorMaterializer)
    extends Actor
    with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  val http = Http(context.system)

  override def receive: Receive = {
    case PricesRequestMsg(currencies) =>
      val uri = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + currencies.mkString(",") + "&tsyms=USD"
      log.info(s"performing request to CryptoCompare $uri")

      http
        .singleRequest(HttpRequest(uri = uri))
        .map(r => CryptoCompareHttpActorResponse(r, currencies))
        .pipeTo(self)
    case CryptoCompareHttpActorResponse(HttpResponse(StatusCodes.OK, _, entity, _), supportedCurrencies) =>
      log.info("Response received from Cryptocompare")

      entity.dataBytes
        .runFold(ByteString.empty) {
          case (acc, b) => acc ++ b
        }
        .map(s => s.utf8String)
        .map(j => ParseUtil.parseToCurrency(j, supportedCurrencies))
        .pipeTo(aggregator)
    case CryptoCompareHttpActorResponse(resp @ HttpResponse(code, _, _, _), Nil) =>
      log.info("Cryptocompare Request failed, response code: " + code)

      resp.discardEntityBytes()

      aggregator ! ErrorEncountered("Cryptocompare Request failed, response code: " + code)
  }
}

object CryptoCompareActor {

  def props(aggregator: ActorRef, materializer: ActorMaterializer): Props =
    Props(classOf[CryptoCompareActor], aggregator, materializer)

}

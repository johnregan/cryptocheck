package client

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.stream.ActorMaterializer
import akka.util.ByteString
import model.ParseUtil

case class PricesRequestMsg(currencies: List[String])

case class CryptoCompareHttpActorResponse(actorId:             ActorRef,
                                          httpResponse:        HttpResponse,
                                          supportedCurrencies: List[String])

class CryptoCompareActor(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  val http = Http(context.system)

  override def receive: Receive = {
    case PricesRequestMsg(currencies) =>
      val s = sender()

      val uri = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + currencies.mkString(",") + "&tsyms=USD"
      log.info(s"performing request to CryptoCompare $uri")

      http
        .singleRequest(HttpRequest(uri = uri))
        .map(r => CryptoCompareHttpActorResponse(s, r, currencies))
        .pipeTo(self)
    case CryptoCompareHttpActorResponse(s, HttpResponse(StatusCodes.OK, _, entity, _), supportedCurrencies) =>
      log.info("Response received from Cryptocompare")

      entity.dataBytes
        .runFold(ByteString.empty) {
          case (acc, b) => acc ++ b
        }
        .map(s => s.utf8String)
        .map(j => ParseUtil.parseToCurrency(j, supportedCurrencies))
        .pipeTo(s)
    case CryptoCompareHttpActorResponse(s, resp @ HttpResponse(code, _, _, _), Nil) =>
      log.info("Cryptocompare Request failed, response code: " + code)

      resp.discardEntityBytes()

      s ! ErrorEncountered("Cryptocompare Request failed, response code: " + code)
  }
}

object CryptoCompareActor {
  def props(materializer: ActorMaterializer): Props = Props(classOf[CryptoCompareActor], materializer)

}

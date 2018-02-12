package client.cryptocompare

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.stream.ActorMaterializer
import model.CryptoPrice
import singleton.SupportedCoinsConfig

case class AggregatedPrices(prices: List[CryptoPrice])

class RequestBatchingActor(implicit val materializer: ActorMaterializer) extends Actor with ActorLogging {

  private var httpActor: ActorRef = null
  private final val BatchSize = 60

  override def receive: Receive = {
    case PricesRequest =>
      httpActor = sender()
      log.info(s"Aggregation request for multiple requests to cryptocompare")

      val aggregator =
        context.system.actorOf(ResponseAggregatorActor.props(self), "responseAggregatorActor" + UUID.randomUUID())
      val cryptoCompare =
        context.system
          .actorOf(CryptoCompareActor.props(aggregator, materializer), "cryptocompareActor" + UUID.randomUUID())

      val batches = SupportedCoinsConfig.supportedCoinsConfig.coins.map(_.Symbol).grouped(BatchSize)
      batches.foreach {
        cryptoCompare ! PricesRequestMsg(_)
      }

    case pricesMsg @ AggregatedPrices(_) =>
      log.info(s"Prices retrieved from aggregator, send to http actor")

      httpActor ! pricesMsg

    case error @ ErrorEncountered(_) =>
      httpActor ! error
  }
}

object RequestBatchingActor {
  def props(materializer: ActorMaterializer): Props = Props(classOf[RequestBatchingActor], materializer)

}

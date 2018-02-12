package client

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import model.{ CryptoPrice, CryptoPriceResponse }
import singleton.SupportedCoinsConfig

case object PricesRequest
case class ErrorEncountered(msg: String)

class ResponseAggregatorActor(batchingActor: ActorRef) extends Actor with ActorLogging {

  val responses: scala.collection.mutable.ListBuffer[CryptoPriceResponse] = scala.collection.mutable.ListBuffer()
  val config = SupportedCoinsConfig.supportedCoinsConfig
  var numberOfCoins: Int = config.coins.size

  override def receive: Receive = {
    case response: CryptoPriceResponse =>
      responses += response
      val numberStored = responses.flatMap(_.prices).size

      log.info(s"Stored ${numberStored}/${numberOfCoins} responses for aggregation")

      val supported: Set[String] = SupportedCoinsConfig.supportedCoinsConfig.coins.map(_.Symbol).toSet
      val stored:    Set[String] = responses.map(_.prices).flatMap(_.map(_.symbol)).toSet

      log.info(s"Remaining to store: ${supported.diff(stored)}")

      if (numberStored == numberOfCoins) {
        log.info(s"Results aggregated, now respond to request batching actor")

        batchingActor ! AggregatedPrices(responses.flatMap(_.prices).toList)
      }
    case ErrorEncountered(errorMsg) =>
      batchingActor ! ErrorEncountered(errorMsg)
  }
}

object ResponseAggregatorActor {
  def props(batchingActor: ActorRef): Props = Props(classOf[ResponseAggregatorActor], batchingActor)

}

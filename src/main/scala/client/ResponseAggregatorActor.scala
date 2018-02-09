package client

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.stream.ActorMaterializer
import model.{ CryptoPrice, CryptoPriceResponse, SupportedCoins }

import scala.io.Source

case object PricesRequest
case object AggregationFinished
case class PricesAggregated(prices: List[CryptoPrice])
case class ErrorEncountered(msg:    String)

class ResponseAggregatorActor(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  val responses:     scala.collection.mutable.ListBuffer[CryptoPriceResponse] = scala.collection.mutable.ListBuffer()
  var httpSender:    ActorRef                                                 = null
  var numberOfCoins: Int                                                      = 0

  override def receive: Receive = {
    case PricesRequest =>
      httpSender = sender()
      log.info(s"Aggregation request for multiple requests to cryptocompare")

      val cryptoCompare =
        context.system.actorOf(CryptoCompareActor.props(materializer), "cryptocompareActor" + UUID.randomUUID())

      SupportedCoins(Source.fromResource("supportedCoins.json").mkString) match {
        case Left(error) => httpSender ! ErrorEncountered("unable to retrieve supported coins")
        case Right(sc) =>
          numberOfCoins = sc.coins.size
          log.info(s"Number of coin prices to retrieve $numberOfCoins")

          val batches = sc.coins.map(_.Symbol).grouped(50)
          batches.foreach {
            cryptoCompare ! PricesRequestMsg(_)
          }
      }
    case response: CryptoPriceResponse =>
      responses += response
      val numberStored = responses.flatMap(_.prices).size

      log.info(s"Stored ${numberStored} responses for aggregation")

      if (numberStored == numberOfCoins)
        self ! AggregationFinished

    case AggregationFinished =>
      log.info(s"Aggregate results and respond to http")

      httpSender ! PricesAggregated(responses.flatMap(_.prices).toList)

    case ErrorEncountered(errorMsg) =>
      httpSender ! ErrorEncountered(errorMsg)
  }
}

object ResponseAggregatorActor {
  def props(materializer: ActorMaterializer): Props = Props(classOf[ResponseAggregatorActor], materializer)

}

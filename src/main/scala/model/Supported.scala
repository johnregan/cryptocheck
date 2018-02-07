package model

import io.circe.parser.parse
import io.circe.generic.auto._

case class SupportedCoins(coins: List[CoinInfo])

case class CoinInfo(Symbol: String, CoinName: String)

object SupportedCoins {

  def apply(rawJson: String): Either[ParsingError, SupportedCoins] =
    parse(rawJson) match {
      case Left(error) => Left(ParsingError(error.message, error.underlying))
      case Right(json) =>
        json.as[SupportedCoins] match {
          case Left(error)           => Left(ParsingError(error.message, error.history))
          case Right(supportedCoins) => Right(supportedCoins)
        }
    }
}

package model

import io.circe.generic.auto._
import io.circe.parser.parse

case class CryptoPrice(symbol: String, price: Double)

case class CryptoPriceResponse(prices: List[CryptoPrice])

case class Price(USD: Double)

object ParseUtil {

  def parseToCurrency(rawJson: String, supported: List[String]): CryptoPriceResponse =
    parse(rawJson) match {
      case Left(_) => CryptoPriceResponse(Nil)
      case Right(json) =>
        CryptoPriceResponse(supported.flatMap { s =>
          json.hcursor.downField(s).as[Price] match {
            case Left(_)      => None
            case Right(price) => Option(CryptoPrice(s, price.USD))
          }
        })
    }
}

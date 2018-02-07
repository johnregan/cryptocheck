package model

import io.circe.Decoder.Result
import io.circe.{ DecodingFailure, Error, Json, ParsingFailure }
import io.circe.parser.parse

case class Time(updated: String, updatedISO: String, updateUk: String)

case class CurrencyData(code: String, symbol: String, rate: String, description: String, decimal: Double)

object CurrencyData {

  import io.circe.generic.auto._

  //  for {
  //    parsed <- parse(rawJson)
  //    usd    <- parsed.findAllByKey("USD")
  //    cd     <- usd.as[CurrencyData]
  //  } yield {
  //    cd
  //  }
  def apply(rawJson: String): Either[Error, CurrencyData] =
    parse(rawJson) match {
      case Left(error) => Left(error.asInstanceOf[Error])
      case Right(json) =>
        json.findAllByKey("USD") match {
          case head :: Nil =>
            head.as[CurrencyData] match {
              case Left(error) => Left(error.asInstanceOf[Error])
              case Right(cd)   => Right(cd)
            }
          case Nil => Left(ParsingFailure("no element found", null).asInstanceOf[Error])
          case _   => Left(ParsingFailure("more than one element found", null).asInstanceOf[Error])
        }
    }
}

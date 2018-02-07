package model

import io.circe.generic.auto._
import io.circe.parser.parse

case object RequestPrice

case class Time(updated: String, updatedISO: String, updateUk: String)

case class CurrencyData(code: String, symbol: String, rate: String, description: String, decimal: Double)

object CurrencyData {

  def apply(rawJson: String): Either[ParsingError, CurrencyData] =
    parse(rawJson) match {
      case Left(error) => Left(ParsingError(error.message, error.underlying))
      case Right(json) =>
        json.findAllByKey("USD") match {
          case head :: Nil =>
            head.as[CurrencyData] match {
              case Left(error) => Left(ParsingError(error.message, error.history))
              case Right(cd)   => Right(cd)
            }
          case Nil => Left(ParsingError("no element found"))
          case _   => Left(ParsingError("more than one element found"))
        }
    }
}

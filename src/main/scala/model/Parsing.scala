package model

import io.circe.CursorOp

case class ParsingError(error: String, history: List[String])

object ParsingError {
  def apply(error: String, history: => List[CursorOp]): ParsingError = new ParsingError(error, history.map(_.toString))

  def apply(error: String, throwable: Throwable): ParsingError = new ParsingError(error, List(throwable.getMessage))
  def apply(error: String): ParsingError = new ParsingError(error, Nil)

}

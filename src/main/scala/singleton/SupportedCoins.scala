package singleton

import model.SupportedCoins

import scala.io.Source

object SupportedCoinsConfig {

  val supportedCoinsConfig = SupportedCoins(Source.fromResource("supportedCoins.json").mkString) match {
    case Left(error)  => throw new RuntimeException("Error when parsing supported coins: " + error)
    case Right(coins) => coins
  }

}

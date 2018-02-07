import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.auto._
import io.circe.parser.{parse, _}
import io.circe.syntax._

val response =
  """
    |{"time":{"updated":"Feb 5, 2018 16:15:00 UTC","updatedISO":"2018-02-05T16:15:00+00:00","updateduk":"Feb 5, 2018 at 16:15 GMT"},"disclaimer":"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org","chartName":"Bitcoin","bpi":{"USD":{"code":"USD","symbol":"&#36;","rate":"7,402.9738","description":"United States Dollar","rate_float":7402.9738},"GBP":{"code":"GBP","symbol":"&pound;","rate":"5,281.6738","description":"British Pound Sterling","rate_float":5281.6738},"EUR":{"code":"EUR","symbol":"&euro;","rate":"5,960.4673","description":"Euro","rate_float":5960.4673}}}
  """.stripMargin.replaceAll("rate_float","decimal")

case class CurrencyData(code:     String, symbol: String, rate: String, description: String, decimal: Double)

val parsed =
  parse(response)
  .map(_.findAllByKey("USD"))
      .map(_.headOption)
        .map(_.map(_.as[CurrencyData]))
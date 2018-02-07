
import com.typesafe.config.ConfigFactory

trait AppConfig {
  private val config     = ConfigFactory.load()
  lazy val httpConfig    = config.getConfig("http")
  lazy val httpPort      = httpConfig.getInt("port")
  lazy val httpInterface = httpConfig.getString("interface")

}

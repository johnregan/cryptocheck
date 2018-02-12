import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import model.SupportedCoins
import routes.BaseService

import scala.concurrent.ExecutionContextExecutor
import scala.io.Source

object CryptoService extends App with BaseService with AppConfig {
  implicit val system:       ActorSystem              = ActorSystem("CryptoActorSystem")
  implicit val executor:     ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer        = ActorMaterializer()

  Http().bindAndHandle(routes(system, materializer), httpInterface, httpPort)
}

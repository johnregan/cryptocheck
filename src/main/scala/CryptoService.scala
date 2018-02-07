import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

object CryptoService extends App with BaseService with AppConfig {
  implicit val system:       ActorSystem              = ActorSystem("CryptoActorSystem")
  implicit val executor:     ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer        = ActorMaterializer()

  val coindeskActor: ActorRef = system.actorOf(CoindeskClientActor.props, "coindeskActor")
  Http().bindAndHandle(routes(coindeskActor), httpInterface, httpPort)
}

package routes

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

trait BaseService extends PriceRoutes {

  def routes(coindeskActor: ActorSystem, materializer: ActorMaterializer): Route =
    priceRoutes(coindeskActor, materializer) ~ supportedCoins
}

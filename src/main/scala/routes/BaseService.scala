package routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route

trait BaseService extends PriceRoutes {

  def routes(coindeskActor: ActorRef): Route = priceRoutes(coindeskActor) ~ supportedCoins
}

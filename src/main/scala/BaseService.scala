import akka.actor.ActorRef
import akka.http.scaladsl.server.Route

trait BaseService extends PriceRoutes {

  def routes: ActorRef => Route = priceRoutes
}

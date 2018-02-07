package hello

import akka.actor.{ Actor, ActorLogging, Props }
import hello.Printer.Greeting

class Printer extends Actor with ActorLogging {

  def receive = {
    case Greeting(greeting) =>
      log.info(s"Greeting received (from ${sender()}): $greeting")
  }
}

object Printer {
  def props: Props = Props[Printer]

  final case class Greeting(greeting: String)
}

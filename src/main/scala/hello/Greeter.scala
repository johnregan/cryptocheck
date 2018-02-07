package hello

import akka.actor.{ Actor, ActorRef, Props }
import hello.Greeter.{ Greet, WhoToGreet }
import hello.Printer.Greeting

class Greeter(message: String, printerActor: ActorRef) extends Actor {

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = s"$message, $who"
    case Greet =>
      printerActor ! Greeting(greeting)
  }
}

object Greeter {
  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))

  final case class WhoToGreet(who: String)

  case object Greet

}

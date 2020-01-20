import akka.actor.{Actor, ActorSystem, Inbox, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

import scala.concurrent.duration._

case class PrimeNumber(number: Int)
case class ListPrimeNumber(primeNumberList: Seq[Int])
case class AnswerMessage(isPrime: Boolean)

class PrimeNumberSearchActor extends Actor {
  def isPrime(n: Int): Boolean =
    if (n < 2) false else !((2 until n - 1) exists (n % _ == 0))

  def receive = {
    case PrimeNumber(num) => sender() ! AnswerMessage(isPrime(num))
    case _        =>
  }
}

class ListPrimeNumberSearchActor extends Actor {
  var listPrimeNumberSender = Actor.noSender
  var primeNumberCount = 0
  var answerCount = 0
  var totalAnswerCount = 0

  val router = {
    val routees = Vector.fill(4) {
      ActorRefRoutee(context.actorOf(Props[PrimeNumberSearchActor]))
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case ListPrimeNumber(primeNumberList) =>
      listPrimeNumberSender = sender()
      totalAnswerCount = primeNumberList.size
      primeNumberList.foreach(p => router.route(PrimeNumber(p), self))
    case AnswerMessage(isPrime) =>
      answerCount += 1
      if (isPrime) primeNumberCount += 1
      if (answerCount == totalAnswerCount)
        listPrimeNumberSender ! primeNumberCount
  }
}

object PrimeNumberSearch extends App {
  val system = ActorSystem("primeNumberSearch")
  val inbox = Inbox.create(system)
  implicit val sender = inbox.getRef()

  val listPrimeNumberSearchActor = system.actorOf(Props[ListPrimeNumberSearchActor], "listPrimeNumberSearchActor")
  listPrimeNumberSearchActor ! ListPrimeNumber(for(i <- 1010000 to 1040000) yield i)
  val result = inbox.receive(100.seconds)
  println(s"Result: ${result}")

  Thread.currentThread().join()
}

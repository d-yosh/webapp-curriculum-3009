import akka.actor.{ActorSystem, PoisonPill, Props}

object PoisonPillStudy extends App {
  val system = ActorSystem("poisonPillStudy")
  val child = system.actorOf(Props[ChildActor], "child")
  child ! "test"
  child ! PoisonPill
  child ! "hogehoge"

  Thread.currentThread().join()
}

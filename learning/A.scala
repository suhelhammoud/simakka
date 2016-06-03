package learning
import java.util.concurrent.TimeUnit

import akka.actor.Status.Success
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Suhel on 6/2/16.
  * Experimental code for learning scala
  */

object A extends App{



    println("Start Actor System")

    val system = ActorSystem("AkkaSim")
    val myActor = system.actorOf(Props[MyActor], "myActor")
    val otherActor = system.actorOf(Props[MyActor], "otherActor")

    myActor ! "a"
    myActor ! "sync"
    myActor.tell("testSync", otherActor)

  Thread.sleep(4000)

}

class MyActor extends Actor {
  val log = Logging(context.system, this)
  implicit val timeout = Timeout(5 seconds)

  def foo(): Unit = {
    sender ! "a"
    sender ! "a"
//    sender ! "a"
//    sender ? "sync"
//    val future = sender ask ("sync")
    log.info("foo()")
    log.info("going to ask {}, for blocking", sender)
    val future = Await.result(sender ? "sync", Duration(5, TimeUnit.SECONDS)) // <-- Blocks here for 5 seconds!
    log.info("ask "+ future)
    sender ! "a"
//    sender ! "b"
    }

  def receive = {

    case "a" => log.info("received {}", "a") ; sender ! "aOK"
    case "sync" =>log.info("received {}", "sync") ; sender ! "syncOK"

    case "testSync" => log.info("received {} from {}", "testSync", sender) ;foo()
    case v:String => log.info("received {}", v)
  }
}
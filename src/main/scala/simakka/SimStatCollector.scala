package simakka

import java.io.{FileWriter, PrintWriter}

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

/**
  * Created by Suhel on 6/2/16.
  *
  * @param fileName filename to be created to save in all stats measures
  */
class SimStatCollector(val fileName: String) extends Actor with SimConstants with ActorLogging {

  val pw = new PrintWriter(new FileWriter(fileName))
  pw.println(StatValueM.header)

  override def receive: Receive = {

    case sm: StatValueM => pw.println(sm.csvFormat)

    case CloseStatCollector => pw.close();

    case EndOfSimulation => //TODO test it later
      pw.close()
      self ! PoisonPill

    case _ => log.info("Unknown message")
  }
}

object SimStatCollectorTest extends SimConstants {

  def main(args: Array[String]) {
    //experimental test
    println("SimStatCollector Test")

    val system = ActorSystem("SimAkka")
    val statCollect = system.actorOf(Props(classOf[SimStatCollector], "/tmp/sim/stateCollector.txt"), "statCollect")

    val rnd = scala.util.Random

    for (i <- 1 to 200) {
      val sm = StatValueM(s"eName${rnd.nextInt(10)}", s"statName${rnd.nextInt(10)}",
        rnd.nextInt(100), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble())
      println(s"going to send $sm")
      statCollect ! sm
    }

    statCollect ! CloseStatCollector

    println("finished sending, wait and terminate")

    Thread.sleep(4000)
    system.terminate()
  }
}

package simakka.experiments.exp01

import simakka.{SimEntity, SimEv, SimEvent}

/**
  * Created by suhel on 6/26/16.
  */
class Ex01Client(name: String, localId: Int) extends SimEntity(name, localId) {

  var counterName: String = "counterEntity"

  override def handleEvent(ev: SimEv): Unit = {
    println("")

  }


  override def initEntity(data: Option[String]): Unit = {
    log.info(s"$name: init with data = $data")
//    counterName = data.get


    schedule(0, 33, counterName, None)
//    schedule(0, 33, counterName, None)

    for (i <- 1 to 2)
      schedule(0, 44, counterName, None)
//      schedule(0, 44, counterName, None)

    schedule(0, 55, counterName, None)
//    schedule(0, 55, counterName, None)

    log.info(s"$name, End of App ")
  }

}

class Ex01Counter(name: String, localId: Int) extends SimEntity(name, localId) {

  var counter = 0
  var startTime = 0L
  var stopTime = 0L

  override def handleEvent(ev: SimEv): Unit = {
    log.info(s"$name: received $ev")
    ev.tag match {
      case 33 => startTime = System.nanoTime()

      case 44 => counter += 1

      case 55 => {
        println(s"counter = $counter")
        stopTime = System.nanoTime();

        val duration = stopTime - startTime
        val thpt = (1e9 * counter) / duration
        log.info(s"Throughput = $thpt")
      }
    }

  }


  override def initEntity(data: Option[String]): Unit = {


  }
}

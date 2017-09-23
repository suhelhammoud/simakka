package simakka

import simakka.distributions._


/**
  * Created by Suhel on 6/17/16.
  */
class SimEntityQ(override val name: String, val id: Int) extends SimEntity(name, id) {
  //TODO set measure and random generators names using enum class
  createMeasure(STAT_INTERVAL, "stat_waiting_times")
  createMeasure(STAT_RATE, "stat_number_events")
  createMeasure(STAT_TIME, "stat_server_utilication")
  createMeasure(STAT_TIME, "stat_queue_lenght")

  SimRndExp("rnd_service_time", 5)
  SimRndExp("rnd_arrival_time", 6)

  override def handleEvent(ev: SimEv): Unit = {
    //TODO behaviour implementation
  }

  override def initEntity(data: Option[String]): Unit = {
    //TODO
  }
}

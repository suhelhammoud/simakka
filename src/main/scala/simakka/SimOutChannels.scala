package simakka

/**
  * Created by Suhel on 6/9/16.
  */

class OutQueue extends collection.mutable.PriorityQueue[SimEv]

object OutQueue {

  def apply(): OutQueue = new OutQueue()(new Ordering[SimEvent] {
    override def compare(x: SimEvent, y: SimEvent): Int = Math.signum(y.time - x.time).toInt
  })

}

class SimOutChannels {

  val outMap = scala.collection.mutable.Map[Int, OutQueue]()


  var numEmptyChannels = 0

  def addLink(to: Int): Unit = {
    //    assert(!outMap.contains(to)) //TODO decide weather unique or
    if (!outMap.contains(to)) {
      outMap.put(to, OutQueue())
      numEmptyChannels += 1
    }
  }


  //  val bufferedOutEvents = OutQueue()

  def flushEvents(oq: OutQueue): Array[SimEv] = {
    val result = Array.fill(oq.size)(oq.dequeue)
    result
  }

  def flushAll() = {
    outMap.mapValues(flushEvents(_))
  }

  def +=(simEv: SimEv): Unit = {
    addEvent(simEv)
  }

  def addEvent(simEv: SimEv): Unit = {
    assert(outMap.contains(simEv.to))
    outMap.get(simEv.to).get.enqueue(simEv)
  }

}



















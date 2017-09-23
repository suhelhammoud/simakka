package simakka

/**
  * Created by Suhel on 6/2/16.
  */
class SimChannel {
  /** This channel can advance its time safely to the lastNullMessageTime,
    * nullMessages update the maxTime to its time */
  var recentSourceTime = 0.0;

  var localChannelTime = 0.0 //TODO check to remove it later

  /** holds only SimEv types, nullMessages are not stored here */
  val queue = scala.collection.mutable.Queue[SimEv]()

  def empty = queue.isEmpty && localChannelTime >= recentSourceTime


  /** Return the time of first event in queue, if queue is empty return the time of last received nullMessage */
  //  def nextLowestTime = if( isEmpty) lastNullMessageTime else front.time;
  def nextLowestTime = if (!queue.isEmpty) front.time
  else if (recentSourceTime > localChannelTime)
    recentSourceTime
  else SimEvNone.time;


  def front: SimEvent = {
    if (empty) SimEvNone
    else if (queue.isEmpty) {
      LocalNullMessage(recentSourceTime)
    }
    else {
      queue.front
    }
  }

  def getNextEvent(): SimEvent = {
    if (empty) SimEvNone
    else if (queue.isEmpty) {
      localChannelTime = recentSourceTime
      LocalNullMessage(recentSourceTime)
    }
    else {
      localChannelTime = queue.front.time
      queue.dequeue()
    }
  }


  def addNullMessage(nm: NullMessage): Unit = {
    assert(nm.time > recentSourceTime)
    recentSourceTime = nm.time
  }

  def addEvent(ev: SimEv) = {
    assert(ev.time > recentSourceTime)
    queue.enqueue(ev)
    recentSourceTime = ev.time
  }

  //  def dequeue: SimEv = {
  //    val ev = queue.dequeue();
  //    localChannelTime = ev.time
  //    ev
  //  }

  override def toString = {
    val s = queue.mkString(", ")
    s"localTime=$localChannelTime ,maxTime = $recentSourceTime, queue = $s"
  }

}



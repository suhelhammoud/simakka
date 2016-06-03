package simakka

/**
  * Created by Suhel on 6/2/16.
  */
class  SimChannel  {
  /** This channel can advance its time safly to the maxTime,
    * nullMessages update the maxTime to its time*/
  var maxTime = 0.0;

  var lastEventTime = 0.0 //TODO check to remove it later

  /** holds only SimEv types, nullMessages are not stored here */
  val queue = scala.collection.mutable.Queue[SimEv]()

  def isEmpty = queue.isEmpty

  def front = queue.front

  /** Return the time of first event in queue, if queue is empty return the time of last received nullMessage*/
  def nextLowestTime = if( isEmpty) maxTime else front.time;

  /** true only if not empty and last nullMessage time (maxTime) is greater of lastEventTime **/
  def canAdvance = ! queue.isEmpty || maxTime > lastEventTime

  def addNullMessage(nm: NullMessage): Unit ={
    assert( nm.time > maxTime)
    maxTime = nm.time
  }

  def addEvent(ev: SimEv) = {
    queue.enqueue(ev)
    assert(ev.time > maxTime)
    maxTime = ev.time
  }

  def dequeue: SimEv = {
    val ev = queue.dequeue() ;
    lastEventTime = ev.time
    ev
  }

  override def toString = {
    val s = queue.mkString(", ")
    s"maxTime = $maxTime, queue = $s"
  }

}



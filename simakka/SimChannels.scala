package simakka

/**
  * Created by Suhel on 6/2/16.
  */
class SimChannels {

  final val LocalChannelID = -1;

  var localChannelTime = 0.0

  var numEmptyChannels = 0

  def getNumEmptyChannels = numEmptyChannels


  /*Hold input channels as
  * key: id of SimEntity who is sending events to this instance
  * value: corresponding SimChannel*/
  val channelMap = scala.collection.mutable.Map[Int, SimChannel]()

  val localEventsQueue = scala.collection.mutable.PriorityQueue[SimEv]()(new Ordering[SimEv] {
    override def compare(x: SimEv, y: SimEv): Int = Math.signum(y.time - x.time).toInt
  })

  def clock = localChannelTime

  /** true of any of the channels were empty **/
  def containsEmpty = numEmptyChannels > 0;

  /**
    * @param from: id of source SimEntity
    */
  def addLink(from: Int): Unit = {
    assert(!channelMap.contains(from))
    channelMap.put(from, new SimChannel())
    numEmptyChannels += 1
  }


  def addNullMessage(nm: NullMessage): Unit = {
    assert(channelMap contains (nm.from))
    val channel = channelMap.get(nm.from).get
    if (channel.empty) numEmptyChannels -= 1
    channel.addNullMessage(nm)
  }


  def addEvent(ev: SimEv): Unit = {
    if (ev.from == ev.to)
      localEventsQueue.enqueue(ev)
    else {
      assert(channelMap contains (ev.from))
      val channel = channelMap.get(ev.from).get
      if (channel.empty) numEmptyChannels -= 1
      channel.addEvent(ev)
    }
  }

  /**
    * Find the next event of the smallest time stamp in all input channels
    *
    * @return
    */
  def nextEvent(): (Int, SimEvent) = {
    val (minId, minEvent) = channelMap.mapValues(_.front).minBy(_._2.time)

    val minLocalEvent = if (localEventsQueue.isEmpty) SimEvNone else localEventsQueue.head

    if (minLocalEvent.time < minEvent.time)
      (LocalChannelID, localEventsQueue.dequeue())
    else {
      val minChannel = channelMap.get(minId).get
      minChannel.getNextEvent() //TODO
      if (minChannel.empty) numEmptyChannels += 1
      (minId, minEvent)
    }
  }

  def canAdvance = numEmptyChannels == 0


  override def toString() = {
    val headTimes = for {i <- channelMap} yield (i._1, i._2.front, i._2.empty)
    s"headTime = $localChannelTime, queue = ${headTimes.mkString("\n")}"
  }


  object SimChannels {

    def testSimChannels: Unit ={
      val e1 = SimEv(2, 1, 11, 22, None)
      val e2 = SimEv(3, 1, 22, 33, None)
      val channels = new SimChannels()
      channels.addEvent(e1)
      channels.addEvent(e2)

      println(channels)
    }

    def main(args: Array[String]): Unit = {
      testSimChannels
    }
  }


}

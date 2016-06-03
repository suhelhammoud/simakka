package simakka

/**
  * Created by Suhel on 6/2/16.
  */
class SimChannels {


  var headTime = 0.0

  /** true of any of the channels where empty**/
  var anyEmpty = true;

  /*Hold input channels as
  * key: id of SimEntity who is sending events to this instance
  * value: corresponding SimChannel*/
  val channelMap = scala.collection.mutable.Map[Int, SimChannel]()

  def clock = headTime

  def containsEmpty = anyEmpty

  /**
    * @param from: id of source SimEntity
    */
  def addLink(from: Int): Unit = {
    channelMap.put(from, new SimChannel())
  }


  def addNullMessage(nm: NullMessage): Unit = {
    assert(channelMap contains (nm.from))
    val channel = channelMap.get(nm.from).get
    channel.addNullMessage(nm)
  }


  def addEvent(ev: SimEv): Unit = {
    assert(channelMap contains (ev.from))
    val channel = channelMap.get(ev.from).get
    channel.addEvent(ev)
    anyEmpty = false;
  }

  /**
    * Find the next event of the smallest time stamp in all input channels
    * @return
    */
  def nextEvent = {
    val (channelKey, channelValue) = channelMap.minBy(_._2.nextLowestTime)
    channelValue.dequeue
  }

  /**
    *
    * @return
    */
  def canAdvance = channelMap.values.forall(_.canAdvance)

  override def toString() = {
    val headTimes = for {i <- channelMap} yield (i._1, i._2.front, i._2.isEmpty)
    s"headTime = $headTime, queue = ${headTimes.mkString("\n")}"
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

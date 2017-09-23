package simakka

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.util.Timeout

import scala.concurrent.duration._

/**
  * Created by Suhel on 6/2/16.
  */

/**
  *
  * @param name    Mandatory unique name for each SimEntity actor
  * @param localID Mandatory unique id for each of SimEntity actor
  *                //  * @param parent  ActorRef of parent actor (SimSystem)
  */
abstract class SimEntity(val name: String, val localID: Int)
  extends Actor with SimConstants with SimStat with ActorLogging with SimEntityLookup {
  /* Used to indicate the id of LocalEventQueue*/
  final val LocalChannelID = -1;

  /*Used as timeout for synchronous calls*/
  implicit val timeout = Timeout(50 seconds)


  /* Input channels */
  val simInChannels = new SimInChannels()

  /* One more separate input channel for local events */
  val localEventQueue = PriorityEventQueue.newInstance()

  //  val simOutChannels = new scala.collection.mutable.Queue[SimEvent]
  val simOutChannels = new SimOutChannels;

  var simTime = 0.0;
  var lastEventTime = 0.0

  var lookahead = 0.0;


  /**
    * Send local events
    *
    * @param delay
    * @param tag
    * @param data
    */
  def scheduleLocal(delay: Double, tag: Int, data: Option[Any]): Unit = {
    schedule(delay, tag, localID, localID, data)
  }


  /**
    * Send event from this SimEntity to other SimEntity by its id
    *
    * @param delay
    * @param tag
    * @param to
    * @param data
    */
  def schedule(delay: Double, tag: Int, to: Int, data: Option[Any]): Unit = {
    schedule(delay, tag, localID, to, data)
  }

  /**
    * Send event from this SimEntity to other SimEntity by its name
    *
    * @param delay
    * @param tag
    * @param toS
    * @param data
    */
  def schedule(delay: Double, tag: Int, toS: String, data: Option[Any]): Unit = {
//    val thatId = entitiesNames.get(toS)
    val thatId = getId(toS)

    if (thatId == None) {
      //TODO use getRef() methods here
      log.error("Could not find id for actor name:{}", toS)
      log.error(getLookInfo())
    } else {
      schedule(delay, tag, localID, thatId.get, data)
    }
  }

  /**
    * Send event between any other two SimEntities from this place, use SimEntity name for the source and destination
    *
    * @param delay
    * @param tag
    * @param fromS
    * @param toS
    * @param data
    */
  def schedule(delay: Double, tag: Int, fromS: String, toS: String, data: Option[Any]): Unit = {

    val from = getId(fromS)
    val to = getId(toS)
    if (from == None || to == None) {
      //TODO use getRef() methods here
      log.error("Could not find id for on of actors : {}, {}", fromS, toS)
      log.error(getLookInfo())

    } else {
      schedule(delay, tag, from.get, to.get, data)
    }
  }

  /**
    * This method is called by all other variations.
    *
    * @param delay
    * @param tag
    * @param from
    * @param to
    * @param data
    */
  def schedule(delay: Double, tag: Int, from: Int, to: Int, data: Option[Any]): Unit = {
    assert(delay >= 0 || contains(from) || contains(to))

    val nextTime = simTime + delay

    val ev = SimEv(nextTime, tag, from, to, data)

    /*Send the ev to the tmp destination SimEntity*/
    if (ev.to == localID) {
      localEventQueue.enqueue(ev)
    } else {
      simOutChannels += ev
    }
  }


  def handleEvent(ev: SimEv)

  def initEntity(data: Option[String])


  def updateStatistics(): Unit = {}


  def sendOutOneEvent(ev: SimEv): Unit = {
    val toRef = getRef(ev.to)
    if (toRef != None)
      toRef.get ! ev
  }

  private def sendOutEvents(time: Double, toId: Int, evs: Array[SimEv]): Unit = {
    if (getRef(toId) == None) return

    if (evs.nonEmpty)
      evs.foreach(sendOutOneEvent(_))
    else {
      getRef(toId).get ! NullMessage(time + lookahead, localID, toId)
    }
  }


  def canAdvance() = simInChannels.nonEmpty()


  def getNextEvent(): SimEvent = {

    val (minId, minEvent) = simInChannels.probNextEvent()
    val result = if (localEventQueue.nonEmpty
      && localEventQueue.head.time < minEvent.time) {
      localEventQueue.dequeue()
    } else {
      simInChannels.extractNextEvent(minId)
    }
    result
  }


  def tick(): Unit = {

    var timeChanged = false

    while (canAdvance) {

      val simEvent = getNextEvent()
      assert(simEvent.time >= simEvent.time)

      if (simEvent.time > simTime) {
        timeChanged = true
        lastEventTime = simTime
        simTime = simEvent.time
        updateStatistics()
      }

      handleEvent(simEvent.asInstanceOf[SimEv])
    }

    if (timeChanged) {
      val outEvents = simOutChannels.flushAll()
      //    outEvents.foreach( entry => sendOutEvent(entry._1, entry._2))
      outEvents.map(kv => sendOutEvents(simTime, kv._1, kv._2))
    }
  }

  override def receive: Receive = LoggingReceive {

    case POCK =>
      log.debug(s"$name: got POCK from system")
      sender ! PockBack(name, localID)

    case nm: NullMessage =>
      log.debug(s"$name: $nm")
      simInChannels.addEvent(nm)
      tick()

    case ev: SimEv =>
      simInChannels.addEvent(ev)
      tick()

    case ie: InitEntity =>
      log.debug(s"$name : $ie")
      initEntity(ie.data)

    //    case ev: SimEv =>
    case alf: AddLinkFrom =>
      log.debug(s"$name : $alf")
      assert(alf.to == localID);
      simInChannels.addLink(alf.from)

    case alt: AddLinkTo =>
      log.debug(s"$name : $alt")
      assert(alt.from == localID);
      simOutChannels.addLink(alt.to)

    case ar: AddRef =>
      log.debug(s"$name : addRef $ar")
      addRef(name, ar.id, ar.actorRef)
      log.debug(s"$name: lookup info=$getLookInfo()")

    case SetLookahead(delay, value) =>
      scheduleLocal(delay, TAG_SET_LOOKAHEAD, Some(value))

    case "Test" => log.info("Test")

    case _ => log.info("Unknown Message")
  }
}

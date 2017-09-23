package simakka

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}

/**
  * Created by Suhel on 6/2/16.
  */

/**
  *
  * @param name    Mandatory unique name for each SimEntity actor
  * @param localID Mandatory unique id for each of SimEntity actor
  *                //  * @param parent  ActorRef of parent actor (SimSystem)
  */
//class SimEntity(val name: String, val localID: Int, val parent: ActorRef)
abstract class SimEntity(val name: String, val localID: Int)
  extends Actor with SimConstants with SimStat with ActorLogging {

  /*Used as timeout of synchronous calls*/
  implicit val timeout = Timeout(5 seconds)

  /*SimEntity id: Int -> SimEntity Actor : ActorRef*/
  val entities = scala.collection.mutable.HashMap[Int, ActorRef]()

  /*SimEntity name: String -> SimEntity id: Int*/
  val entitiesNames = scala.collection.mutable.HashMap[String, Int]()

  /*Input channels*/
  val simChannels = new SimChannels()

  /*One separate channel for local events */
  val localChannel = new SimChannel()

  val localEvents = new scala.collection.mutable.Queue[SimEv]

  //  val simOutChannels = new scala.collection.mutable.Queue[SimEvent]
  val simOutChannels = new SimOutChannels;

  var simTime = 0.0;

  var lookahead = 0.0;

  private var tmpLocalEvents = collection.mutable.ArrayBuffer.empty[SimEv]

  private var tmpOutEvnets = collection.mutable.ArrayBuffer.empty[SimEv]

  /**
    * Get the actorRef any SimEntity by its name
    * If name of SimEntity is not saved in local map then get it from the SimSystem and save it locally for future calls
    *
    * @param name
    * @return ActorRef option value (None means not found)
    */
  def getRef(name: String): Option[ActorRef] = {
    val actorID = entitiesNames.get(name)
    if (actorID == None) {
      log.debug("getRef({})", name)
      val qnr = Await.result(context.parent ? QueryName(name), Duration(5, TimeUnit.SECONDS))
        .asInstanceOf[QueryIDResponse]
      if (qnr.actorRef == None) {
        log.error("SimEntity with name {} does not exist!", name)
        return None
      } else {
        entitiesNames.put(name, qnr.id)
        entities.put(qnr.id, qnr.actorRef.get)
        return qnr.actorRef
      }

    } else {
      val actorRef = entities.get(actorID.get)
      return actorRef
    }
  }

  /**
    * Get the ActorRef of any entity by its id, ask the parent actor if not found locally, cache it for future calls
    *
    * @param id
    * @return
    */
  def getRef(id: Int): Option[ActorRef] = {
    val actorRef = entities.get(id)
    if (actorRef == None) {
      log.debug("getRef({})", id)
      val qir = Await.result(context.parent ? QueryID(id), Duration(5, TimeUnit.SECONDS))
        .asInstanceOf[QueryIDResponse]
      if (qir.actorRef == None) {
        log.error("SimEntity with id {} does not exist!", id)
        return None
      } else {
        entities.put(qir.id, qir.actorRef.get)
        return qir.actorRef;
      }
    } else {
      actorRef
    }
  }

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
    val thatId = entitiesNames.get(toS)
    if (thatId == None) {
      //TODO use getRef() methods here
      log.error("Could not find id for actor name:{}", toS)
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
    val from = entitiesNames.get(fromS)
    val to = entitiesNames.get(toS)
    if (from == None || to == None) {
      //TODO use getRef() methods here
      log.error("Could not find id for on of actors : {}, {}", fromS, toS)
    } else {
      schedule(delay, tag, from.get, to.get, data)
    }
  }

  /**
    * This method is called by all other variations,
    *
    * @param delay
    * @param tag
    * @param from
    * @param to
    * @param data
    */
  def schedule(delay: Double, tag: Int, from: Int, to: Int, data: Option[Any]): Unit = {
    assert(delay >= 0)
    if (delay < 0 || !entities.contains(from) || !entities.contains(to)) {
      log.error("negative delay {}, or ids are not defined from:{}, to:{}", delay, from, to)
      return;
    }
    val nextTime = simTime + delay

    val ev = SimEv(nextTime, tag, from, to, data)

    /*Send the ev to the tmp destination SimEntity*/
    if (ev.to == localID) {
      localEvents += ev
    } else {
      simOutChannels += ev
    }

  }


  def handleEvent(ev: SimEvent)


  def processEvent(ev: SimEv) = {

    handleEvent(ev)

  }


  def tick(): Unit = {
    def sendOutEvent(time: Double, id: Int, evs: Array[SimEvent]): Unit = {
      if (evs.isEmpty)
        getRef(id).get ! NullMessage(time + lookahead, localID, id)
      else
        evs.foreach(getRef(id).get ! _)
    }

    while (simChannels.canAdvance) {
      val (id, ev) = simChannels.nextEvent()
      simTime = ev.time
      handleEvent(ev)

    }


    val outEvents = simOutChannels.flushAll()
    //    outEvents.foreach( entry => sendOutEvent(entry._1, entry._2))
    outEvents.map(v => sendOutEvent(simTime, v._1, v._2))
  }

  override def receive: Receive = {

    case nm: NullMessage =>
      simChannels.addNullMessage(nm)
      tick()

    case ev: SimEv =>
      simChannels.addEvent(ev)
      tick()

    //    case ev: SimEv =>
    case AddLinkFrom(from, to) =>
      assert(to == localID);
      simChannels.addLink(from)

    case AddLinkTo(from, to) =>
      assert(from == localID);
      simOutChannels.addLink(to)

    case AddRef(name: String, id: Int, actorRef: ActorRef) =>
      entities.put(id, actorRef)
      entitiesNames.put(name, id)

    case SetLookahead(delay, value) =>
      scheduleLocal(delay, TAG_SET_LOOKAHEAD, Some(value))

    case "Test" => log.info("Test")

    case _ => log.info("Unknown Message")
  }


}

package simakka

import akka.actor.ActorRef

/**
  * Created by Suhel on 6/2/16.
  * All constants and message types for all the library are defined here in one place,
  * any class needs to use it should extends this trait
  * TODO: based on functionality , split this into several traits later.
  */
trait SimConstants {
  //Messages used by the system have ids lower than LIMIT, user customized messages have ids start from LIMIT
  val TAG_LIMIT= 1000;
  val LOCALHOST = -1;

  val ACTOR_NOT_FOUND = null; //TODO: important, change type later to Object case class

  /*SimEv defined tags
  * TODO: use enumeration class to hold the tags later*/
  var index = 0; //temp var to increase Constants number

  val NOT_FOUND = index; index +=1;
  val TAG_SET_LOOKAHEAD = index ; index += 1
  val TAG_PAUSE = index; index +=1
  val TAG_PAUSE_END = index; index +=1
  val TAG_PROCESS = index; index += 1
  val TAG_PROCESS_END = index; index += 1;
}

case object EndOfSimulation

/* Constants messages of static classes communicated between actors */
case object TICK_OK;
case object PROB;
case object CloseStatCollector
case object SimStartTest


/* Messages with parameters communicated between actors*/
//Not used yet
case class  SimStart(name: String)
case class InitEntity(name: String, data: Option[String])

case class CreateEntity(tp: String, name: String)

case class RemoveEntity(data: Option[Any])
case class Transfer(data: Option[Any])

/*  Lookup SimEntity names across the system */
case class QueryID(id: Int)

case class QueryIDResponse(id: Int, actorRef: Option[ActorRef])

case class QueryName(name: String)
case class QueryNameResponse(name: String, id: Option[Int], actorRef: Option[ActorRef])


/**
  * @param from Source SimEntity id
  * @param to Destination SimEntity id
  */
case class AddLinkFrom(from: Int, to: Int)

case class AddLinkTo(from: Int, to: Int)

case class AddLink(from: Int, to: Int)

case class AddLinkN(sFrom: String, sTo: String)

case class AddRef(name: String, id: Int, actorRef: ActorRef)

case class AddEntity(id: Int, name: String)

/**
  * Message used to ask a SimEntity to dynamically change its lookahead value
  *
  * @param time simulation moment of change
  * @param value new value of lookahead
  */
case class SetLookahead(time: Double, value: Double)

// TODO used for initial test, to be deleted later
case object POCK
case class PockBack(name: String, id: Int)
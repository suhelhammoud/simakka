package simakka

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef}

/**
  * Created by Suhel on 6/2/16.
  */
class SimSystem  extends Actor with SimConstants with ActorLogging{

  val atomicId = new AtomicInteger
  final val entities = scala.collection.mutable.HashMap[Int, ActorRef]()
  final val entitiesNames = scala.collection.mutable.HashMap[String, Int]()

  ////  context.setReceiveTimeout(Duration(100, TimeUnit.MILLISECONDS))


  def addLink(from: Int, to: Int): Unit = {
    log.debug(s"AddLink($from, $to)")
    val fromEntity = entities.get(from)
    val toEntity = entities.get(to)
    if(toEntity == None || fromEntity == None){
      log.error(s"Either entity $from or $to is not available in entities yet")
    }else{
      toEntity.get ! AddLink(from, to)
    }
  }

  def queryID(id: Int) = {
    val result = QueryIDResponse(id,entities.get(id))
    log.info(s"queryID($id) = $result")
    sender ! result
  }

  def queryName(name: String)={
    val id = entitiesNames.get(name)
    val result = QueryNameResponse(name, id, if(id == None) None else entities.get(id.get))
    log.info(s"queryName($name) = $result")
    sender ! result
  }

  def addEntity(name: String): Unit ={
    //TODO implementation
   }

  override def receive: Receive = {

    case QueryID(id) => queryID(id)

    case AddLink(from, to) => addLink(from, to)

    case _ => log.info("Not recognized message")

  }


}

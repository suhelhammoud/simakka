package simakka

import akka.actor.ActorRef

/**
  * Created by Suhel on 6/16/16.
  */
trait SimEntityLookup {
  final private val entities = scala.collection.mutable.HashMap[Int, ActorRef]()
  final private val entitiesNames = scala.collection.mutable.HashMap[String, Int]()

  def newSimEv(time: Double, tag: Int, fromS: String, toS: String, data: Option[Any]) = {
    val from = getId(fromS).get
    val to = getId(toS).get
    SimEv(time, tag, from, to, data)
  }

  def getRef(idd: Int): Option[ActorRef] = {
    assert(entities.size == entitiesNames.size)
    entities.get(idd)
  }

  def getId(nm: String) = {
    assert(entities.size == entitiesNames.size)
    entitiesNames.get(nm)
  }

  def getRef(name: String): Option[ActorRef] = {
    assert(entities.size == entitiesNames.size)

    val lid = entitiesNames.get(name)
    if (lid == None) None
    else entities.get(lid.get)
  }

  def addRef(nm: String, idd: Int, actorRef: ActorRef): Unit = {
    assert(entities.size == entitiesNames.size)

    val bid = entities.put(idd, actorRef)
    println(s"addref idd:$idd, ref:$actorRef, bid:$bid")
    val bnm = entitiesNames.put(nm, idd)
    println(s"addref nm:$nm, idd:$idd, bid:$bnm")
    assert(bid == bnm)
    assert(entities.size == entitiesNames.size)

  }

  def contains(idd: Int) = entities.contains(idd)

  def contains(nm: String) = entitiesNames.contains(nm)

  def getLookInfo(): String = {
    assert(entities.size == entitiesNames.size)

    val names = entitiesNames.keySet.mkString(", ")
    val ids = entities.keySet.mkString(", ")
    s"names: ${entitiesNames.size} = $names, entites: ${entities.size} = $ids"
  }
}

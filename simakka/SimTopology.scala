package simakka

import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by suhel on 6/10/16.
  */


case class SimLink(from: String, to: String)


case class SimEntityD(name: String, id: Int) {


  //  val id: Int = system.atomicID.incrementAndGet()
  //  system.addEntity(this)


  def linkTo(that: SimEntityD)(implicit system: SimTopology) = {
    system.addEntity(that)

    system.addLink(SimLink(this.name, that.name))
  }

  def -->(that: SimEntityD)(implicit system: SimTopology) = linkTo(that)(system)

  def linkFrom(that: SimEntityD)(implicit system: SimTopology) = {
    system.addEntity(that)

    system.addLink(SimLink(that.name, this.name))
  }

  def <--(that: SimEntityD)(implicit system: SimTopology) = linkFrom(that)(system)

  def linkWith(that: SimEntityD)(implicit system: SimTopology) = {
    that.linkTo(this)(system)
    this.linkTo(that)(system)
  }

  def <-->(that: SimEntityD)(implicit system: SimTopology) = linkWith(that)(system)

}

class SimTopology {
  //  final val system = SimTopology


  final val system = this
  val atomicID = new AtomicInteger(1000)

  val links = new collection.mutable.ArrayBuffer[SimLink]()
  val entities = new collection.mutable.HashMap[String, SimEntityD]()


  implicit def stringToSimEntityD(nm: String): SimEntityD = {
    val e = system.entities.get(nm)
    if (e == None) {
      val ett = SimEntityD(nm, atomicID.incrementAndGet())
      addEntity(ett)
      ett
    }
    else
      e.get
  }

  def +=(any: Any) {
    any match {
      case se: SimEntityD => addEntity(se)

      case sl: SimLink => addLink(sl)

      case (sl1: SimLink, sl2: SimLink) =>
        addLink(sl1)
        addLink(sl2)

      case _ => println("Unknown Message")
    }
  }

  def addEntity(simEntityD: SimEntityD): Unit = {
    if (!system.entities.contains(simEntityD.name))
      system.entities.put(simEntityD.name, simEntityD)
  }

  def addLink(link: SimLink): Unit = {
    if (!system.links.contains(link))
      system.links += link
  }


  def enable(s: String) = {
    //TODO
    this
  }

  def to(s: String) = {
    //TODO
    this
  }

  def after(time: Double) {
    //TODO
    this
  }

  def terminate = {
    //TODO
    this
  }

  override def toString() = {
    val es = entities.mkString("\n")
    val ls = links.mkString("\n")

    s"entities:\n$entities \nlinks:\n$links"
  }

  def runDSL() = {}
}


object SimTopology {
  def main(args: Array[String]) {
    println("Start SimTopology")
    val app = new MySimApp
    app.runDSL()
    println(app.toString())
  }
}

class MySimApp extends SimTopology {
  override def runDSL(): Unit = {
    implicit val system = this

    "Amer" --> "Suhel"
    "Amer" --> "Suhel"

    "Amer" <-- "Suhel"

    "Rami" <--> "Suhel"

    this += "amer"


    this += "Maryam"

    this enable "tracing" to "data/trace.txt"
    this enable "reports" to "data/report.txt"

  }


}

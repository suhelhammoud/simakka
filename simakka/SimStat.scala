package simakka


/**
  * Created by Suhel on 6/2/16.
  */


trait SimStat {
  val STAT_RATE = 0;
  val STAT_INTERVAL = 1;
  val STAT_TIME = 2;
  val STAT_USER = 3;

  var simTime: Double


  val stats = collection.mutable.Map[String, StatValue]()

  def getMeasure(name: String) = stats.getOrElse(name, 0)

  def updateMeasureTime(sv: StatValue, newValue: Double) = {
    sv.accum = (simTime - sv.lastTime) * sv.value
    sv.lastTime = simTime
    sv.value = newValue
  }

  def updateMeasureRate(sv: StatValue) = {
    sv.accum += 1;
    sv.lastTime = simTime;
  }

  def updateMeasureInterval(sv: StatValue) = {
    sv.accum += simTime - sv.lastTime
    sv.lastTime = simTime;
  }

  def updateMeasureUser(sv: StatValue, value: Double) = {
    sv.accum += value
    sv.lastTime = simTime;
  }

  def createMeasure(stateType: Int, name: String): Unit = {
    stats.put(name, StatValue(stateType, 0, 0, 0))
  }

  def updateMeasure(name: String, value: Double) = {
    assert(stats.contains(name))
    val sv = stats.get(name).get

    sv.statType match {
      case STAT_TIME => updateMeasureTime(sv, value)
      case STAT_RATE => updateMeasureRate(sv)
      case STAT_INTERVAL => updateMeasureInterval(sv)
      case STAT_USER => updateMeasureUser(sv, value)
      case _ => println("not recognized")
    }
  }

  def getStat(name: String) = {
    stats.get(name)
  }

  def getStats = stats


}

/**
  * Serialized StateValue version to collect statistics via separate actor
  *
  * @param entityName
  * @param statName
  * @param statType
  * @param value
  * @param accum
  * @param lastTime
  */
case class StatValueM(val entityName: String, val statName: String,
                      val statType: Int, val value: Double, val accum: Double, val lastTime: Double){

  def csvFormat = s"$entityName, $statName, $statType, $value, $accum, $lastTime"
}

/**
  * For providing static header function
  */
object StatValueM{
  def header = List("entityName", "statName", "statType", "value", "accum", "lastTime").mkString(", ")
}


/**
  * Basic unit to store one measure
  * @param statType
  * @param value
  * @param accum
  * @param lastTime
  */
class StatValue(val statType: Int, var value: Double, var accum: Double,
                var lastTime: Double) {

  def toStatValueM(entityName: String, statName: String) =
    StatValueM(entityName, statName,statType, value, accum, lastTime)

  override def toString = {
    s"StateValue(statTyple:$statType, value:$value, accum:$accum, lastTime:$lastTime)"
  }
}

/**
  * For providing apply method and future static vars and functions
  */
object StatValue {

  def apply(statTyple: Int, value: Double, accum: Double, lastTime: Double) =
    new StatValue(statTyple, value, accum, lastTime)

  def testStatValue(): Unit ={
    /** dummy class used instead of SimEntity to test statValue */
    class TestStat extends SimStat {   var simTime = 0.0 }

    val ts = new TestStat()

    ts.createMeasure(ts.STAT_TIME, "arrivals")
    ts.simTime = 1;
    ts.updateMeasure("arrivals", 3)
    println(ts.getStat("arrivals"))
    ts.simTime = 10
    ts.updateMeasure("arrivals", 2)
    println(ts.getStat("arrivals"))
    val tsm = ts.getStat("arrivals").get.toStatValueM("entity1", "stat1")

    println(tsm)
    println(StatValueM.header)
    println(tsm.csvFormat)
  }

  def main(args: Array[String]) {
    testStatValue()
  }
}


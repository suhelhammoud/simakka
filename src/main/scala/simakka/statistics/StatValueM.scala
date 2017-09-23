package simakka.statistics

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
                      val statType: Int, val value: Double, val accum: Double, val lastTime: Double) {

  def csvFormat = s"$entityName, $statName, $statType, $value, $accum, $lastTime"
}

object StatValueM {

  /* To provide static header captions */
  def header = List("entityName", "statName", "statType", "value", "accum", "lastTime").mkString(", ")
}
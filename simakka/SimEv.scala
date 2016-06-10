package simakka


/**
  * Created by Suhel on 6/2/16.
  */

sealed trait SimEvent {
  val time: Double
}

final case class SimEv(time: Double, tag: Int, from: Int, to: Int, data: Option[Any]) extends SimEvent

final case class LocalNullMessage(time: Double) extends SimEvent

final object SimEvNone extends SimEvent {
  override val time: Double = Double.MaxValue
}

case class NullMessage(time: Double, from: Int, to: Int)


package simakka


/**
  * Created by Suhel on 6/2/16.
  */

case class SimEv(time: Double, tag: Int, from: Int, to: Int, data: Option[Any] )

case class NullMessage(time: Double, from: Int, to: Int)


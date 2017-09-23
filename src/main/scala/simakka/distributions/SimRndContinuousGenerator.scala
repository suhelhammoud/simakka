package simakka.distributions

/**
  * Created by Suhel on 6/10/16.
  */
trait SimRndContinuousGenerator extends SimRndGenerator {
  /**
    * Sample the random number generator.
    *
    * @return The sample
    */
  def sample(): Double
}

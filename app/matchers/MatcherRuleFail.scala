package matchers

import models.ClientInfo

/**
 * A matcher rule that always fails, intended to drag down scores for testing purposes
 */
class MatcherRuleFail(cap: Int = 5) extends MatcherRule {
  override val name: String = "TESTING: Guaranteed failure"

  /**
   * A rough estimation of how much self will enjoy playing with each other in this aspect.
   *
   * @return The estimation, on a scale of 0-max
   */
  override def score(self: ClientInfo, other: ClientInfo): Int = 0

  override val max: Int = cap
}

object MatcherRuleFail extends MatcherRuleFail(5)

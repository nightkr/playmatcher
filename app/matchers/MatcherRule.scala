package matchers

import models.ClientInfo

/**
 * A single matcher rule, should represent one aspect of matching.
 */
trait MatcherRule {
  val name: String

  /**
   * The maximum score
   */
  val max: Int

  /**
   * A rough estimation of how much self will enjoy playing with each other in this aspect.
   *
   * @return The estimation, on a scale of 0-max
   */
  def score(self: ClientInfo, other: ClientInfo): Int
}

class MatcherRuleUtil(val rule: MatcherRule) extends AnyVal {
  /**
   * Returns a new MatcherRule based on rule, where the values of max and score (here both referred to as x) are overridden as f(x)
   */
  def map(f: Int => Int): MatcherRule = new MatcherRule {
    override val name: String = rule.name

    override def score(self: ClientInfo, other: ClientInfo): Int = f(rule.score(self, other))

    override val max: Int = f(rule.max)
  }
}

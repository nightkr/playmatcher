package matchers

import models.ClientInfo

case class Matcher(selfInfo: ClientInfo, rules: MatcherRule*) {
  /**
   * The maximum score possible with the current ruleset
   */
  private lazy val max = rules.map(_.max).sum

  /**
   * The total raw score from the rules, ranged 0-max
   */
  private def calculateScore(other: ClientInfo): Int = rules.map(_.score(selfInfo, other)).sum

  /**
   * The total score from the rules, ranged 0-100
   */
  def score(other: ClientInfo): Int = calculateScore(other) * 100 / max
}

object Matcher {
  def default(self: ClientInfo): Matcher = Matcher(self, MatcherRuleCommonGames/*, MatcherRuleFail*/)
}

package matchers

import models.ClientInfo

class MatcherRuleCommonGames(cap: Int = 5) extends MatcherRule {
  override val name: String = "Games in common"
  override val max: Int = cap

  override def score(self: ClientInfo, other: ClientInfo): Int = self.games.intersect(other.games).length.min(max)
}

object MatcherRuleCommonGames extends MatcherRuleCommonGames(5)
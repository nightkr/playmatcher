import scala.language.implicitConversions

package object matchers {
  implicit def matcherRuleUtil(rule: MatcherRule): MatcherRuleUtil = new MatcherRuleUtil(rule)
}

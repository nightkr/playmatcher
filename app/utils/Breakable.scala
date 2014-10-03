package utils

import scala.util.control.ControlThrowable

object Breakable {
  type Break = () => Nothing
  private def break(tag: Tag): Break = () => throw new BreakException(tag)

  def apply(f: Break => Unit): Unit = {
    val tag = new Tag
    try {
      f(break(tag))
    } catch {
      case BreakException(`tag`) =>
    }
  }

  private class Tag
  private class BreakException(val tag: Tag) extends ControlThrowable
  private object BreakException {
    def unapply(ex: BreakException): Option[Tag] = Some(ex.tag)
  }
}

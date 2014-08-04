import play.api.mvc.Call

import scala.language.implicitConversions

package object utils {
  implicit def enrichCall(call: Call): RichCall = RichCall(call)
}

import scala.language.implicitConversions

import play.api.mvc.Result

package object models {
  implicit def result2resultUserUtil(res: Result): ResultUserUtil = ResultUserUtil(res)
}

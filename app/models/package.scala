import play.api.mvc.Result

import scala.language.implicitConversions

package object models {
  implicit def result2resultUserUtil(res: Result): ResultUserUtil = ResultUserUtil(res)
}

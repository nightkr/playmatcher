package utils

import org.jboss.netty.handler.codec.http.QueryStringEncoder
import play.api.mvc.Call

case class RichCall(call: Call) extends AnyVal {
  def withQueryString(qs: Map[String, Seq[String]]): Call = {
    val qse = new QueryStringEncoder(call.url)
    for {
      (key, values) <- qs
      value <- values
    } {
      qse.addParam(key, value)
    }
    call.copy(url = qse.toString)
  }
}

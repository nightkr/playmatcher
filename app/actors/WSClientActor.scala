package actors

import actors.ClientManager.{MatchFound, Register}
import akka.actor._
import models.ClientInfo
import play.api.libs.json.{JsString, JsObject, JsValue}

class WSClientActor(out: ActorRef, info: ClientInfo) extends Actor with HasClientManager {
  override def receive: Receive = {
    case msg: JsValue =>
      out ! msg
      println(msg)

    case MatchFound(other) =>
      out ! JsObject(Seq(
        "event" -> JsString("match found"),
        "person" -> other.toJson
      ))
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart() {
    super.preStart()
    clientManager ! Register(info)
  }
}

object WSClientActor {
  def props(out: ActorRef, info: ClientInfo) = Props(new WSClientActor(out, info))
}
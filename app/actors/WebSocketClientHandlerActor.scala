package actors

import actors.ClientManager.{MatchFound, Register}
import akka.actor._
import models.ClientInfo
import play.api.libs.json.{JsString, JsObject, JsValue}

class WebSocketClientHandlerActor(out: ActorRef, info: ClientInfo) extends Actor with HasClientManager {
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

  override def preStart() {
    super.preStart()
    clientManager ! Register(info)
  }
}

object WebSocketClientHandlerActor {
  def props(out: ActorRef, info: ClientInfo) = Props(new WebSocketClientHandlerActor(out, info))
}
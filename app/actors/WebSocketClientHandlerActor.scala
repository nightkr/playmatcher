package actors

import actors.ClientManager.{MatchFound, NewMatchThreshold, Register}
import akka.actor._
import models.ClientInfo
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

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

    case NewMatchThreshold(threshold) =>
      out ! JsObject(Seq(
        "event" -> JsString("new threshold"),
        "threshold" -> JsNumber(math.round(threshold))
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
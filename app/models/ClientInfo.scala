package models

import play.api.libs.json.{JsArray, JsString, JsObject}

case class ClientInfo(name: String, games: Seq[Game]) {
  def toJson = JsObject(Seq(
    "name" -> JsString(name),
    "games" -> JsArray(games.map(_.toJson))
  ))
}

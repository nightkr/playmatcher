package models

import play.api.libs.json.{JsArray, JsString, JsObject}

case class Game(name: String) {
  def toJson = JsObject(Seq("name" -> JsString(name)))
}

case class ClientInfo(name: String, games: Seq[Game]) {
  def toJson = JsObject(Seq(
    "name" -> JsString(name),
    "games" -> JsArray(games.map(_.toJson))
  ))
}

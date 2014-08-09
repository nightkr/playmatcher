package models

import play.api.libs.json.{JsString, JsObject}
import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._

import scala.slick.lifted.ProvenShape

case class GameID(id: Long) extends AnyVal with BaseId
object GameID extends IdCompanion[GameID]

case class Game(id: Option[GameID], name: String) extends WithId[GameID] {
  def toJson = JsObject(Seq("name" -> JsString(name)))
}

class Games(tag: Tag) extends IdTable[GameID, Game](tag, "GAMES") {
  def name = column[String]("NAME")

  def nameIndex = index("NAME_INDEX", name, unique = true)

  override def * : ProvenShape[Game] = (id.?, name) <> (Game.tupled, Game.unapply)
}
package models

import play.api.libs.json.{JsString, JsObject}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import utils.SteamAPI

import scala.concurrent.Future
import scala.slick.lifted.ProvenShape

case class GameID(id: Long) extends AnyVal with BaseId
object GameID extends IdCompanion[GameID]

case class Game(id: Option[GameID], name: String, steamAppID: Option[Long] = None, icon: Option[String] = None) extends WithId[GameID] {
  def toJson = JsObject(Seq("name" -> JsString(name)))
}

class Games(tag: Tag) extends IdTable[GameID, Game](tag, "GAMES") {
  def name = column[String]("NAME")
  def steamAppID = column[Option[Long]]("STEAM_APPID")
  def icon = column[Option[String]]("icon")

  def nameIndex = index("NAME_INDEX", name, unique = true)
  def steamAppIDIndex = index("STEAM_APPID_INDEX", steamAppID, unique = true)

  override def * : ProvenShape[Game] = (id.?, name, steamAppID, icon) <> (Game.tupled, Game.unapply)
}

object Games extends TableQuery[Games](new Games(_)) {
  def getOrCreate(name: String, steamAppID: Option[Long], icon: Option[String])(implicit session: Session): GameID = {
    val existingGame = this.filter(_.name === name).firstOption
    val game = existingGame.getOrElse(Game(None, name, steamAppID, icon))
    val updatedGame: Game = game.copy(steamAppID = game.steamAppID.orElse(steamAppID), icon = game.icon.orElse(icon))
    val id = updatedGame.id.getOrElse(this.returning(this.map(_.id)).insert(updatedGame))
    val updatedWithID = updatedGame.copy(id = Some(id))
    if (updatedWithID != game) {
      this.filter(_.id === id).update(updatedWithID)
    }
    id
  }
}

case class UserGameID(id: Long) extends AnyVal with BaseId
object UserGameID extends IdCompanion[UserGameID]

case class UserGame(id: Option[UserGameID], userID: UserID, gameID: GameID) extends WithId[UserGameID]

class UserGames(tag: Tag) extends IdTable[UserGameID, UserGame](tag, "USER_GAMES") {
  def userID = column[UserID]("USER_ID")
  def gameID = column[GameID]("GAME_ID")

  def user = foreignKey("USER_FK", userID, Users)(_.id)
  def game = foreignKey("GAME_FK", gameID, Games)(_.id)

  override def * : ProvenShape[UserGame] = (id.?, userID, gameID) <> (UserGame.tupled, UserGame.unapply)
}
object UserGames extends TableQuery[UserGames](new UserGames(_)) {
  def populateFromSteam(id: Long, user: UserID)(sessionLender: (Session => Int) => Int): Future[Int] = {
    SteamAPI().User(id).games().map { steamGames =>
      sessionLender { implicit session =>
        val games = steamGames.map(game => Games.getOrCreate(game.name, Some(game.appid), Some(game.iconPath)))
        val preExistingGames = this.filter(_.userID === user).filter(_.gameID inSet games).list
        val newGames = games.filterNot(preExistingGames.map(_.gameID).contains)

        this.insertAll(newGames.map(UserGame(None, user, _)): _*).get
      }
    }
  }
}

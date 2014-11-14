package models

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsNumber, JsObject, JsString}
import utils.SteamAPI

import scala.concurrent.Future
import scala.slick.lifted.ProvenShape

case class GameID(id: Long) extends AnyVal with BaseId

object GameID extends IdCompanion[GameID]

case class Game(id: Option[GameID], name: String, steamAppID: Option[Long] = None, icon: Option[String] = None) extends WithId[GameID] {
  def toJson = JsObject(Seq("id" -> JsNumber(id.get.id), "name" -> JsString(name)))
}

class Games(tag: Tag) extends IdTable[GameID, Game](tag, "games") {
  def nameIndex = index("NAME_INDEX", name, unique = true)

  def name = column[String]("NAME")

  def steamAppIDIndex = index("STEAM_APPID_INDEX", steamAppID, unique = true)

  def steamAppID = column[Option[Long]]("STEAM_APPID")

  override def * : ProvenShape[Game] = (id.?, name, steamAppID, icon) <>(Game.tupled, Game.unapply)

  def icon = column[Option[String]]("icon")
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

case class UserGame(id: Option[UserGameID], userID: UserID, gameID: GameID, enabled: Boolean = true) extends WithId[UserGameID]

class UserGames(tag: Tag) extends IdTable[UserGameID, UserGame](tag, "user_games") {
  def user = foreignKey("USER_FK", userID, Users)(_.id)

  def game = foreignKey("GAME_FK", gameID, Games)(_.id)

  override def * : ProvenShape[UserGame] = (id.?, userID, gameID, enabled) <>(UserGame.tupled, UserGame.unapply)

  def gameID = column[GameID]("GAME_ID")

  def userID = column[UserID]("USER_ID")

  def enabled = column[Boolean]("ENABLED")
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

  def byUser(user: UserID)(implicit session: Session): Seq[(UserGame, Game)] = {
    this.filter(_.userID === user).innerJoin(Games).on(_.gameID === _.id).list
  }

  def setEnabled(game: GameID, user: UserID, enabled: Boolean)(implicit session: Session): Boolean = {
    this
      .filter(_.gameID === game)
      .filter(_.userID === user)
      .map(_.enabled)
      .update(enabled) > 0
  }

  def setAllEnabled(user: UserID, enabled: Boolean)(implicit session: Session): Int = {
    this
      .filter(_.userID === user)
      .map(_.enabled)
      .update(enabled)
  }
}

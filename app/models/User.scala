package models

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import play.api.Application
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{RequestHeader, Result}
import utils.SteamAPI

import scala.concurrent.Future
import scala.slick.lifted
import scala.slick.lifted.ProvenShape

case class UserID(id: Long) extends AnyVal with BaseId

object UserID extends IdCompanion[UserID]

case class User(id: Option[UserID], name: Option[String] = None, steamid: Option[Long] = None, banned: Boolean = false) extends WithId[UserID]

class Users(tag: Tag) extends IdTable[UserID, User](tag, "users") {
  override def * : ProvenShape[User] = (id.?, name, steamid, banned) <>(User.tupled, User.unapply)

  def name = column[Option[String]]("NAME")

  def steamid = column[Option[Long]]("STEAMID")

  def banned = column[Boolean]("BANNED")
}

object Users extends TableQuery[Users](new Users(_)) {
  def getOrRegisterIDBySteamid(steamid: Long)(implicit req: RequestHeader, session: Session): UserID = {
    getExistingIDBySteamid(steamid).firstOption.getOrElse(registerSteamid(steamid))
  }

  def getExistingIDBySteamid(steamid: Long): lifted.Query[lifted.Column[UserID], UserID, Seq] = for {
    u <- this
    if u.steamid === steamid
  } yield u.id

  def registerSteamid(steamid: Long)(implicit req: RequestHeader, session: Session): UserID = {
    val uid = currentIDOrCreate()
    this.filter(_.id === uid).map(_.steamid).update(Some(steamid))
    uid
  }

  def currentIDOrCreate()(implicit req: RequestHeader, session: Session): UserID =
    Users.current.map(_.id).firstOption
      .getOrElse(Users.returning(Users.map(_.id)) += User(None))

  def current(implicit req: RequestHeader): lifted.Query[Users, User, Seq] = this.filter(_.id === currentID)

  def currentID(implicit req: RequestHeader): Option[UserID] = {
    val idStr = req.session.get("uid")
    idStr.map(i => UserID(i.toLong))
  }

  def updateSteamInfo(steamid: Long)(sessionLender: (Session => Int) => Int)(implicit req: RequestHeader, app: Application): Future[Unit] = {
    SteamAPI().User(steamid).summary().map(_.foreach { summary =>
      sessionLender { implicit session =>
        this.filter(_.steamid === steamid).map(_.name).update(Some(summary.personaname))
      }
    })
  }

  object Steam {
    private val openIDRegex = "http://steamcommunity.com/openid/id/(\\d+)".r

    def openIDtoSteamid(openID: String): Long = openID match {
      case openIDRegex(memberID) => memberID.toLong
    }
  }

}

case class ResultUserUtil(res: Result) extends AnyVal {
  def withUser(uid: UserID): Result = res.withSession("uid" -> uid.id.toString)
}
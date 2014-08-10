package models

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import play.api.mvc.{RequestHeader, Result, Request}

import scala.slick.lifted
import scala.slick.lifted.ProvenShape
import scala.util.Try

case class UserID(id: Long) extends AnyVal with BaseId
object UserID extends IdCompanion[UserID]

case class User(id: Option[UserID], banned: Boolean = false) extends WithId[UserID]

class Users(tag: Tag) extends IdTable[UserID, User](tag, "USERS") {
  def banned = column[Boolean]("BANNED")
  override def * : ProvenShape[User] = (id.?, banned) <> (User.tupled, User.unapply)
}

object Users extends TableQuery[Users](new Users(_)) {
  def currentID(implicit req: RequestHeader): Option[UserID] = {
    val idStr = req.session.get("uid")
    idStr.map(i => UserID(i.toLong))
  }

  def current(implicit req: RequestHeader): lifted.Query[Users, User, Seq] = this.filter(_.id === currentID)

  def currentIDOrCreate()(implicit req: RequestHeader, session: Session): UserID =
    Users.current.map(_.id).firstOption
      .getOrElse(Users.returning(Users.map(_.id)) += User(None))

  def getOrRegisterIDByIdentity(kind: String, value: String)(implicit req: RequestHeader, session: Session): UserID = {
    getExistingIDByIdentity(kind, value).firstOption.getOrElse(registerIdentity(kind, value))
  }

  def getExistingIDByIdentity(kind: String, value: String): lifted.Query[lifted.Column[UserID], UserID, Seq] = for {
    i <- Identities
    if i.kind === kind
    if i.value === value
    u <- i.user
  } yield u.id
  
  def registerIdentity(kind: String, value: String)(implicit req: RequestHeader, session: Session): UserID = {
    val uid = currentIDOrCreate()
    Identities += Identity(None, uid, kind, value)
    uid
  }
}

case class ResultUserUtil(res: Result) extends AnyVal {
  def withUser(uid: UserID): Result = res.withSession("uid" -> uid.id.toString)
}
package models

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._

import scala.slick.lifted.ProvenShape

case class IdentityID(id: Long) extends AnyVal with BaseId
object IdentityID extends IdCompanion[IdentityID]

case class Identity(id: Option[IdentityID], userID: UserID, kind: String, value: String) extends WithId[IdentityID]

object Identity {
  object Kind {
    val STEAM = "STEAM"
  }

  object Steam {
    private val openIDRegex = "http://steamcommunity.com/openid/id/(\\d+)".r
    def openIDToIdentityValue(openID: String): String = openID match {
      case openIDRegex(memberID) => memberID
    }
  }
}

class Identities(tag: Tag) extends IdTable[IdentityID, Identity](tag, "IDENTITIES") {
  def userID = column[UserID]("USER_ID")
  def kind = column[String]("KIND")
  def value = column[String]("VALUE")

  def user = foreignKey("USER_FK", userID, Users)(_.id)
  def userKindIndex = index("USER_KIND_INDEX", (userID, kind), unique = true)
  def kindValueIndex = index("KIND_VALUE_INDEX", (kind, value), unique = true)

  override def * : ProvenShape[Identity] = (id.?, userID, kind, value) <> ((Identity.apply _).tupled, Identity.unapply)
}

object Identities extends TableQuery[Identities](new Identities(_))

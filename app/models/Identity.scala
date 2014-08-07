package models

import play.api.db.slick.Config.driver.simple._

import scala.slick.lifted.ProvenShape

case class Identity(id: Int, userID: Int, kind: String, value: String)

class Identities(tag: Tag) extends Table[Identity](tag, "IDENTITIES") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def userID = column[Int]("USER_ID")
  def kind = column[String]("KIND")
  def value = column[String]("VALUE")

  override def * : ProvenShape[Identity] = (id, userID, kind, value) <> (Identity.tupled, Identity.unapply)
}

object Identities extends TableQuery[Identities](new Identities(_))

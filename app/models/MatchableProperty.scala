package models

import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._

import scala.slick.lifted.ProvenShape

case class MatchablePropertyID(id: Long) extends AnyVal with BaseId

object MatchablePropertyID extends IdCompanion[MatchablePropertyID]

case class MatchableProperty(id: Option[MatchablePropertyID], label: String) extends WithId[MatchablePropertyID]

class MatchableProperties(tag: Tag) extends IdTable[MatchablePropertyID, MatchableProperty](tag, "matchable_properties") {
  def label = column[String]("LABEL")

  override def * : ProvenShape[MatchableProperty] = (id.?, label) <> (MatchableProperty.tupled, MatchableProperty.unapply)
}

object MatchableProperties extends TableQuery[MatchableProperties](new MatchableProperties(_))

case class MatchablePropertyChoiceID(id: Long) extends AnyVal with BaseId

object MatchablePropertyChoiceID extends IdCompanion[MatchablePropertyChoiceID]

case class MatchablePropertyChoice(id: Option[MatchablePropertyChoiceID], label: String, value: Int, property: MatchablePropertyID) extends WithId[MatchablePropertyChoiceID]

class MatchablePropertyChoices(tag: Tag) extends IdTable[MatchablePropertyChoiceID, MatchablePropertyChoice](tag, "matchable_property_choices") {
  def label = column[String]("LABEL")
  def value = column[Int]("VALUE")
  def propertyID = column[MatchablePropertyID]("PROPERTY_ID")
  def property = foreignKey("PROPERTY_FK", propertyID, MatchableProperties)(_.id)

  override def * : ProvenShape[MatchablePropertyChoice] = (id.?, label, value, propertyID) <> (MatchablePropertyChoice.tupled, MatchablePropertyChoice.unapply)
}

object MatchablePropertyChoices extends TableQuery[MatchablePropertyChoices](new MatchablePropertyChoices(_))

case class UserMatchablePropertyID(id: Long) extends AnyVal with BaseId


package controllers

import models.{User, Users}
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple.{Session => DBSession, _}
import play.api.Play.current
import play.api.db.slick.DB
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._

import scala.concurrent.Future

object DBAction extends ActionBuilder[({type T[A] = (Request[A], DBSession)})#T] {
  override def invokeBlock[A](request: Request[A],
                              block: ((Request[A], DBSession)) => Future[Result]
                               ): Future[Result] = {
    DB.withSession(block(request, _))
  }
}

class UserAction(orElse: Result) extends ActionBuilder[({type T[A] = (Request[A], DBSession, User)})#T] {
  override def invokeBlock[A](request: Request[A],
                              block: ((Request[A], DBSession, User))
                                => Future[Result]
                               ): Future[Result] = {
    DB.withSession { implicit session =>
      Users.current(request).firstOption match {
        case Some(user) => block(request, session, user)
        case None => Future.successful(orElse)
      }
    }
  }
}

trait PMController extends Controller {

  object UIUserAction extends UserAction(Redirect(routes.SteamAuthentication.steamLogin()))

  object APIUserAction extends UserAction(Unauthorized(JsObject(Seq(
    "error" -> JsString("unauthorized")
  ))))

}

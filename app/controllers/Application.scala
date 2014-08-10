package controllers

import actors.WSClientActor
import models._
import play.api.Logger
import play.api.db.slick.DB
import utils._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.libs.openid.{SteamOpenID, BaseOpenIDCompanion}
import play.api.libs.concurrent.Execution.Implicits._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._

import scala.util.Try


object Application extends Controller {

  val clientInfoForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "games" -> seq(nonEmptyText.transform[Game](Game(None, _), _.name))
    )(ClientInfo.apply)(ClientInfo.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index(clientInfoForm.bindFromRequest()))
  }

  def connect = Action { implicit request =>
    clientInfoForm.bindFromRequest().fold[Result](
      _ => Redirect(routes.Application.index().withQueryString(request.queryString)),
      _ => Ok(views.html.matcher())
    )
  }

  def connectWS = WebSocket.acceptWithActor[JsValue, JsValue] { implicit request => out =>
    val clientInfo = clientInfoForm.bindFromRequest(request.queryString).get
    WSClientActor.props(out, clientInfo)
  }

}

object SteamAuthentication extends Controller {
  private def openIDRealm(implicit req: RequestHeader): Option[String] = Some(routes.Application.index().absoluteURL())

  private val steamOpenID = "http://steamcommunity.com/openid"

  def steamLogin(returnTo: Option[String]) = Action.async { implicit request =>
    val realReturnTo = Seq(
      returnTo,
      request.headers.get("Referer"),
      Some(routes.Application.index().url)
    ).filter(_.isDefined).head.get
    SteamOpenID().redirectURL(steamOpenID, routes.SteamAuthentication.steamCallback(realReturnTo).absoluteURL(), realm = openIDRealm)
      .map(TemporaryRedirect)
      .recover { case ex: Throwable =>
        Logger.error("OpenID redirect retrieval failed", ex)
        Redirect(realReturnTo)
          .flashing("error" -> "OpenID authentication failed")
    }
  }

  def steamCallback(returnTo: String) = Action.async { implicit request =>
    SteamOpenID().verifiedId.map(Some.apply).recover { case ex: Throwable =>
        Logger.error(s"OpenID verification failed: ${request.uri}", ex)
        None
    }.map {
      case Some(openID) =>
        DB.withTransaction { implicit session =>
          val uid = Users.getOrRegisterIDByIdentity(Identity.Kind.STEAM, openID.id)

          Redirect(returnTo)
            .withUser(uid)
        }
      case None =>
        Redirect(returnTo)
          .flashing("error" -> "OpenID verification failed")
    }
  }
}

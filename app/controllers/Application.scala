package controllers

import actors.WSClientActor
import models.{ClientInfo, Game}
import utils._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller, Result, WebSocket}


object Application extends Controller {

  val clientInfoForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "games" -> seq(nonEmptyText.transform[Game](Game.apply, _.name))
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

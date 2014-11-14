package controllers

import models._
import play.api.libs.json._

object Customization extends PMController {
  def myGames = APIUserAction { rq =>
    implicit val (req, session, user) = rq
    val games = UserGames.byUser(user.id.get)
    Ok(JsArray(games.map {
      case (userGame, game) =>
        JsObject(Seq(
          "id" -> JsNumber(userGame.id.get.id),
          "enabled" -> JsBoolean(userGame.enabled),
          "game" -> game.toJson
        ))
    }))
  }

  def setEnabled(game: GameID, enable: Boolean) = APIUserAction { rq =>
    implicit val (req, session, user) = rq
    if (UserGames.setEnabled(game, user.id.get, enable)) {
      Ok(JsObject(Seq("status" -> JsString("ok"))))
    } else {
      NotFound(JsObject(Seq("error" -> JsString("not found"))))
    }
  }

  def setAllEnabled(enable: Boolean) = APIUserAction { rq =>
    implicit val (req, session, user) = rq
    UserGames.setAllEnabled(user.id.get, enable)
    Ok(JsObject(Seq("status" -> JsString("ok"))))
  }
}

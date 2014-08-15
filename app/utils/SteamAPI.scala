package utils

import play.api.{Logger, Application}
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import utils.SteamAPI.SteamGame

import scala.concurrent.Future

class SteamAPI(app: Application) {
  private val ws = WS.client(app)
  private val apiKey = app.configuration.getString("steam.key")
  private val urlBase = "http://api.steampowered.com"

  def withApiKey[T](f: String => Future[Seq[T]]): Future[Seq[T]] = {
    apiKey.map(f).getOrElse {
      Logger.warn("Tried to access Steam API, but no key was set in local.conf")
      Future(Seq())
    }
  }

  case class User(steamid: Long) {
    def games(): Future[Seq[SteamGame]] = withApiKey { apiKey =>
      val url = ws.url(s"$urlBase/IPlayerService/GetOwnedGames/v0001/").withQueryString(
        "key" -> apiKey,
        "steamid" -> steamid.toString,
        "include_appinfo" -> "1",
        "format" -> "json"
      )
      val rq = url.get()
      rq.map(_.json \ "response" \ "games").map(_.as[Seq[JsValue]].map(game => SteamGame(
        (game \ "appid").as[Long],
        (game \ "name").as[String],
        (game \ "img_icon_url").as[String]
      )))
    }
  }
}

object SteamAPI extends PerApplicationCompanion[SteamAPI] {
  override def create(app: Application): SteamAPI = new SteamAPI(app)

  case class SteamGame(appid: Long, name: String, icon: String) {
    def iconPath = s"http://media.steampowered.com/steamcommunity/public/images/apps/$appid/$icon.jpg"
  }
}

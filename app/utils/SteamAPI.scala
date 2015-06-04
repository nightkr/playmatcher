package utils

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws.WS
import play.api.{Application, Logger}
import utils.SteamAPI.{SteamGame, SteamPlayerSummary}

import scala.concurrent.Future

class SteamAPI(app: Application) {
  private val ws = WS.client(app)
  private val apiKey = app.configuration.getString("steam.key")
  private val urlBase = "http://api.steampowered.com"
  private val bigpicUrlBase = "http://store.steampowered.com/api"

  private def withApiKey[T](f: String => Future[Seq[T]]): Future[Seq[T]] = {
    apiKey.map(f).getOrElse {
      Logger.warn("Tried to access Steam API, but no key was set in local.conf")
      Future(Seq())
    }
  }

  private def bigpicGameDetails(appids: Seq[Long]): Future[Map[Long, JsObject]] = {
    val rq = ws.url(s"$bigpicUrlBase/appdetails/").withQueryString(
      "appids" -> appids.mkString(","),
      "filters" -> "categories"
    ).get()
    rq.map(_.json.as[Map[String, JsObject]]/*(Reads.seq[(String, JsObject)])*/.flatMap { case (appid, obj) =>
      if ((obj \ "success").as[Boolean])
        Some((appid.toLong, (obj \ "data").as[JsObject]))
      else None
    }.toMap)
  }

  case class User(steamid: Long) {
    def games(): Future[Seq[SteamGame]] = withApiKey { apiKey =>
      val rq = ws.url(s"$urlBase/IPlayerService/GetOwnedGames/v0001/").withQueryString(
        "key" -> apiKey,
        "steamid" -> steamid.toString,
        "include_appinfo" -> "1",
        "format" -> "json"
      ).get()
      val bigpicRq = ws.url(s"$bigpicUrlBase/appdetails/").withQueryString(

      )
      for {
        games <- rq.map(_.json \ "response" \ "games").map(_.as[Seq[JsValue]])
      } yield games.map(game => SteamGame(
        (game \ "appid").as[Long],
        (game \ "name").as[String],
        (game \ "img_icon_url").as[String],
        Seq()
      ))
      /*val req = .map(game => SteamGame(
        (game \ "appid").as[Long],
        (game \ "name").as[String],
        (game \ "img_icon_url").as[String]
      )))*/
    }

    def summary(): Future[Option[SteamPlayerSummary]] = withApiKey { apiKey =>
      val url = ws.url(s"$urlBase/ISteamUser/GetPlayerSummaries/v0002/").withQueryString(
        "key" -> apiKey,
        "steamids" -> steamid.toString
      )
      val rq = url.get()
      rq.map(_.json \ "response" \ "players").map(_.as[Seq[JsValue]].map(player => SteamPlayerSummary(
        (player \ "steamid").as[String].toLong,
        (player \ "personaname").as[String]
      )))
    }.map(_.headOption)
  }

}

object SteamAPI extends PerApplicationCompanion[SteamAPI] {
  override def create(app: Application): SteamAPI = new SteamAPI(app)

  case class SteamGame(appid: Long, name: String, icon: String, categories: Seq[(Int, String)]) {
    def iconPath = s"http://media.steampowered.com/steamcommunity/public/images/apps/$appid/$icon.jpg"
  }

  case class SteamPlayerSummary(steamid: Long, personaname: String)

}

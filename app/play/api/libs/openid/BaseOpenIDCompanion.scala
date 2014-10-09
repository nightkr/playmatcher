package play.api.libs.openid

import play.api.Application
import play.api.libs.ws.{WS, WSClient}
import utils.PerApplicationCompanion

import scala.concurrent.Future
import scala.util.DynamicVariable

class BaseOpenID(ws: WSClient) extends OpenIDClient(ws)

class SteamOpenID(ws: WSClient) extends BaseOpenID(ws) {
  override val discovery: SteamOpenID.SteamDiscovery = new SteamOpenID.SteamDiscovery(ws)

  override def redirectURL(openID: String, callbackURL: String, axRequired: Seq[(String, String)], axOptional: Seq[(String, String)], realm: Option[String]): Future[String] = {
    discovery.fakeNormalizeIdentifier.withValue(Some("http://specs.openid.net/auth/2.0/identifier_select")) {
      super.redirectURL(openID, callbackURL, axRequired, axOptional, realm)
    }
  }
}

object SteamOpenID extends PerApplicationCompanion[SteamOpenID] {

  override def create(app: Application) = new SteamOpenID(WS.client(app))

  class SteamDiscovery(ws: WSClient) extends Discovery(ws: WSClient) {
    val fakeNormalizeIdentifier = new DynamicVariable[Option[String]](None)

    override def normalizeIdentifier(openID: String): String = {
      super.normalizeIdentifier(fakeNormalizeIdentifier.value.getOrElse(openID))
    }

    override def discoverServer(openID: String): Future[OpenIDServer] = {
      fakeNormalizeIdentifier.withValue(None)(super.discoverServer(openID))
    }
  }

}
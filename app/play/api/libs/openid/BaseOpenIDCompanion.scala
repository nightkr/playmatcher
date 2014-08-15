package play.api.libs.openid

import play.api.{Plugin, Application}
import play.api.libs.ws.{WSClient, WS}
import utils.PerApplicationCompanion

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
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
  class SteamDiscovery(ws: WSClient) extends Discovery(ws: WSClient) {
    val fakeNormalizeIdentifier = new DynamicVariable[Option[String]](None)

    override def normalizeIdentifier(openID: String): String = {
      super.normalizeIdentifier(fakeNormalizeIdentifier.value.getOrElse(openID))
    }

    override def discoverServer(openID: String): Future[OpenIDServer] = {
      fakeNormalizeIdentifier.withValue(None)(super.discoverServer(openID))
    }
  }

  override def create(app: Application) = new SteamOpenID(WS.client(app))
}
package play.api.libs.openid

import play.api.{Plugin, Application}
import play.api.libs.ws.{WSClient, WS}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.runtime.universe.TypeTag
import scala.util.DynamicVariable

class BaseOpenID(ws: WSClient) extends OpenIDClient(ws)

/**
 * Hack to store a new OpenIDClient for each application, since the default OpenID implementation breaks when the current application is replaced, for example on reloads.
 * See https://github.com/playframework/playframework/issues/3277 for more info.
 */
trait BaseOpenIDCompanion[T] {
  def create(ws: WSClient): T
  
  private lazy val clients: TrieMap[Application, T] = TrieMap()

  def onApplicationStop(app: Application): Unit = {
    clients -= app
  }

  def apply()(implicit app: Application): T = {
    clients.getOrElseUpdate(app, {
      create(WS.client(app))
    })
  }
}

/**
 * Prevent memory leaks after application reloads
 */
class ReloadSafeOpenIDPlugin(implicit app: Application) extends Plugin {
  override def onStop(): Unit = {
    SteamOpenID.onApplicationStop(app)
    super.onStop()
  }
}

class SteamOpenID(ws: WSClient) extends BaseOpenID(ws) {
  override val discovery: SteamOpenID.SteamDiscovery = new SteamOpenID.SteamDiscovery(ws)

  override def redirectURL(openID: String, callbackURL: String, axRequired: Seq[(String, String)], axOptional: Seq[(String, String)], realm: Option[String]): Future[String] = {
    discovery.fakeNormalizeIdentifier.withValue(Some("http://specs.openid.net/auth/2.0/identifier_select")) {
      super.redirectURL(openID, callbackURL, axRequired, axOptional, realm)
    }
  }
}
object SteamOpenID extends BaseOpenIDCompanion[SteamOpenID] {
  class SteamDiscovery(ws: WSClient) extends Discovery(ws: WSClient) {
    val fakeNormalizeIdentifier = new DynamicVariable[Option[String]](None)

    override def normalizeIdentifier(openID: String): String = {
      super.normalizeIdentifier(fakeNormalizeIdentifier.value.getOrElse(openID))
    }

    override def discoverServer(openID: String): Future[OpenIDServer] = {
      fakeNormalizeIdentifier.withValue(None)(super.discoverServer(openID))
    }
  }

  override def create(ws: WSClient) = new SteamOpenID(ws)
}
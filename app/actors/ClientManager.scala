package actors

import actors.ClientManager.{MatchFound, Register}
import akka.actor._
import models.ClientInfo

import scala.collection.mutable

class ClientManager extends Actor with ActorLogging {
  val queue = mutable.Queue[(ActorRef, ClientInfo)]()

  override def receive: Receive = {
    case Register(info) =>
      val client = sender()
      queue.dequeueFirst({ case (otherRef, otherInfo) => infoMatches(info, otherInfo)}) match {
        case None =>
          context.watch(client)
          queue += ((client, info))
        case Some((otherRef, otherInfo)) =>
          client ! MatchFound(otherInfo)
          otherRef ! MatchFound(info)
      }

    case Terminated(client) =>
      queue.dequeueAll { case (ref, info) => ref == client}
  }

  def infoMatches(x: ClientInfo, y: ClientInfo): Boolean = {
    val sharedGames = x.games.intersect(y.games).length
    sharedGames > 0
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop() {
    super.postStop()

    for ((ref, _) <- queue) {
      ref ! PoisonPill
    }

    log.warning("ClientManager terminating")
  }
}

object ClientManager extends akka.actor.ExtensionId[ClientManagerExtensionImpl] with akka.actor.ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension] = ClientManager

  override def createExtension(system: ExtendedActorSystem): ClientManagerExtensionImpl = new ClientManagerExtensionImpl(system)

  case class Register(info: ClientInfo)

  case class MatchFound(other: ClientInfo)

}

class ClientManagerExtensionImpl(protected val system: ExtendedActorSystem) extends Extension {
  lazy val ref = system.actorOf(Props[ClientManager], "playmatcher-client-manager")
}

trait HasClientManager {
  this: Actor =>
  lazy val clientManager = ClientManager(context.system).ref
}



package actors

import actors.ClientManager.{MatchFound, Register}
import akka.actor._
import matchers.Matcher
import models.ClientInfo

import scala.collection.mutable

class ClientManager extends Actor with ActorLogging {
  val queue = mutable.Queue[(ActorRef, Matcher)]()

  override def receive: Receive = {
    case Register(info) =>
      val client = sender()
      val matcher = Matcher.default(info)

      queue.dequeueFirst({ case (otherRef, otherMatcher) => infoMatches(matcher, otherMatcher)}) match {
        case None =>
          context.watch(client)
          queue += client -> matcher
        case Some((otherRef, otherInfo)) =>
          client ! MatchFound(otherInfo.selfInfo)
          otherRef ! MatchFound(info)
      }

    case Terminated(client) =>
      queue.dequeueAll { case (ref, info) => ref == client}
  }

  def infoMatches(self: Matcher, other: Matcher): Boolean = {
    val selfScore = self.score(other.selfInfo)
    val otherScore = other.score(self.selfInfo)
    val score = (selfScore + otherScore) / 2
    score > 0
  }

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



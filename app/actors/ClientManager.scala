package actors

import actors.ClientManager.{Tick, MatchFound, Register}
import akka.actor._
import matchers.Matcher
import models.ClientInfo
import utils.Breakable

import scala.collection.mutable
import scala.concurrent.duration._

class ClientManager extends Actor with ActorLogging {
  import context._

  val queue = mutable.Queue[(ActorRef, Matcher)]()

  override def receive: Receive = {
    case Register(info) =>
      val client = sender()
      val matcher = Matcher.default(info)
      context.watch(client)
      queue += client -> matcher

    case Tick =>
      Breakable { break =>
        for (entry@(ref, matcher) <- queue) {
          queue.dequeueFirst({ case (otherRef, otherMatcher) => ref != otherRef && infoMatches(matcher, otherMatcher)}) match {
            case None =>
            case Some((otherRef, otherMatcher)) =>
              queue.dequeueFirst(_ eq entry)
              ref ! MatchFound(otherMatcher.selfInfo)
              otherRef ! MatchFound(matcher.selfInfo)
              break()
          }
        }
      }

    case Terminated(client) =>
      queue.dequeueAll { case (ref, info) => ref == client}
  }

  def infoMatches(selfMatcher: Matcher, otherMatcher: Matcher): Boolean = {
    val selfScore = selfMatcher.score(otherMatcher.selfInfo)
    val otherScore = otherMatcher.score(selfMatcher.selfInfo)
    val score = (selfScore + otherScore) / 2
    score > 0
  }

  override def preStart(): Unit = {
    super.preStart()

    system.scheduler.schedule(0.seconds, 1.second, self, Tick)
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
  case object Tick

  case class MatchFound(other: ClientInfo)

}

class ClientManagerExtensionImpl(protected val system: ExtendedActorSystem) extends Extension {
  lazy val ref = system.actorOf(Props[ClientManager], "playmatcher-client-manager")
}

trait HasClientManager {
  this: Actor =>
  lazy val clientManager = ClientManager(context.system).ref
}



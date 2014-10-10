package actors

import actors.ClientManager.{MatchFound, NewMatchThreshold, Register, Tick}
import akka.actor._
import matchers.Matcher
import models.ClientInfo
import utils.Breakable

import scala.collection.mutable
import scala.concurrent.duration._

class ClientManager extends Actor with ActorLogging {

  import context._

  val queue = mutable.Queue[(ActorRef, Matcher)]()
  val clientThresholds = mutable.WeakHashMap[ClientInfo, Double]()
  val scoreCache = mutable.WeakHashMap[Matcher, mutable.WeakHashMap[Matcher, Int]]()

  override def receive: Receive = {
    case Register(info) =>
      val client = sender()
      val matcher = Matcher.default(info)
      context.watch(client)
      queue += client -> matcher
      clientThresholds += info -> 100

    case Tick =>
      for ((actor, matcher) <- queue) {
        val threshold = clientThresholds(matcher.selfInfo) * 0.99
        clientThresholds(matcher.selfInfo) = threshold
        actor ! NewMatchThreshold(threshold)
      }

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
    val selfScore = calculateScore(selfMatcher, otherMatcher)
    val otherScore = calculateScore(otherMatcher, selfMatcher)
    selfScore >= clientThresholds(selfMatcher.selfInfo) && otherScore >= clientThresholds(otherMatcher.selfInfo)
  }

  def calculateScore(selfMatcher: Matcher, otherMatcher: Matcher): Int = {
    scoreCache
      .getOrElseUpdate(selfMatcher, mutable.WeakHashMap())
      .getOrElseUpdate(otherMatcher, selfMatcher.score(otherMatcher.selfInfo))
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

  sealed trait Event

  sealed trait Command

  case class Register(info: ClientInfo) extends Command

  case class MatchFound(other: ClientInfo) extends Event

  case class NewMatchThreshold(threshold: Double) extends Event

  private case object Tick

}

class ClientManagerExtensionImpl(protected val system: ExtendedActorSystem) extends Extension {
  lazy val ref = system.actorOf(Props[ClientManager], "playmatcher-client-manager")
}

trait HasClientManager {
  this: Actor =>
  lazy val clientManager = ClientManager(context.system).ref
}



package utils

import play.api.{Application, Plugin}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

trait PerApplicationCompanion[T] {
  def create(app: Application): T

  private lazy val clients: TrieMap[Application, T] = TrieMap()

  def onApplicationStop(app: Application): Unit = {
    clients -= app
  }

  def apply()(implicit app: Application): T = {
    clients.getOrElseUpdate(app, {
      app.plugin[PerApplicationCompanionCleanupPlugin].foreach(_.register(this))
      create(app)
    })
  }
}

/**
 * Prevent memory leaks after application reloads
 */
class PerApplicationCompanionCleanupPlugin(implicit app: Application) extends Plugin {
  private val objs = new mutable.WeakHashMap[PerApplicationCompanion[_], Unit]

  def register(obj: PerApplicationCompanion[_]): Unit = {
    synchronized {
      objs += obj -> Unit
    }
  }

  override def onStop(): Unit = {
    synchronized {
      for ((obj, _) <- objs) {
        obj.onApplicationStop(app)
      }
      objs.clear()
    }
    super.onStop()
  }
}
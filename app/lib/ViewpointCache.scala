/*
package lib

import net.openreview.model._
import net.openreview.model.users.User

import play.api.Play.current
import play.api.cache.Cache
import pov.{UserViewpoint, Viewpoint}
import raw.PublicStorage.world
import raw.{PublicStorage, World, StatefulEventProcessor, UserFetcher}
import scala.Some

/**
 * Viewpoint itself is in core, but this cache only makes sense in front
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 */
object ViewpointCache {

  import scala.collection.mutable.HashSet

  val keyset = HashSet[String]()

  /**
   * Needed only when the viewpoint should be updated after the request handler is called, 
   * e.g. because the user stores something to the db.
   * @param user
   * @param v
   */
  def update(user: Option[User], v: Viewpoint) {
    val key = user.map(_.id.toString).getOrElse("nouser")
    if (v.update) {
      keyset += key
      Cache.set(key, v)
    }
  }
  def clear() {
    for {k <- keyset} {
      Cache.remove(k)
    }
    keyset.clear()
  }

  PublicStorage.registerCacheClearCallback(clear)

  def withEmptyWorld(processor: StatefulEventProcessor): Viewpoint = {
    Cache.getOrElse[Viewpoint](processor.uuid.toString + "-noWorld") {
      val v = new UserViewpoint(Some(processor), World.empty)
      v.update
      v
    }
  }

  def apply(user: Option[User]): Viewpoint = {
    val key = user.map(_.id.toString).getOrElse("nouser")

    val vopt = Cache.getAs[Viewpoint](key)

    val v = vopt.getOrElse(new UserViewpoint(user.flatMap(u => UserFetcher(u.id)), world))

    if (v.update) {
      Cache.set(key, v)
      keyset += key
    }

    v
  }

}
*/

package org.photon.common.components

import com.twitter.util.Future
import scala.collection.{TraversableLike, mutable}

trait Service {
  def boot(): Future[Unit]
  def kill(): Future[Unit]
}

trait ServiceManagerComponent {
  def services: mutable.Builder[Service, _ <: TraversableLike[Service, _]]
}

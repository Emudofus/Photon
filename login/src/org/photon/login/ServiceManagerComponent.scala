package org.photon.login

import com.twitter.util.Future
import scala.collection.mutable

trait Service {
  def boot(): Future[Unit]
  def kill(): Future[Unit]
}

trait ServiceManagerComponent {
  def services: mutable.Builder[Service, _]
}

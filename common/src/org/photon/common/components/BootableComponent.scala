package org.photon.common.components

import com.twitter.util.{Await, Future}

trait BootableComponent extends ServiceManagerComponent {

  lazy val services = List.newBuilder[Service]

  def boot() {
    val s = services.result()

    Future.collect(s map {_.boot()}) onSuccess { _ =>
      sys.addShutdownHook {
        Await result Future.collect(s map {_.kill()})
      }
    }
  }

}

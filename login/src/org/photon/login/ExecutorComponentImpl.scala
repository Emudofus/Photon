package org.photon.login

import java.util.concurrent.{ExecutorService, Executors}

trait ExecutorComponentImpl extends ExecutorComponent {
  lazy val executor: ExecutorService = Executors.newCachedThreadPool
}

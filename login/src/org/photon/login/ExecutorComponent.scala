package org.photon.login

import java.util.concurrent.ExecutorService

trait ExecutorComponent {
  implicit val executor: ExecutorService
}

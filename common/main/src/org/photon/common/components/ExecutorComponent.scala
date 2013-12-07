package org.photon.common.components

import java.util.concurrent.ExecutorService

trait ExecutorComponent {
  implicit val executor: ExecutorService
}

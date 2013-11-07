package org.photon.common.components

import java.sql.Connection

trait DatabaseComponent {
  implicit val database: Connection
}

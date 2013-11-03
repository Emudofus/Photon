package org.photon.login

import java.sql.Connection

trait DatabaseComponent {
  implicit val database: Connection
}

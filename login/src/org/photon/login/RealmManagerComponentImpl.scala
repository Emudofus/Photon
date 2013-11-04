package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{Server, ServerState}

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent =>

  class RealmManagerImpl extends RealmManager {
    def onlineServers: Future[Seq[Server]] = Future(Seq(
      Server(1, ServerState.online, 0, joinable = true) // TODO
    ))
  }

  val realmManager = new RealmManagerImpl
}

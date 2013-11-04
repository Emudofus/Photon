package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{Server, ServerState}

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent =>

  class RealmManagerImpl extends RealmManager {
    def onlineServers: Future[Seq[Server]] = Future(Seq(
      Server(1, ServerState.online, 0, joinable = true) // TODO
    ))

    def playerList(user: User): Future[Seq[(Int, Int)]] = Future(Seq(1 -> 1))
  }

  val realmManager = new RealmManagerImpl
}

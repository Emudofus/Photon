package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{Server, ServerState}
import com.typesafe.scalalogging.slf4j.Logging

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent with ServiceManagerComponent =>

  class RealmManagerImpl extends RealmManager with Logging {
    def onlineServers: Future[Seq[Server]] = Future(Seq(
      Server(1, ServerState.online, 0, joinable = true) // TODO
    ))

    def playerList(user: User): Future[Seq[(Int, Int)]] = Future(Seq(1 -> 1))

    def find(serverId: Int): Option[RealmServer] = None

    def boot() = {
      logger.info("successfully booted")
      Future.Done
    }

    def kill() = {
      logger.info("successfully killed")
      Future.Done
    }
  }

  val realmManager = new RealmManagerImpl
  services += realmManager
}

package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{PlayersOfServer, Server, ServerState}
import com.typesafe.scalalogging.slf4j.Logging

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent with ServiceManagerComponent =>

  class RealmManagerImpl extends RealmManager with Logging {
    def onlineServers: Future[Seq[Server]] = Future(Seq(
      Server(1, ServerState.online, 0, joinable = true) // TODO
    ))

    def playerList(user: User): Future[Seq[PlayersOfServer]] = Future(Seq(PlayersOfServer(1, 1)))

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

  class RealmServerImpl(val address: String, val port: Int, var infosOption: Option[Server]) extends RealmServer {
    def infos = infosOption.get

    def grantAccess(user: User, ticket: String) = Future.exception(throw GrantAccessException())
  }

  val realmManager = new RealmManagerImpl
  services += realmManager
}

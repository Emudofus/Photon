package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{PlayersOfServer, Server}

trait RealmServer {
  def address: String
  def port: Int
  def infos: Server
  def isAvailable: Boolean

  def fetchPlayers(user: User): Future[PlayersOfServer]
  def grantAccess(user: User, ticket: String): Future[Unit]
}

trait RealmManager extends Service {
  def availableServers: Seq[Server]
  def playerList(user: User): Future[Seq[PlayersOfServer]]
  def find(serverId: Int): Option[RealmServer]
}

trait RealmManagerComponent { self: ConfigurationComponent =>
  sealed abstract class RealmAccessException extends RuntimeException
  case class GrantAccessException() extends RealmAccessException

  val realmManagerConfig = config.getConfig("photon.network.realm")
  val realmManagerPort = realmManagerConfig.getInt("port")

  val realmManager: RealmManager
}

package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.dofus.login.{PlayersOfServer, Server}
import org.photon.common.Observable
import org.photon.common.components.Service
import java.security.MessageDigest

case class PublicIdentity(address: String = "INVALID ADDRESS", port: Int = -1)
case class RealmAuthException(reason: String = "", nested: Throwable = null) extends RuntimeException(reason, nested)

trait RealmServer {
  def identity: PublicIdentity
  def infos: Server
  def isAvailable: Boolean

  def fetchPlayers(user: User): Future[PlayersOfServer]
  def grantAccess(user: User, ticket: String): Future[Unit]
}

trait RealmManager extends Service with Observable {
  emitted('updated)

  def availableServers: Seq[Server]
  def playerList(user: User): Future[Seq[PlayersOfServer]]
  def find(serverId: Int): Option[RealmServer]
}

trait RealmManagerComponent { self: ConfigurationComponent =>
  sealed abstract class RealmAccessException extends RuntimeException
  case class PlayerListException() extends RealmAccessException
  case class GrantAccessException() extends RealmAccessException

  val realmManagerConfig = config.getConfig("photon.network.realm")
  val realmManagerPort = realmManagerConfig.getInt("port")
  val realmManagerSaltLen = realmManagerConfig.getInt("salt-len")
  val realmManagerPasswordDigest = MessageDigest.getInstance(realmManagerConfig.getString("password-digest"))
  val realmManagerPassword = realmManagerPasswordDigest.digest(realmManagerConfig.getString("password").getBytes("UTF-8"))

  val realmManager: RealmManager
}

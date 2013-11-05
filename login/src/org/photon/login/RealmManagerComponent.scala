package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{PlayersOfServer, Server}

trait RealmServer {
  def address: String
  def port: Int
}

trait RealmManager extends Service {
  def onlineServers: Future[Seq[Server]]
  def playerList(user: User): Future[Seq[PlayersOfServer]]
  def find(serverId: Int): Option[RealmServer]
}

trait RealmManagerComponent { self: ConfigurationComponent =>

  val realmManagerConfig = config.getConfig("photon.network.realm")

  val realmManager: RealmManager
}

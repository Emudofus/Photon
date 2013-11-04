package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.Server

trait RealmManager {
  def onlineServers: Future[Seq[Server]]
  def playerList(user: User): Future[Seq[(Int, Int)]]
}

trait RealmManagerComponent { self: ConfigurationComponent =>

  val realmManagerConfig = config.getConfig("photon.network.realm")

  val realmManager: RealmManager
}

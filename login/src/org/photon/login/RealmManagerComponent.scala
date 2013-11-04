package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.Server

trait RealmManager {
  def onlineServers: Future[Seq[Server]]
}

trait RealmManagerComponent { self: ConfigurationComponent =>

  val realmManagerConfig = config.getConfig("photon.network.realm")

  val realmManager: RealmManager
}

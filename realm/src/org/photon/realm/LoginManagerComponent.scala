package org.photon.realm

import org.photon.common.components.Service
import org.photon.protocol.dofus.login.ServerState.ServerState
import com.twitter.util.Future
import java.security.MessageDigest

trait LoginManager extends Service {
  def updateState(state: ServerState): Future[Unit]
}

trait LoginManagerComponent {
  self: ConfigurationComponent =>

  val loginManagerConfig = config.getConfig("photon.network.login")
  val loginManagerPort = loginManagerConfig.getInt("port")
  val loginManagerPasswordDigest = MessageDigest.getInstance(loginManagerConfig.getString("password-digest"))
  val loginManagerPassword = loginManagerPasswordDigest.digest(loginManagerConfig.getString("password").getBytes("UTF-8"))

  val loginManager: LoginManager
}

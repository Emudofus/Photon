package org.photon.realm

import org.photon.common.components.Service
import org.photon.protocol.dofus.login.ServerState.ServerState
import com.twitter.util.Future
import java.security.MessageDigest

trait LoginManager extends Service {
  def updateState(state: ServerState): Future[Unit]
}

case class PublicIdentity(id: Int, completion: Int, address: String, port: Int)

trait LoginManagerComponent {
  self: ConfigurationComponent =>

  val loginManagerConfig = config.getConfig("photon.network.login")
  val loginManagerPort = loginManagerConfig.getInt("port")
  val loginManagerPasswordDigest = MessageDigest.getInstance(loginManagerConfig.getString("password-digest"))
  val loginManagerPassword = loginManagerPasswordDigest.digest(loginManagerConfig.getString("password").getBytes("UTF-8"))
  val loginManagerIdentity = {
    val tmp = loginManagerConfig.getConfig("identity")
    PublicIdentity(
      tmp.getInt("id"),
      tmp.getInt("completion"),
      tmp.getString("address"),
      tmp.getInt("port")
    )
  }

  val loginManager: LoginManager
}

package org.photon.realm

import org.photon.common.components.Service

trait LoginManager extends Service {

}

trait LoginManagerComponent {
  self: ConfigurationComponent =>

  val loginManagerConfig = config.getConfig("photon.network.login")
  val loginManagerPort = loginManagerConfig.getInt("port")

  val loginManager: LoginManager
}

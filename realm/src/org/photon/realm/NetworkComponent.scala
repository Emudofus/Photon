package org.photon.realm

import org.photon.common.{network => base}
import java.nio.charset.Charset

trait NetworkService extends base.NetworkService

trait NetworkSession extends base.NetworkSession

trait NetworkComponent {
  self: ConfigurationComponent =>

  val networkConfig = config.getConfig("photon.network.realm")
  val networkPort = networkConfig.getInt("port")
  val networkCharset = Charset.forName(networkConfig.getString("charset"))

  val networkService: NetworkService
}

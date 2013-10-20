package org.photon.login

import com.twitter.util.Future
import java.nio.charset.Charset
import com.typesafe.config.ConfigFactory

trait NetworkSession {
  type TFut = Future[this.type]
  type NetworkService <: org.photon.login.NetworkService

  def service: NetworkService
  def closeFuture: TFut

  def write(o: Any): TFut
  def flush(): TFut
  def close(): TFut

  def !(o: Any) = write(o) flatMap (_.flush())
}

trait NetworkService {
  type TFut = Future[this.type]
  type NetworkSession <: org.photon.login.NetworkSession

  def boot(): TFut
  def kill(): TFut
  def connected: Seq[NetworkSession]
}

trait NetworkComponent { self: ConfigurationComponent =>

  val networkConfig = config.atKey("photon.network.login")
  val networkPort = networkConfig.getInt("port")
  val networkCharset = Charset.forName(networkConfig.getString("charset"))
  val networkMaxFrameLength = networkConfig.getInt("maxFrameLength")

  val networkService: NetworkService

}
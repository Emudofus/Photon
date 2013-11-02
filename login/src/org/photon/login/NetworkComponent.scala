package org.photon.login

import com.twitter.util.{NonFatal, Promise, Future}
import java.nio.charset.Charset
import com.typesafe.config.ConfigFactory
import java.net.SocketAddress

trait NetworkSession {
  import NetworkSession._
  type NetworkService <: org.photon.login.NetworkService

  var state: State
  var userOption: Option[User]
  def user = userOption.get

  def service: NetworkService
  def closeFuture: Future[NetworkSession]
  def remoteAddress: SocketAddress
  def ticket: String

  def write(o: Any): Future[NetworkSession]
  def flush(): Future[NetworkSession]
  def close(): Future[NetworkSession]

  def !(o: Any) = write(o) flatMap (_.flush())

  def !!(o: Any) = (this ! o) flatMap (_.close())

  class Transaction private[NetworkSession]() {
    private[NetworkSession] var future: Future[NetworkSession] = _

    def write(o: Any) = {
      if (future != null) future = future flatMap (_.write(o))
      else future = NetworkSession.this.write(o)
      this
    }
  }

  def transaction[R](fn: Transaction => R): Future[NetworkSession] = {
    val t = new Transaction
    try {
      fn(t)
      t.future.flatMap(_.flush())
    } catch {
      case NonFatal(e) => Future.exception(e)
    }
  }
}

object NetworkSession {
  def write(o: Any)(implicit tr: NetworkSession#Transaction) = tr.write(o)

  sealed trait State
  case object VersionCheckState extends State
  case object AuthenticationState extends State
  case object ServerSelectionState extends State
}

trait NetworkService {
  type NetworkSession <: org.photon.login.NetworkSession

  def boot(): Future[NetworkService]
  def kill(): Future[NetworkService]
  def connected: Seq[NetworkSession]
}

trait NetworkComponent { self: ConfigurationComponent =>

  val networkConfig = config.getConfig("photon.network.login")

  val networkPort = networkConfig.getInt("port")
  val networkCharset = Charset.forName(networkConfig.getString("charset"))
  val networkMaxFrameLength = networkConfig.getInt("max-frame-length")

  val networkService: NetworkService

}
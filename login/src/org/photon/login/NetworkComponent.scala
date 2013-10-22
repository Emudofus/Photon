package org.photon.login

import com.twitter.util.{NonFatal, Promise, Future}
import java.nio.charset.Charset
import com.typesafe.config.ConfigFactory
import java.net.SocketAddress

trait NetworkSession {
  type NetworkService <: org.photon.login.NetworkService

  def service: NetworkService
  def closeFuture: Future[NetworkSession]
  def remoteAddress: SocketAddress

  def write(o: Any): Future[NetworkSession]
  def flush(): Future[NetworkSession]
  def close(): Future[NetworkSession]

  def !(o: Any) = write(o) flatMap (_.flush())

  class Transaction private[NetworkSession]() {
    private[NetworkSession] var future: Future[NetworkSession] = _

    def write(o: Any) = {
      if (future != null) future = future flatMap (_.write(o))
      else future = NetworkSession.this.write(o)
      this
    }

    def then(o: Any) = write(o)
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

  implicit class FutureNetworkSessionExtension(val future: Future[NetworkSession]) extends AnyVal {
    def thenClose() = future.flatMap(_.close())
  }
}

trait NetworkService {
  type NetworkSession <: org.photon.login.NetworkSession

  def boot(): Future[NetworkService]
  def kill(): Future[NetworkService]
  def connected: Seq[NetworkSession]
}

trait NetworkComponent { self: ConfigurationComponent =>

  val networkConfig = config.atKey("photon.network.login")
  val networkPort = networkConfig.getInt("port")
  val networkCharset = Charset.forName(networkConfig.getString("charset"))
  val networkMaxFrameLength = networkConfig.getInt("maxFrameLength")

  val networkService: NetworkService

}
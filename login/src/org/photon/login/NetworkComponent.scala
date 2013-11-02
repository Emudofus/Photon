package org.photon.login

import com.twitter.util.Future
import java.nio.charset.Charset
import java.net.SocketAddress
import scala.collection.mutable
import scala.annotation.tailrec

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

  type Transaction = mutable.Builder[Any, _]

  def transaction[R](fn: Transaction => R): Future[NetworkSession] = {
    val buf = List.newBuilder[Any]
    fn(buf)

    @tailrec
    def rec(fut: Future[NetworkSession], buf: List[Any]): Future[NetworkSession] = buf match {
      case head :: tail => rec(fut flatMap {_ write head}, tail)
      case Nil => fut
    }

    rec(Future(this), buf.result()) flatMap {_.flush()}
  }

  def transaction(msgs: Any*): Future[NetworkSession] = {

    @tailrec
    def rec(fut: Future[NetworkSession], l: List[Any]): Future[NetworkSession] = l match {
      case head :: tail => rec(fut flatMap {_.write(head)}, tail)
      case Nil => fut
    }

    rec(Future(this), msgs.toList) flatMap {_.flush()}
  }
}

object NetworkSession {
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
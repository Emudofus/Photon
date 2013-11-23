package org.photon.common.network

import com.twitter.util.Future
import java.net.SocketAddress
import scala.annotation.tailrec
import scala.collection.mutable
import org.photon.common.components.Service

trait NetworkSession {
  def service: NetworkService
  def closeFuture: Future[NetworkSession]
  def remoteAddress: SocketAddress

  def write(o: Any): Future[NetworkSession]
  def flush(): Future[NetworkSession]
  def close(): Future[NetworkSession]

  def !(o: Any) = write(o) flatMap (_.flush())

  def !!(o: Any) = (this ! o) flatMap (_.close())

  def transaction(msgs: Any*): Future[NetworkSession] = {

    @tailrec
    def rec(fut: Future[NetworkSession], l: List[Any]): Future[NetworkSession] = l match {
      case head :: tail => rec(fut flatMap {_.write(head)}, tail)
      case Nil => fut
    }

    rec(Future(this), msgs.toList) flatMap {_.flush()}
  }

  def transaction[R](fn: mutable.Builder[Any, _] => R): Future[NetworkSession] = {
    val buf = List.newBuilder[Any]
    fn(buf)
    transaction(buf.result(): _*)
  }
}

trait NetworkService extends Service {
  def connected: Seq[NetworkSession]
}
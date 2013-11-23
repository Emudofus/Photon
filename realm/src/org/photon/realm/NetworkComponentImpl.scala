package org.photon.realm

import com.twitter.util.Future
import scala.collection.mutable
import org.photon.common.Async
import org.apache.mina.core.session.IoSession
import org.apache.mina.transport.socket.nio.{NioProcessor, NioSocketAcceptor}
import org.photon.common.components.{ServiceManagerComponent, ExecutorComponent}
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.filter.util.WriteRequestFilter
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.write.WriteRequest
import java.net.InetSocketAddress
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.textline.TextLineCodecFactory
import org.apache.mina.core.service.IoHandlerAdapter
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import org.photon.protocol.dofus.{DofusMessage, DofusProtocol}
import org.photon.protocol.photon.UserInfos

trait NetworkComponentImpl extends NetworkComponent {
  self: ConfigurationComponent with ExecutorComponent with HandlerComponent with ServiceManagerComponent =>
  import MinaConversion._
  import scala.collection.JavaConversions._

  val networkService = new NetworkServiceImpl
  services += networkService

  private val logger = Logger(LoggerFactory getLogger classOf[NetworkComponentImpl])
  private val networkSessionAttributeKey = "photon.network.realm.session"

  class NetworkServiceImpl extends NetworkService {
    val connected = mutable.ArrayBuffer.empty[NetworkSession]
    val grantedUsers = mutable.Map.empty[String, UserInfos]

    val acceptor = new NioSocketAcceptor(executor, new NioProcessor(executor))
    acceptor.setDefaultLocalAddress(new InetSocketAddress(networkPort))
    acceptor.setHandler(new NetworkHandlerImpl)

    acceptor.getFilterChain.addLast("frame", new ProtocolCodecFilter(
      new TextLineCodecFactory(networkCharset, "\u0000", "\n\u0000")))
    acceptor.getFilterChain.addLast("codec", new NetworkCodecImpl)
    acceptor.getFilterChain.addLast("logging", new NetworkLoggingImpl)

    def boot() = Async {
      acceptor.bind()
      logger.debug(s"listening on $networkPort")
      logger.info("successfully booted")
    }

    def kill() = Async {
      acceptor.unbind()
      for (s <- acceptor.getManagedSessions.values()) s.close(true).await()
      acceptor.dispose()

      logger.info("successfully killed")
    }

    def grantUser(user: UserInfos, ticket: String) = Future {
      if (grantedUsers.exists { case (a, b) => a == ticket || b == user }) {
        throw GrantAccessException(reason = "ticket already taken or user already granted")
      }

      grantedUsers(ticket) = user
    }

    def auth(ticket: String) = Future {
      grantedUsers.remove(ticket)
        .getOrElse(throw AuthException(reason = s"unknown ticket $ticket"))
    }
  }

  class NetworkSessionImpl(underlying: IoSession) extends NetworkSession {
    var userOption: Option[UserInfos] = None

    def service = networkService
    def remoteAddress = underlying.getRemoteAddress
    val closeFuture = underlying.getCloseFuture.toTw(this)

    def flush() = Future(this)
    def write(o: Any) = underlying.write(o).toTw(this)
    def close() = {
      underlying.close(false)
      closeFuture
    }
  }

  class NetworkCodecImpl extends WriteRequestFilter {
    override def messageReceived(next: NextFilter, s: IoSession, o: Any) {
      DofusProtocol.deserialize(o.asInstanceOf[String]) match {
        case Some(m) => next.messageReceived(s, m)
        case None => logger.debug(s"cannot parse $o")
      }
    }

    def doFilterWrite(next: NextFilter, s: IoSession, req: WriteRequest) =
      DofusProtocol.serialize(req.getMessage match {
        case m: DofusMessage => List(m)
        case m: List[DofusMessage] => m
        case m: Seq[DofusMessage] => m.toList
      })
  }

  class NetworkLoggingImpl extends IoFilterAdapter {
    override def exceptionCaught(nextFilter: NextFilter, session: IoSession, cause: Throwable) {
      logger.error(s"session ${session.getRemoteAddress} has thrown an exception", cause)
      nextFilter.exceptionCaught(session, cause)
    }

    override def messageSent(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) {
      logger.debug(s"send ${writeRequest.getMessage} to ${session.getRemoteAddress}")
      nextFilter.messageSent(session, writeRequest)
    }

    override def messageReceived(nextFilter: NextFilter, session: IoSession, message: Any) {
      logger.debug(s"receive $message from ${session.getRemoteAddress}")
      nextFilter.messageReceived(session, message)
    }

    override def sessionClosed(nextFilter: NextFilter, session: IoSession) {
      logger.debug(s"session ${session.getRemoteAddress} is now disconnected")
      nextFilter.sessionClosed(session)
    }

    override def sessionOpened(nextFilter: NextFilter, session: IoSession) {
      logger.debug(s"session ${session.getRemoteAddress} is now connected")
      nextFilter.sessionOpened(session)
    }
  }

  class NetworkHandlerImpl extends IoHandlerAdapter {
    import HandlerComponent._

    override def sessionOpened(s: IoSession) {
      val session = new NetworkSessionImpl(s)
      s.setAttribute(networkSessionAttributeKey, session)

      networkHandler(Connect(session))
    }

    override def sessionClosed(s: IoSession) {
      val session = s.getAttribute(networkSessionAttributeKey).asInstanceOf[NetworkSessionImpl]

      networkHandler(Disconnect(session))
    }

    override def messageReceived(s: IoSession, o: Any) {
      val session = s.getAttribute(networkSessionAttributeKey).asInstanceOf[NetworkSessionImpl]

      val task = Message(session, o.asInstanceOf[DofusMessage])
      if (networkHandler.isDefinedAt(task)) {
        networkHandler(task)
      } else {
        logger.warn(s"no handler found for $o}")
      }
    }
  }
}

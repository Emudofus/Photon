package org.photon.login

import com.twitter.util.Future
import org.apache.mina.core.session.IoSession
import org.apache.mina.transport.socket.nio.{NioProcessor, NioSocketAcceptor}
import java.net.InetSocketAddress
import org.photon.common.{Observable, Strings, Async}
import scala.collection.mutable
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.filter.codec._
import org.apache.mina.filter.codec.textline.TextLineCodecFactory
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.write.WriteRequest
import org.photon.protocol.dofus.login.{AuthenticationMessage, VersionMessage}
import com.typesafe.scalalogging.slf4j.Logging
import org.apache.mina.filter.util.WriteRequestFilter
import java.util.Random
import org.photon.protocol.dofus.{DofusProtocol, DofusMessage}

trait NetworkComponentImpl extends NetworkComponent {
  self: ConfigurationComponent with ExecutorComponent with HandlerComponent with ServiceManagerComponent =>
  import MinaConversion._
  import scala.collection.JavaConversions._

  val networkService = new NetworkServiceImpl
  services += networkService

  private val networkSessionAttributeKey = "photon.network.login.session"

  class NetworkServiceImpl extends NetworkService with Logging {
    val acceptor = new NioSocketAcceptor(executor, new NioProcessor(executor))
    acceptor.setDefaultLocalAddress(new InetSocketAddress(networkPort))
    acceptor.setHandler(new NetworkHandlerImpl)

    acceptor.getFilterChain.addLast("frame", new ProtocolCodecFilter(
      new TextLineCodecFactory(networkCharset, "\u0000", "\n\u0000")))
    acceptor.getFilterChain.addLast("codec", new NetworkCodecImpl)
    acceptor.getFilterChain.addLast("logging", new NetworkLoggingImpl)

    val connected = mutable.ArrayBuffer.empty[NetworkSession]

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
  }

  class NetworkSessionImpl(val ticket: String, session: IoSession) extends NetworkSession {
    import NetworkSession._

    var state: State = VersionCheckState
    var realmUpdatedLid: Option[Observable.Lid] = None
    var userOption: Option[User] = None

    def service = networkService
    val closeFuture = session.getCloseFuture.toTw(this)
    val remoteAddress = session.getRemoteAddress

    def write(o: Any) = session.write(o).toTw(this)
    def flush() = Future(this)
    def close() = {
      session.close(false)
      closeFuture
    }

    override def !(o: Any) = write(o)
    override def transaction(msgs: Any*) = write(msgs.toSeq)
  }

  class NetworkCodecImpl extends WriteRequestFilter with Logging {
    import NetworkSession._

    override def messageReceived(nextFilter: NextFilter, s: IoSession, o: Any) {
      val session = s.getAttribute(networkSessionAttributeKey).asInstanceOf[NetworkSessionImpl]

      o match {
        case data: String =>
          val msg = session.state match {
            case VersionCheckState => VersionMessage.deserialize(data)
            case AuthenticationState => AuthenticationMessage.deserialize(data)
            case ServerSelectionState =>
              val (opcode, rest) = data.splitAt(2)
              DofusProtocol.deserializers.get(opcode).flatMap(_.deserialize(rest))
          }

          msg match {
            case None => logger.warn(s"cannot parse $data")
            case Some(m) => nextFilter.messageReceived(s, m)
          }
      }
    }

    def doFilterWrite(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) = {
      val builder = mutable.StringBuilder.newBuilder

      def serialize(msg: DofusMessage) {
        builder ++= msg.definition.opcode
        msg.serialize(builder)
      }

      writeRequest.getMessage match {
        case msgs: Seq[DofusMessage] =>
          for (msg <- msgs) {
            serialize(msg)
            builder += '\u0000'
          }
        case msg: DofusMessage =>
          serialize(msg)
      }

      val res = builder.result()
      logger.trace(s"send [${res.length}] $res to ${session.getRemoteAddress}")
      res
    }
  }

  class NetworkLoggingImpl extends IoFilterAdapter with Logging {
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

  class NetworkHandlerImpl extends IoHandlerAdapter with Logging {
    import HandlerComponent._

    implicit val random = new Random(System.nanoTime)

    override def sessionOpened(s: IoSession) {
      val session = new NetworkSessionImpl(Strings.next(32), s)
      s.setAttribute(networkSessionAttributeKey, session)

      networkHandler(Connect(session))
    }

    override def sessionClosed(s: IoSession) {
      val session = s.getAttribute(networkSessionAttributeKey).asInstanceOf[NetworkSessionImpl]

      networkHandler(Disconnect(session))
    }

    override def messageReceived(s: IoSession, o: Any) {
      val session = s.getAttribute(networkSessionAttributeKey).asInstanceOf[NetworkSessionImpl]

      val task = Message(session, o)
      if (networkHandler.isDefinedAt(task)) {
        networkHandler(task)
      } else {
        logger.warn(s"no handler found for $o}")
      }
    }
  }
}
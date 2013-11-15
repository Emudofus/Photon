package org.photon.realm

import org.photon.common.components.{ExecutorComponent, ServiceManagerComponent}
import org.apache.mina.transport.socket.nio.NioSocketConnector
import java.net.InetSocketAddress
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.photon.common.Async
import com.typesafe.scalalogging.slf4j.Logging
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.write.WriteRequest
import com.twitter.util.Future
import org.photon.protocol.photon._
import scala.Some

trait LoginManagerComponentImpl extends LoginManagerComponent {
  self: ConfigurationComponent with ServiceManagerComponent with ExecutorComponent =>
  import MinaConversion._

  val loginManager = new LoginManagerImpl
  services += loginManager

  class LoginManagerImpl extends LoginManager with Logging {
    val acceptor = new NioSocketConnector
    acceptor.setDefaultRemoteAddress(new InetSocketAddress(loginManagerPort))
    acceptor.setHandler(new LoginManagerHandlerImpl)

    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory))
    acceptor.getFilterChain.addLast("logging", new LoginManagerLoggingImpl)

    var sessionOption: Option[IoSession] = None
    def session = sessionOption.get

    def boot() = Async {
      sessionOption = Some(acceptor.connect().await().getSession)
      logger.debug(s"listening on $loginManagerPort")
      logger.info("successfully booted")
    }

    def kill() = Async {
      acceptor.dispose(true)
      logger.info("successfully killed")
    }
  }

  class LoginManagerLoggingImpl extends IoFilterAdapter with Logging {
    override def exceptionCaught(nextFilter: NextFilter, session: IoSession, cause: Throwable) {
      logger.error(s"unhandled exception", cause)
    }

    override def messageSent(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) {
      logger.debug(s"send ${writeRequest.getMessage}")
    }

    override def messageReceived(nextFilter: NextFilter, session: IoSession, message: Any) {
      logger.debug(s"receive $message")
    }

    override def sessionClosed(nextFilter: NextFilter, session: IoSession) {
      logger.debug("session closed")
    }

    override def sessionOpened(nextFilter: NextFilter, session: IoSession) {
      logger.debug("session opened")
    }
  }

  class LoginManagerHandlerImpl extends IoHandlerAdapter {
    override def messageReceived(session: IoSession, message: Any) {
      handle(Message(message))
    }

    override def sessionClosed(session: IoSession) {
      handle(Disconnect)
    }

    override def sessionOpened(session: IoSession) {
      handle(Connect)
    }
  }

  protected sealed trait Req
  protected case object Connect extends Req
  protected case object Disconnect extends Req
  protected case class Message(o: Any) extends Req
  protected type LoginManagerHandler = PartialFunction[Req, Future[_]]

  import loginManager.session
  protected def handle: LoginManagerHandler = {
    case Connect => Future.Done
    case Disconnect => Future.Done

    case Message(HelloConnectMessage(salt)) => ???
    case Message(AuthSuccessMessage) => ???
    case Message(AuthErrorMessage) => ???

    case Message(Ack) => Future.Done

    case Message(PlayerListMessage(userId)) =>
      session ! PlayerListSuccessMessage(userId, 1) // TODO give player list to login

    case Message(GrantAccessMessage(user, ticket)) =>
      session ! GrantAccessSuccessMessage(user.id) // TODO grant access
  }
}

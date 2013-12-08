package org.photon.realm

import org.photon.common.components.{ExecutorComponent, ServiceManagerComponent}
import org.apache.mina.transport.socket.nio.NioSocketConnector
import java.net.InetSocketAddress
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.photon.common.Async
import com.typesafe.scalalogging.slf4j.Logger
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.write.WriteRequest
import com.twitter.util.{NonFatal, Throw, Return, Future}
import org.photon.protocol.photon._
import scala.Some
import org.photon.protocol.dofus.login.{ServerState, Server}
import org.slf4j.LoggerFactory

trait LoginManagerComponentImpl extends LoginManagerComponent {
	self: ConfigurationComponent with ServiceManagerComponent with ExecutorComponent with PlayerRepositoryComponent with NetworkComponent =>
	import MinaConversion._

	val loginManager = new LoginManagerImpl
	services += loginManager

	private val logger = Logger(LoggerFactory getLogger classOf[LoginManagerComponentImpl])

	class LoginManagerImpl extends LoginManager {
		val acceptor = new NioSocketConnector
		acceptor.setDefaultRemoteAddress(new InetSocketAddress(loginManagerPort))
		acceptor.setHandler(new LoginManagerHandlerImpl)

		acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory))
		acceptor.getFilterChain.addLast("logging", new LoginManagerLoggingImpl)

		var sessionOption: Option[IoSession] = None
		def session = sessionOption.get

		def boot() = Async {
			sessionOption = Some(acceptor.connect().await().getSession)
			logger.info("successfully booted")
		}

		def kill() = Async {
			acceptor.dispose(true)
			logger.info("successfully killed")
		}

		def updateState(state: ServerState.ServerState) = (session ! StateUpdateMessage(state)).unit
	}

	class LoginManagerLoggingImpl extends IoFilterAdapter {
		override def exceptionCaught(nextFilter: NextFilter, session: IoSession, cause: Throwable) {
			logger.error(s"unhandled exception", cause)
			nextFilter.exceptionCaught(session, cause)
		}

		override def messageSent(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) {
			logger.debug(s"send ${writeRequest.getMessage}")
			nextFilter.messageSent(session, writeRequest)
		}

		override def messageReceived(nextFilter: NextFilter, session: IoSession, message: Any) {
			logger.debug(s"receive $message")
			nextFilter.messageReceived(session, message)
		}

		override def sessionClosed(nextFilter: NextFilter, session: IoSession) {
			logger.debug("session closed")
			nextFilter.sessionClosed(session)
		}

		override def sessionOpened(nextFilter: NextFilter, session: IoSession) {
			logger.debug("session opened")
			nextFilter.sessionOpened(session)
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

	def computePassword(salt: Array[Byte]): Array[Byte] = loginManagerPasswordDigest.digest(loginManagerPassword ++ salt)

	import loginManager.session
	protected def handle: LoginManagerHandler = {
		case Connect => Future.Done
		case Disconnect => Future.Done

		case Message(HelloConnectMessage(salt)) =>
			session ! AuthMessage(loginManagerIdentity.id, computePassword(salt), salt)

		case Message(AuthSuccessMessage) =>
			logger.info("successfully connected to login")

			session ! PublicIdentityMessage(loginManagerIdentity.address, loginManagerIdentity.port)
			session ! InfosUpdateMessage(Server(
				loginManagerIdentity.id,
				ServerState.online,
				loginManagerIdentity.completion,
				joinable = true
			))

		case Message(AuthErrorMessage) => ???

		case Message(Ack) => Future.Done

		case Message(PlayerListMessage(userId)) =>
			playerRepository.findByOwner(userId) transform {
				case Return(players) =>
					session ! PlayerListSuccessMessage(userId, players.size)

				case Throw(NonFatal(ex)) =>
					logger.error(s"can't get players list of $userId", ex)
					session ! PlayerListErrorMessage(userId)
			}

		case Message(GrantAccessMessage(user, ticket)) =>
			networkService.grantUser(user, ticket) transform {
				case Return(_) => session ! GrantAccessSuccessMessage(user.id)

				case Throw(GrantAccessException(reason, underlying)) =>
					logger.debug(s"can't grant access to ${user.id} because : $reason", underlying)
					session ! GrantAccessErrorMessage(user.id)
			}
	}
}

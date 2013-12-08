package org.photon.realm.handler

import org.photon.realm.{HandlerComponent, NetworkComponent, AuthException, BaseHandlerComponent}
import org.photon.protocol.dofus.account.{AuthMessage, AuthRequestMessage}
import com.twitter.util.{Throw, Return}
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger

trait AuthHandlerComponent extends BaseHandlerComponent {
	self: NetworkComponent =>
	import HandlerComponent._
	private val logger = Logger(LoggerFactory getLogger classOf[AuthHandlerComponent])

	override def networkHandler = super.networkHandler orElse
		(authHandler filter nonAuthenticated)

	def authHandler: NetworkHandler = {
		case Message(s, AuthRequestMessage(ticket)) =>
			networkService.auth(ticket) transform {
				case Return(user) =>
					s.userOption = Some(user)
					s ! AuthMessage(success = true)

				case Throw(AuthException(reason, underlying)) =>
					logger.debug(s"can't auth ${s.remoteAddress} because : $reason", underlying)
					s ! AuthMessage(success = false)
			}
	}
}

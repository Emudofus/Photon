package org.photon.realm

import org.photon.protocol.dofus.DofusMessage
import com.twitter.util.Future

object HandlerComponent {
	sealed trait Req {
		def session: NetworkSession
	}

	case class Connect(session: NetworkSession) extends Req
	case class Disconnect(session: NetworkSession) extends Req
	case class Message(session: NetworkSession, o: DofusMessage) extends Req

	type NetworkHandler = PartialFunction[Req, Future[_]]
	type Filter = Req => Future[Req]

	private def filter(fn: NetworkHandler, f: Filter): NetworkHandler = {
		case req if fn.isDefinedAt(req) =>
			f(req) flatMap { it => fn(it) }
	}

	private def then(fn: NetworkHandler, f: Filter): NetworkHandler = {
		case req if fn.isDefinedAt(req) =>
			f(req) flatMap { _ => f(req).unit }
	}

	implicit class RichNetworkHandler(val fn: NetworkHandler) extends AnyVal {
		def filter(f: Filter): NetworkHandler = HandlerComponent.filter(fn, f)
		def then(f: Filter): NetworkHandler = HandlerComponent.then(fn, f)
	}

	def nonAuthenticated(req: Req) = Future {
		if (req.session.userOption.nonEmpty)
			throw new IllegalStateException(s"session ${req.session.remoteAddress} must not be authenticated (user ${req.session.user.id})")

		req
	}

	def authenticated(req: Req) = Future {
		if (req.session.userOption.isEmpty)
			throw new IllegalStateException(s"session ${req.session.remoteAddress} must be authenticated")

		req
	}

	def playing(req: Req) = authenticated(req) map { req =>
		if (req.session.playerOption.isEmpty)
			throw new IllegalStateException(s"session ${req.session.remoteAddress} must be playing (user ${req.session.user.id})")

		req
	}

	def notPlaying(req: Req) = Future {
		if (req.session.playerOption.isDefined)
			throw new IllegalStateException(s"session ${req.session.remoteAddress} must not be playing")

		req
	}
}

trait HandlerComponent {
	import HandlerComponent._

	def networkHandler: NetworkHandler
}

trait BaseHandlerComponent extends HandlerComponent {
	import HandlerComponent._

	def networkHandler: NetworkHandler = PartialFunction.empty
}
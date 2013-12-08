package org.photon.realm

import org.photon.protocol.dofus.DofusMessage
import com.twitter.util.Future
import scala.annotation.tailrec

object HandlerComponent {
	sealed trait Req {
		def session: NetworkSession
	}

	case class Connect(session: NetworkSession) extends Req
	case class Disconnect(session: NetworkSession) extends Req
	case class Message(session: NetworkSession, o: DofusMessage) extends Req

	type NetworkHandler = PartialFunction[Req, Future[_]]
	type NetworkFilter = PartialFunction[Req, Future[Req]]

	val emptyFilter: NetworkFilter = PartialFunction.empty

	val authenticated: NetworkFilter = {
		case req if req.session.userOption.isDefined => Future(req)
		case req => Future exception new IllegalStateException(s"${req.session.remoteAddress} must be authenticated")
	}

	val nonAuthenticated: NetworkFilter = {
		case req if req.session.userOption.isEmpty => Future(req)
		case req => Future exception new IllegalStateException(s"${req.session.remoteAddress} must not be authenticated")
	}

	val playing: NetworkFilter = {
		case req if req.session.playerOption.isDefined => Future(req)
		case req => Future exception new IllegalStateException(s"${req.session.remoteAddress} must be playing")
	}

	val notPlaying: NetworkFilter = {
		case req if req.session.playerOption.isEmpty => Future(req)
		case req => Future exception new IllegalStateException(s"${req.session.remoteAddress} must not be playing")
	}

	def NetworkFilter_&&(self: NetworkFilter, other: NetworkFilter): NetworkFilter = {
		case req => self(req).flatMap(other)
	}

	implicit class RichNetworkFilter(val self: NetworkFilter) extends AnyVal {
		def &&(other: NetworkFilter): NetworkFilter = NetworkFilter_&&(self, other)
	}
}

trait HandlerComponent {
	import HandlerComponent._

	val networkHandler: NetworkHandler
}

trait BaseHandlerComponent extends HandlerComponent {
	import HandlerComponent._

	private[this] val networkHandlerBuilder = List.newBuilder[NetworkHandler]
	lazy val networkHandler: NetworkHandler = {
		@tailrec
		def rec(head: NetworkHandler, tail: List[NetworkHandler]): NetworkHandler = tail match {
			case h :: t => rec(head orElse h, t)
			case Nil => head
		}

		rec(PartialFunction.empty, networkHandlerBuilder.result())
	}

	def handle(filter: NetworkFilter = emptyFilter)(handler: NetworkHandler) {
		networkHandlerBuilder += (filter match {
			case `emptyFilter` => handler
			case _ => {
				case req if handler.isDefinedAt(req) =>
					filter(req) flatMap { r => handler(r) }
			}
		})
	}
}
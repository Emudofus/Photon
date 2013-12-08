package org.photon.login

import com.twitter.util.Future
import scala.annotation.tailrec

object HandlerComponent {
	sealed trait Req {
		def session: NetworkSession
	}
	case class Connect(session: NetworkSession) extends Req
	case class Disconnect(session: NetworkSession) extends Req
	case class Message(session: NetworkSession, o: Any) extends Req

	type NetworkHandler = PartialFunction[Req, Future[_]]
	type NetworkFilter = PartialFunction[Req, Future[Req]]


	def NetworkFilter_&&(self: NetworkFilter, other: NetworkFilter): NetworkFilter = {
		case req => self(req).flatMap(other)
	}

	implicit class RichNetworkFilter(val self: NetworkFilter) extends AnyVal {
		def &&(other: NetworkFilter): NetworkFilter = NetworkFilter_&&(self, other)
	}

	val emptyFilter: NetworkFilter = PartialFunction.empty

	val authenticated: NetworkFilter = {
		case req if req.session.userOption.isDefined => Future(req)
		case req => Future exception new IllegalStateException(s"session ${req.session.remoteAddress} must be authenticated")
	}
}

trait HandlerComponent {
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

	def when(filter: NetworkFilter)(handler: NetworkHandler) {
		networkHandlerBuilder += (filter match {
			case `emptyFilter` => handler
			case _ => {
				case req if handler.isDefinedAt(req) => filter(req) flatMap { _ => handler(req) }
			}
		})
	}

	def handle(handler: NetworkHandler) {
		when(emptyFilter)(handler)
	}
}

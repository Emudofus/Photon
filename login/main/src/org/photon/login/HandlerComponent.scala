package org.photon.login

import com.twitter.util.Future

object HandlerComponent {
	sealed trait Req
	case class Connect(session: NetworkSession) extends Req
	case class Disconnect(session: NetworkSession) extends Req
	case class Message(session: NetworkSession, o: Any) extends Req

	type NetworkHandler = PartialFunction[Req, Future[_]]
}

trait HandlerComponent {
	import HandlerComponent._
	val networkHandler: NetworkHandler
}

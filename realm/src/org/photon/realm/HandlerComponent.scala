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

  def filter(fn: NetworkHandler, f: Filter): NetworkHandler = {
    case req if fn.isDefinedAt(req) =>
      f(req) flatMap { it => fn(it) }
  }

  implicit class RichNetworkHandler(val fn: NetworkHandler) extends AnyVal {
    def %>(f: Filter): NetworkHandler = filter(fn, f)
  }

  def non_authenticated(req: Req) = Future {
    if (req.session.userOption.nonEmpty)
      throw new IllegalStateException(s"session ${req.session.remoteAddress} must not be authenticated (user ${req.session.user.id})")

    req
  }

  def authenticated(req: Req) = Future {
    if (req.session.userOption.isEmpty)
      throw new IllegalStateException(s"session ${req.session.remoteAddress} must be authenticated")

    req
  }
}

trait HandlerComponent {
  import HandlerComponent._

  val networkHandler: NetworkHandler
}

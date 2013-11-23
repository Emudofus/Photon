package org.photon.realm

import org.photon.protocol.dofus.DofusMessage
import com.twitter.util.Future

object HandlerComponent {
  type NetworkHandler = PartialFunction[Req, Future[_]]

  sealed trait Req
  case class Connect(s: NetworkSession) extends Req
  case class Disconnect(s: NetworkSession) extends Req
  case class Message(s: NetworkSession, o: DofusMessage) extends Req
}

trait HandlerComponent {
  import HandlerComponent._

  val networkHandler: NetworkHandler
}

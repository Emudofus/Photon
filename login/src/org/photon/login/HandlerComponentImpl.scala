package org.photon.login

import org.photon.protocol.DofusProtocol
import org.photon.protocol.login._
import com.typesafe.scalalogging.slf4j.Logging

trait HandlerComponentImpl extends HandlerComponent with Logging { self: UserAuthenticationComponent =>
  import HandlerComponent._
  import org.photon.login.NetworkSession._

  def newTicket(): String = ???

  val connections: NetworkHandler = {
    case Connect(s) =>
      s ! HelloConnectMessage(newTicket())

    case Disconnect(s) =>
  }


  val versionHandler: NetworkHandler = {
    case Message(s, VersionMessage(DofusProtocol.version)) => // valid version, do nothing
    case Message(s, VersionMessage(invalid)) =>
      logger.warn(s"client ${s.remoteAddress} had a invalid client version $invalid")

      (s ! InvalidVersionMessage).thenClose()
  }


  val authHandler: NetworkHandler = {
    case Message(s, AuthenticationMessage(username, password)) =>
      authenticate(s, username, password) onSuccess { _ =>
        s transaction { implicit t =>
          write(???)
        }

      } onFailure {
        case BannedUserException() =>       (s ! BannedUserMessage).thenClose()
        case AlreadyConnectedException() => (s ! AlreadyConnectedMessage).thenClose()
        case AccessDeniedException() =>     (s ! AccessDeniedMessage).thenClose()
      }
  }


  val realmHandler: NetworkHandler = {
    case Message(s, QueueStatusRequest) =>
  }



  val networkHandler = connections orElse versionHandler orElse authHandler orElse realmHandler
}

package org.photon.login

import org.photon.protocol.DofusProtocol
import org.photon.protocol.login._
import com.typesafe.scalalogging.slf4j.Logging
import org.photon.common.Strings

trait HandlerComponentImpl extends HandlerComponent with Logging { self: UserAuthenticationComponent =>
  import HandlerComponent._
  import org.photon.login.NetworkSession._

  val connections: NetworkHandler = {
    case Connect(s) =>
      s ! HelloConnectMessage(s.ticket)

    case Disconnect(s) =>
  }


  val versionHandler: NetworkHandler = {
    case Message(s, VersionMessage(DofusProtocol.version)) =>
      s.state = AuthenticationState

    case Message(s, VersionMessage(invalid)) =>
      logger.warn(s"client ${s.remoteAddress} had a invalid client version $invalid")

      s !! InvalidVersionMessage
  }


  val authHandler: NetworkHandler = {
    case Message(s, AuthenticationMessage(username, password)) =>
      require(s.userOption.isEmpty, s"expected a non-logged client ${s.remoteAddress}")

      authenticate(s, username, password) onSuccess { user =>
        s.state = ServerSelectionState
        s.userOption = Some(user)

        s transaction { implicit t =>
          write(???)
        }

      } onFailure {
        case BannedUserException() =>       s !! BannedUserMessage
        case AlreadyConnectedException() => s !! AlreadyConnectedMessage
        case AccessDeniedException() =>     s !! AccessDeniedMessage
      }
  }


  val realmHandler: NetworkHandler = {
    case Message(s, QueueStatusRequest) =>
  }



  val networkHandler = connections orElse versionHandler orElse authHandler orElse realmHandler
}

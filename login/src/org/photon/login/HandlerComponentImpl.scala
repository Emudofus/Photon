package org.photon.login

import org.photon.protocol.DofusProtocol
import org.photon.protocol.login._
import com.typesafe.scalalogging.slf4j.Logging
import com.twitter.util.{Future, Throw, Return}

trait HandlerComponentImpl extends HandlerComponent with Logging {
  self: UserAuthenticationComponent with RealmManagerComponent =>

  import HandlerComponent._
  import org.photon.login.NetworkSession._

  def connections: NetworkHandler = {
    case Connect(s) =>
      s ! HelloConnectMessage(s.ticket)

    case Disconnect(s) => Future.Done
  }


  def versionHandler: NetworkHandler = {
    case Message(s, VersionMessage(DofusProtocol.version)) =>
      s.state = AuthenticationState
      Future.Done

    case Message(s, VersionMessage(invalid)) =>
      logger.warn(s"client ${s.remoteAddress} had a invalid client version $invalid")

      s !! InvalidVersionMessage
  }


  def authHandler: NetworkHandler = {
    case Message(s, AuthenticationMessage(username, password)) =>
      require(s.userOption.isEmpty, s"expected a non-logged client ${s.remoteAddress}")

      authenticate(s, username, password) transform {
        case Return(user) =>
          s.state = ServerSelectionState
          s.userOption = Some(user)

          s transaction (
            SetNicknameMessage(user.nickname),
            SetCommunityMessage(user.communityId),
            SetSecretQuestion(user.secretQuestion),
            AuthenticationSuccessMessage(hasRights = false)
          )

        case Throw(BannedUserException()) =>        s !! BannedUserMessage
        case Throw(AlreadyConnectedException()) =>  s !! AlreadyConnectedMessage
        case Throw(AccessDeniedException()) =>      s !! AccessDeniedMessage

        case Throw(ex) =>
          logger.error(s"can't authenticate ${s.remoteAddress}", ex)
          Future.exception(ex)
      }
  }


  def realmHandler: NetworkHandler = {
    case Message(s, QueueStatusRequestMessage) => Future.Done

    case Message(s, ServerListRequestMessage) =>
      realmManager.onlineServers
        .map(ServerListMessage(_))
        .flatMap(s ! _)
  }



  val networkHandler = connections orElse versionHandler orElse authHandler orElse realmHandler
}

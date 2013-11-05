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

          realmManager.onlineServers flatMap {
            servers => s.transaction(
              SetNicknameMessage(user.nickname),
              SetCommunityMessage(user.communityId),
              SetSecretQuestion(user.secretQuestion),
              AuthenticationSuccessMessage(hasRights = false),
              ServerListMessage(servers)
            )
          }

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

    case Message(s, PlayerListRequestMessage) => realmManager.playerList(s.user) flatMap {
      players => s ! PlayerListMessage(s.user.subscriptionEnd, players)
    }

    case Message(s, ServerSelectionRequestMessage(serverId)) => realmManager.find(serverId) match {
      case Some(realm) => realm.grantAccess(s.user, s.ticket) transform {
        case Return(_) => s ! ServerSelectionMessage(realm.address, realm.port, s.ticket)

        case Throw(GrantAccessException()) =>
          logger.error(s"${s.remoteAddress} was not allowed to switch to ${realm.infos.id}")
          s ! ServerSelectionErrorMessage

        case Throw(ex) =>
          logger.error(s"${s.remoteAddress} can not switch to ${realm.infos.id}", ex)
          Future.exception(ex)
      }

      case None => s !! ServerSelectionErrorMessage
    }
  }



  val networkHandler = connections orElse versionHandler orElse authHandler orElse realmHandler
}

package org.photon.realm

import org.photon.protocol.dofus.account._
import com.twitter.util.{Return, Throw, Future}
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import scala.Some
import org.photon.protocol.dofus.login.QueueStatusRequestMessage

trait HandlerComponentImpl extends HandlerComponent {
  self: NetworkComponent with ConfigurationComponent with PlayerRepositoryComponent =>
  import HandlerComponent._

  private val logger = Logger(LoggerFactory getLogger classOf[HandlerComponentImpl])
  private val communityId = config.getInt("photon.realm.community")

  val networkHandler =
    connections orElse
    (authHandler filter nonAuthenticated) orElse
    (playerSelectionHandler filter authenticated)

  def connections: NetworkHandler = {
    case Connect(s) => s ! HelloGameMessage
    case Disconnect(s) => Future.Done
  }

  def authHandler: NetworkHandler = {
    case Message(s, AuthRequestMessage(ticket)) =>
      networkService.auth(ticket) transform {
        case Return(user) =>
          s.userOption = Some(user)
          s ! AuthMessage(success = true)

        case Throw(AuthException(reason, underlying)) =>
          logger.debug(s"can't auth ${s.remoteAddress} because : $reason", underlying)
          s ! AuthMessage(success = false)
      }
  }
  
  def playerSelectionHandler: NetworkHandler = {
    case Message(s, QueueStatusRequestMessage) => Future.Done // TODO queue
    case Message(s, GiftListRequestMessage(locale)) => Future.Done // TODO gifts
    case Message(s, IdentityMessage(identity)) => Future.Done // useless

    case Message(s, RegionalVersionRequestMessage) =>
      s ! RegionalVersionMessage(communityId)

    case Message(s, PlayerListRequestMessage) => playerRepository.findByOwner(s.user.id) flatMap {
      case players =>
        s ! PlayerListMessage(s.user.subscriptionEnd, players.toStream map { _.toPlayerTemplate })
    }
  }
}

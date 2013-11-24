package org.photon.realm.handler

import org.photon.realm._
import com.twitter.util.{Throw, Return, Future}
import org.photon.protocol.dofus.account._
import org.photon.protocol.dofus.login.QueueStatusRequestMessage
import com.twitter.util.Throw

trait PlayerSelectionComponent extends BaseHandlerComponent {
  self: ConfigurationComponent with PlayerRepositoryComponent =>
  import HandlerComponent._

  private val communityId = config.getInt("photon.realm.community")

  override def networkHandler = super.networkHandler orElse
    (playerSelectionHandler filter authenticated)

  def playerSelectionHandler: NetworkHandler = {
    case Message(s, QueueStatusRequestMessage) => Future.Done // TODO queue
    case Message(s, GiftListRequestMessage(locale)) => Future.Done // TODO gifts
    case Message(s, IdentityMessage(identity)) => Future.Done // useless

    case Message(s, RegionalVersionRequestMessage) =>
      s ! RegionalVersionMessage(communityId)

    case Message(s, PlayerListRequestMessage) => playerRepository.findByOwner(s.user.id) flatMap { players =>
      s ! PlayerListMessage(s.user.subscriptionEnd, players.toStream map { _.toPlayerTemplate })
    }

    case Message(s, RandomPlayerNameRequestMessage) =>
      s ! RandomPlayerNameMessage(name = "Photon")

    case Message(s, PlayerCreationRequestMessage(name, breed, gender, color1, color2, color3)) =>
      playerRepository.create(s.user.id, name, breed.toShort, gender, color1, color2, color3)
        .join(playerRepository findByOwner s.user.id)
        .transform {
          case Return( (player, players) ) => s ! (
            PlayerCreationSuccessMessage,
            PlayerListMessage(s.user.subscriptionEnd, players.toStream map { _.toPlayerTemplate })
          )

          case Throw(SubscriptionOutException()) => s ! SubscriptionOutCreationMessage
          case Throw(UnavailableSpaceException()) => s ! UnavailableSpaceCreationMessage
          case Throw(ExistingPlayerNameException()) => s ! ExistingPlayerNameCreationMessage
          case Throw(BadPlayerNameException()) => s ! BadPlayerNameCreationMessage
        }
  }
}

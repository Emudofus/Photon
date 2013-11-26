package org.photon.realm.handler

import org.photon.realm._
import com.twitter.util.{Throw, Return, Future}
import org.photon.protocol.dofus.account._
import org.photon.protocol.dofus.login.QueueStatusRequestMessage

trait PlayerSelectionComponent extends BaseHandlerComponent {
  self: ConfigurationComponent with PlayerRepositoryComponent =>
  import HandlerComponent._

  private val communityId = config.getInt("photon.realm.community")
  private val secretAnswerSinceLevel = config.getInt("photon.realm.secret-answer-since-level")

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
      val a = playerRepository findByOwner s.user.id
      val b = playerRepository.create(s.user.id, name, breed.toShort, gender, color1, color2, color3)

      (a join b) transform {
        case Return( (players, player) ) => s transaction (
          PlayerCreationSuccessMessage,
          PlayerListMessage(s.user.subscriptionEnd, (players :+ player).toStream.map(_.toPlayerTemplate))
        )

        case Throw(SubscriptionOutException()) =>     s ! SubscriptionOutCreationMessage
        case Throw(UnavailableSpaceException()) =>    s ! UnavailableSpaceCreationMessage
        case Throw(ExistingPlayerNameException()) =>  s ! ExistingPlayerNameCreationMessage
        case Throw(BadPlayerNameException()) =>       s ! BadPlayerNameCreationMessage
      }

    case Message(s, PlayerDeletionRequestMessage(playerId, secretAnswer)) => playerRepository.find(playerId) flatMap { player =>
      if (player.level >= secretAnswerSinceLevel && s.user.secretAnswer != secretAnswer) {
        s ! PlayerDeletionErrorMessage
      } else {
        playerRepository.remove(player) transform {
          case Return(_) => playerRepository.findByOwner(s.user.id) flatMap { players =>
            s transaction (
              PlayerDeletionSuccessMessage,
              PlayerListMessage(s.user.subscriptionEnd, players.toStream.map(_.toPlayerTemplate))
            )
          }
          case Throw(_) => s ! PlayerDeletionErrorMessage
        }
      }
    }

    case Message(s, PlayerSelectionRequestMessage(playerId)) => ???
  }
}

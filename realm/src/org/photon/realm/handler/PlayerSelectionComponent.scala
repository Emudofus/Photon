package org.photon.realm.handler

import org.photon.realm.{HandlerComponent, PlayerRepositoryComponent, ConfigurationComponent, BaseHandlerComponent}
import com.twitter.util.Future
import org.photon.protocol.dofus.account._
import org.photon.protocol.dofus.login.QueueStatusRequestMessage

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

    case Message(s, PlayerListRequestMessage) => playerRepository.findByOwner(s.user.id) flatMap {
      case players =>
        s ! PlayerListMessage(s.user.subscriptionEnd, players.toStream map { _.toPlayerTemplate })
    }
  }
}

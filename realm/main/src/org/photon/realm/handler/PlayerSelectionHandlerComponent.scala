package org.photon.realm.handler

import org.photon.realm._
import com.twitter.util.{Throw, Return, Future}
import org.photon.protocol.dofus.account._
import org.photon.protocol.dofus.chat.{SystemChatMessage, UpdateChannelListMessage}
import org.photon.protocol.dofus.spells.SpellListMessage
import org.photon.protocol.dofus.emotes.EmoteListMessage
import org.photon.protocol.dofus.items.UpdateWeightMessage
import org.photon.protocol.dofus.friends.ToggleConnectionListenerMessage
import org.photon.protocol.dofus.infos.{CurrentAddressInfo, WelcomeInfo, InfoMessage}

trait PlayerSelectionHandlerComponent extends BaseHandlerComponent {
	self: ConfigurationComponent with PlayerRepositoryComponent =>
	import HandlerComponent._

	private val communityId = config.getInt("photon.realm.community")
	private val secretAnswerSinceLevel = config.getInt("photon.realm.secret-answer-since-level")
	private val motd = config.getString("photon.realm.motd")

	when(authenticated && notPlaying) {
		case Message(s, GiftListRequestMessage(locale)) => Future.Done // TODO gifts

		case Message(s, IdentityMessage(identity)) => Future.Done // useless

		case Message(s, RegionalVersionRequestMessage) =>
			s ! RegionalVersionMessage(communityId)
	}

	when(authenticated && notPlaying) {

		// READ
		case Message(s, PlayerListRequestMessage) => playerRepository.findByOwner(s.user.id) flatMap { players =>
			s ! PlayerListMessage(s.user.subscriptionEnd, players.toStream map { _.toPlayerTemplate })
		}

		// CREATE
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

		// DELETE
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

		// SELECTION
		case Message(s, PlayerSelectionRequestMessage(playerId)) => playerRepository.find(playerId) transform {
			case Return(player) =>
				s.playerOption = Some(player)

				s transaction (
					PlayerSelectionSuccessMessage(
						player.id,
						player.name,
						player.level,
						player.breed,
						player.gender,
						player.appearence.skin,
						player.appearence.colors.first,
						player.appearence.colors.second,
						player.appearence.colors.third
					),
					UpdateChannelListMessage(Seq.empty, add = true), // TODO channels
					SpellListMessage(Seq.empty), // TODO spells
					EmoteListMessage(Seq.empty), // TODO emotes
					UpdateWeightMessage(current = 0, max = 0), // TODO items
					ToggleConnectionListenerMessage(enable = false), // TODO friends
					InfoMessage(WelcomeInfo),
					InfoMessage(CurrentAddressInfo(s.remoteAddress.toString)),
					SystemChatMessage(motd)
				)
			case Throw(_) => s ! PlayerSelectionErrorMessage
		}
	}
}

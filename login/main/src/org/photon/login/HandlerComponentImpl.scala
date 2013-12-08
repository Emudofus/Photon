package org.photon.login

import org.photon.protocol.dofus.login._
import com.typesafe.scalalogging.slf4j.Logger
import com.twitter.util.{Future, Throw, Return}
import org.slf4j.LoggerFactory
import org.photon.protocol.dofus.DofusProtocol
import org.photon.common.Observable

trait HandlerComponentImpl extends HandlerComponent {
	self: UserAuthenticationComponent with RealmManagerComponent =>

	import HandlerComponent._
	import org.photon.login.NetworkSession._

	private val logger = Logger(LoggerFactory getLogger classOf[HandlerComponentImpl])

	handle {
		case Connect(s) =>
			s ! HelloConnectMessage(s.ticket)

		case Disconnect(s) =>
			s.realmUpdatedLid foreach (realmManager.unsubscribe('updated, _))
			s.realmUpdatedLid = None
			Future.Done
	}


	handle {
		case Message(s, VersionMessage(DofusProtocol.version)) =>
			s.state = AuthenticationState
			Future.Done

		case Message(s, VersionMessage(invalid)) =>
			logger.warn(s"client ${s.remoteAddress} had a invalid client version $invalid")

			s !! InvalidVersionMessage
	}


	handle {
		case Message(s, AuthenticationMessage(username, password)) =>
			require(s.userOption.isEmpty, s"expected a non-logged client ${s.remoteAddress}")

			authenticate(s, username, password) transform {
				case Return(user) =>
					s.state = ServerSelectionState
					s.userOption = Some(user)
					s.realmUpdatedLid = Some(realmManager.subscribe('updated, realmServerUpdated(s)))

					s.transaction(
						SetNicknameMessage(user.nickname),
						SetCommunityMessage(user.communityId),
						SetSecretQuestion(user.secretQuestion),
						AuthenticationSuccessMessage(hasRights = false),
						ServerListMessage(realmManager.availableServers)
					)

				case Throw(BannedUserException()) =>        s !! BannedUserMessage
				case Throw(AlreadyConnectedException()) =>  s !! AlreadyConnectedMessage
				case Throw(AccessDeniedException()) =>      s !! AccessDeniedMessage

				case Throw(ex) =>
					logger.error(s"can't authenticate ${s.remoteAddress}", ex)
					Future.exception(ex)
			}
	}


	when(authenticated) {
		case Message(s, QueueStatusRequestMessage) => Future.Done

		case Message(s, PlayerListRequestMessage) => realmManager.playerList(s.user) flatMap {
			players => s ! PlayerListMessage(s.user.subscriptionEnd, players)
		}

		case Message(s, ServerSelectionRequestMessage(serverId)) => realmManager.find(serverId) match {
			case Some(realm) => realm.grantAccess(s.user, s.ticket) transform {
				case Return(_) => s ! ServerSelectionMessage(realm.identity.address, realm.identity.port, s.ticket)

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


	def realmServerUpdated(s: NetworkSession): Observable.UnitListener = {
		case realm: RealmServer =>
			s ! ServerListMessage(Seq(realm.infos))
		case () =>
			realmManager.playerList(s.user) flatMap {
				case players => s ! (
					ServerListMessage(realmManager.availableServers),
					PlayerListMessage(s.user.subscriptionEnd, players)
					)
			}
	}
}

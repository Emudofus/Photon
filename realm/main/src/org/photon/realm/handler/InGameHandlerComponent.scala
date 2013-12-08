package org.photon.realm.handler

import org.photon.realm.BaseHandlerComponent
import org.photon.protocol.dofus.basics.{CurrentDateMessage, CurrentDateRequestMessage}
import com.twitter.util.{Future, Time}
import org.photon.protocol.dofus.game._
import org.photon.protocol.dofus.infos.ScreenResolutionMessage

trait InGameHandlerComponent extends BaseHandlerComponent {
	import org.photon.realm.HandlerComponent._

	when(playing) {
		case Message(s, CurrentDateRequestMessage) =>
			s ! CurrentDateMessage(Time.now)

		case Message(s, ScreenResolutionMessage(width, height, tpe)) => Future.Done // useless
	}

	when(playing) {
		case Message(s, GameContextCreationMessage(context)) if context == Context.Solo =>
			s transaction (
				GameContextCreationSuccessMessage(context),
				// TODO player's characteristics
				MapDataMessage(id = 1, date = "", key = "") // TODO maps
			)

		case Message(s, GameContextDescriptionMessage) => Future.Done
	}
}

package org.photon.realm.handler

import org.photon.realm.{HandlerComponent, BaseHandlerComponent}
import org.photon.protocol.dofus.login.QueueStatusRequestMessage
import com.twitter.util.Future

trait QueueHandlerComponent extends BaseHandlerComponent {
	import HandlerComponent._

	handle() {
		case Message(s, QueueStatusRequestMessage) => Future.Done // TODO queue
	}
}

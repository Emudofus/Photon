package org.photon.realm

import com.twitter.util.Future
import org.photon.protocol.dofus.account.HelloGameMessage

trait HandlerComponentImpl extends BaseHandlerComponent
	with handler.AuthHandlerComponent
	with handler.PlayerSelectionHandlerComponent
	with handler.QueueHandlerComponent
	with handler.InGameHandlerComponent
{
	self: NetworkComponent
		with ConfigurationComponent
		with PlayerRepositoryComponent =>

	import HandlerComponent._

	handle {
		case Connect(s) => s ! HelloGameMessage
		case Disconnect(s) => Future.Done
	}

}

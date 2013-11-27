package org.photon.realm

import com.twitter.util.Future
import org.photon.realm.handler.{PlayerSelectionHandlerComponent, AuthHandlerComponent}
import org.photon.protocol.dofus.account.HelloGameMessage

trait HandlerComponentImpl extends BaseHandlerComponent
  with AuthHandlerComponent
  with PlayerSelectionHandlerComponent
{
  self: NetworkComponent
    with ConfigurationComponent
    with PlayerRepositoryComponent =>

  import HandlerComponent._
  override def networkHandler = connections orElse super.networkHandler

  def connections: NetworkHandler = {
    case Connect(s) => s ! HelloGameMessage
    case Disconnect(s) => Future.Done
  }

}

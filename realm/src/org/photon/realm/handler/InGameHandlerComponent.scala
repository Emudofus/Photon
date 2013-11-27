package org.photon.realm.handler

import org.photon.realm.BaseHandlerComponent
import org.photon.protocol.dofus.basics.{CurrentDateMessage, CurrentDateRequestMessage}
import com.twitter.util.Time

trait InGameHandlerComponent extends BaseHandlerComponent {
  import org.photon.realm.HandlerComponent._

  override def networkHandler = super.networkHandler orElse
    (basics filter playing)

  def basics: NetworkHandler = {
    case Message(s, CurrentDateRequestMessage) =>
      s ! CurrentDateMessage(Time.now)


  }
}

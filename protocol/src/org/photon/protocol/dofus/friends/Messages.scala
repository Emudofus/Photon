package org.photon.protocol.dofus.friends

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer}
import com.twitter.util.Try

case class ToggleConnectionListenerMessage(enable: Boolean) extends DofusMessage {
  def definition = ToggleConnectionListenerMessage
  def serialize(out: Out) = out += (if (enable) '+' else '-')
}

object ToggleConnectionListenerMessage extends DofusDeserializer {
  val opcode = "FO"
  def deserialize(in: In) = Try(in.charAt(0) == '+').toOption.map(ToggleConnectionListenerMessage.apply)
}

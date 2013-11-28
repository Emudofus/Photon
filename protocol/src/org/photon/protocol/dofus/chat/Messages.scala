package org.photon.protocol.dofus.chat

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer}
import com.twitter.util.Try

case class UpdateChannelListMessage(channels: Seq[Channel], add: Boolean) extends DofusMessage {
  def definition = UpdateChannelListMessage
  def serialize(out: Out) {
    out += (if (add) '+' else '-') += '|'
    channels.addString(out)
  }
}

object UpdateChannelListMessage extends DofusDeserializer {
  val opcode = "cC"
  def deserialize(in: In) = Try {
    UpdateChannelListMessage(
      add = in.charAt(0) == '+',
      channels = in.substring(1)
    )
  }.toOption
}

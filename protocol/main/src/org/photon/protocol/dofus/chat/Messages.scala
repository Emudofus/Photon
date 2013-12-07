package org.photon.protocol.dofus.chat

import org.photon.protocol.dofus.{DofusStaticMessage, DofusMessage, DofusDeserializer}
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

case class PublicChatMessage(senderId: Long, senderName: String, message: String, channel: Channel) extends DofusMessage {
  def definition = PublicChatMessage
  def serialize(out: Out) {
    out append channel append '|'
    out append senderId append '|'
    out append senderName append '|'
    out append message
  }
}

object PublicChatMessage extends DofusDeserializer {
  val opcode = "cMK"
  def deserialize(in: In) = None
}

case class PrivateChatMessage(senderId: Long, senderName: String, message: String, copy: Boolean) extends DofusMessage {
  def definition = PrivateChatMessage
  def serialize(out: Out) {
    out append (if (copy) 'F' else 'T') append '|'
    out append senderId append '|'
    out append senderName append '|'
    out append message
  }
}

object PrivateChatMessage extends DofusDeserializer {
  val opcode = "cMK"
  def deserialize(in: In) = None
}

case object ChatErrorMessage extends DofusStaticMessage {
  val opcode = "cME"
  val data = "f"
}

case class SystemChatMessage(rawMessage: String) extends DofusMessage {
  def definition = SystemChatMessage
  def serialize(out: Out) = out ++= rawMessage
}

object SystemChatMessage extends DofusDeserializer {
  val opcode = "cs"
  def deserialize(in: In) = None
}
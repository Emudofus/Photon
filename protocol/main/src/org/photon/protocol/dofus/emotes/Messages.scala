package org.photon.protocol.dofus.emotes

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer}

case class EmoteListMessage(emotes: Seq[Emote], suffix: String = "0") extends DofusMessage {
  def definition = EmoteListMessage
  def serialize(out: Out) = out ++= emotes += '|' ++= suffix
}

object EmoteListMessage extends DofusDeserializer {
  val opcode = "eL"
  def deserialize(in: In) = in.split("|", 2) match {
    case Array(emotes, suffix) => Some(EmoteListMessage(emotes, suffix))
    case _ => None
  }
}
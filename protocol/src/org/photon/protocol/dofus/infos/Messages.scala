package org.photon.protocol.dofus.infos

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer}

case class InfoMessage(info: Info) extends DofusMessage {
  def definition = InfoMessage
  def serialize(out: Out) {
    out ++= info.id
    info.serialize(out)
  }
}

object InfoMessage extends DofusDeserializer {
  val opcode = "Im"
  def deserialize(in: In) = None
}

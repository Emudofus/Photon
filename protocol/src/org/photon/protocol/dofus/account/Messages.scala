package org.photon.protocol.dofus.account

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer, DofusStaticMessage}

case object HelloGameMessage extends DofusStaticMessage {
  val opcode = "HG"
  val data = ""
}

case class AuthRequestMessage(ticket: String) extends DofusMessage {
  def definition = AuthRequestMessage
  def serialize(out: Out) = out ++= ticket
}

object AuthRequestMessage extends DofusDeserializer {
  val opcode = "AT"
  def deserialize(in: In) = Some(AuthRequestMessage(in))
}

case class AuthMessage(success: Boolean) extends DofusMessage {
  def definition = AuthMessage
  def serialize(out: Out) = out += (if (success) 'K' else 'E')
}

object AuthMessage extends DofusDeserializer {
  val opcode = "AT"
  def deserialize(in: In) =
    if (in.length == 1)
      Some(AuthMessage(in.charAt(1) == 'K'))
    else
      None
}
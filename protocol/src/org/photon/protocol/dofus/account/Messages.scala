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

case object RegionalVersionRequestMessage extends DofusStaticMessage {
  val opcode = "AV"
  val data = ""
}

case class RegionalVersionMessage(community: Int) extends DofusMessage {
  def definition = RegionalVersionMessage
  def serialize(out: Out) = out append community
}

object RegionalVersionMessage extends DofusDeserializer {
  val opcode = "AV"
  def deserialize(in: In) = try {
    Some(RegionalVersionMessage(in.toInt))
  } catch {
    case _: NumberFormatException => None
  }
}

case class GiftListRequestMessage(locale: String) extends DofusMessage {
  def definition = GiftListRequestMessage
  def serialize(out: Out) = out ++= locale
}

object GiftListRequestMessage extends DofusDeserializer {
  val opcode = "Ag"
  def deserialize(in: In) = Some(GiftListRequestMessage(in))
}

case class IdentityMessage(identity: String) extends DofusMessage {
  def definition = IdentityMessage
  def serialize(out: Out) = out ++= identity
}

object IdentityMessage extends DofusDeserializer {
  val opcode = "Ai"
  def deserialize(in: In) = Some(IdentityMessage(in))
}

case object PlayerListRequestMessage extends DofusStaticMessage {
  val opcode = "AL"
  val data = ???
}
package org.photon.protocol.dofus.account

import org.photon.protocol.dofus._
import com.twitter.util.Time

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
  val data = ""
}

case class PlayerListMessage(subscriptionEnd: Time, players: Seq[Player]) extends DofusMessage {
  def definition = PlayerListMessage
  def serialize(out: Out) {
    out append subscriptionEnd.untilNow.inMilliseconds append '|'
    out append players.size append '|'
    players.serialize(out, sep = "|")
  }
}

object PlayerListMessage extends DofusDeserializer {
  val opcode = "ALK"
  def deserialize(in: In) = None
}

case object RandomPlayerNameRequestMessage extends DofusStaticMessage {
  val opcode = "AP"
  val data = ""
}

case class RandomPlayerNameMessage(name: String) extends DofusMessage {
  def definition = RandomPlayerNameMessage
  def serialize(out: Out) = out ++= name
}

object RandomPlayerNameMessage extends DofusDeserializer {
  val opcode = "APK"
  def deserialize(in: In) = Some(RandomPlayerNameMessage(in))
}

case class PlayerCreationRequestMessage(
  name: String,
  breed: Int,
  gender: Boolean,
  color1: Int,
  color2: Int,
  color3: Int
) extends DofusMessage {
  def definition = PlayerCreationRequestMessage
  def serialize(out: Out) {
    out append name append '|'
    out append breed append '|'
    out append btoi(gender) append '|'
    out append color1 append '|'
    out append color2 append '|'
    out append color3
  }
}

object PlayerCreationRequestMessage extends DofusDeserializer {
  val opcode = "AA"
  def deserialize(in: In) = in.split("\\|", 6) match {
    case Array(name, Int(breed), gender, Int(color1), Int(color2), Int(color3)) =>
      Some(PlayerCreationRequestMessage(name, breed, itob(gender), color1, color2, color3))

    case _ => None
  }
}

abstract class PlayerCreationErrorMessage(val reason: Char) extends DofusStaticMessage {
  val opcode = "AA"
  val data = "E" + reason
}

case object SubscriptionOutCreationMessage extends PlayerCreationErrorMessage('s')
case object UnavailableSpaceCreationMessage extends PlayerCreationErrorMessage('f')
case object ExistingPlayerNameCreationMessage extends PlayerCreationErrorMessage('a')
case object BadPlayerNameCreationMessage extends PlayerCreationErrorMessage('n')

case object PlayerCreationSuccessMessage extends DofusStaticMessage {
  val opcode = "AA"
  val data = "K"
}

case class PlayerSelectionRequestMessage(playerId: Long) extends DofusMessage {
  def definition = PlayerSelectionRequestMessage
  def serialize(out: Out) = out append playerId
}

object PlayerSelectionRequestMessage extends DofusDeserializer {
  val opcode = "AS"
  def deserialize(in: In) = in match {
    case Int(playerId) => Some(PlayerSelectionRequestMessage(playerId))
    case _ => None
  }
}

abstract class PlayerSelectionMessage(val data: String) extends DofusStaticMessage {
  val opcode = "AS"
}

case object PlayerSelectionSuccessMessage extends PlayerSelectionMessage("K")
case object PlayerSelectionErrorMessage extends PlayerSelectionMessage("E")

case class PlayerDeletionRequestMessage(playerId: Long, secretAnswer: String) extends DofusMessage {
  def definition = PlayerDeletionRequestMessage
  def serialize(out: Out) = out append (playerId) append (secretAnswer)
}

object PlayerDeletionRequestMessage extends DofusDeserializer {
  val opcode = "AD"
  def deserialize(in: In) = in.split("\\|", 2) match {
    case Array(Long(playerId), secretAnswer) =>
      Some(PlayerDeletionRequestMessage(playerId, secretAnswer))
    case _ =>
      None
  }
}

abstract class PlayerDeletionMessage(val data: String) extends DofusStaticMessage {
  val opcode = "AD"
}

case object PlayerDeletionSuccessMessage extends PlayerDeletionMessage("K")
case object PlayerDeletionErrorMessage extends PlayerDeletionMessage("E")
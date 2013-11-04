package org.photon.protocol.login

import org.photon.protocol._
import com.twitter.util.Time

case class VersionMessage(version: String) extends DofusMessage {
  def definition = VersionMessage

  def serialize(out: Out) {
    out ++= version
  }
}

object VersionMessage extends DofusDeserializer {
  def opcode = ""
  def deserialize(in: In) = Some(VersionMessage(in))
}

abstract class AuthenticationErrorMessage(error: String) extends DofusStaticMessage {
  def opcode = "Al"
  val data = "E" + error
}

case object InvalidVersionMessage extends AuthenticationErrorMessage("v" + DofusProtocol.version)
case object AccessDeniedMessage extends AuthenticationErrorMessage("f")
case object BannedUserMessage extends AuthenticationErrorMessage("b")
case object AlreadyConnectedMessage extends AuthenticationErrorMessage("c")

case class HelloConnectMessage(ticket: String) extends DofusMessage {
  def definition = HelloConnectMessage

  def serialize(out: Out) {
    out ++= ticket
  }
}

object HelloConnectMessage extends DofusDeserializer {
  val opcode = "HC"
  def deserialize(in: In) = Some(HelloConnectMessage(in))
}

case class AuthenticationMessage(username: String, password: String) extends DofusMessage {
  def definition = AuthenticationMessage

  def serialize(out: Out) {
    out ++= username ++= "\n" ++= password
  }
}

object AuthenticationMessage extends DofusDeserializer {
  val opcode = ""
  def deserialize(in: In) = in.split("\\n#1") match {
    case Array(username, password) => Some(AuthenticationMessage(username, password))
    case _ => None
  }
}

case object QueueStatusRequestMessage extends DofusStaticMessage {
  val opcode = "Af"
  val data = ""
}

case class SetNicknameMessage(nickname: String) extends DofusMessage {
  def definition = SetNicknameMessage
  def serialize(out: Out) = out ++= nickname
}

object SetNicknameMessage extends DofusDeserializer {
  def opcode = "Ad"
  def deserialize(in: In) = Some(SetNicknameMessage(in))
}

case class SetCommunityMessage(communityId: Int) extends DofusMessage {
  def definition = SetCommunityMessage
  def serialize(out: Out) = out ++= communityId.toString
}

object SetCommunityMessage extends DofusDeserializer {
  def opcode = "Ac"
  def deserialize(in: In) = {
    try {
      Some(SetCommunityMessage(in.toInt))
    } catch {
      case _: NumberFormatException => None
    }
  }
}

case class SetSecretQuestion(secretQuestion: String) extends DofusMessage {
  def definition = SetSecretQuestion
  def serialize(out: Out) = out ++= secretQuestion.replace(" ", "+")
}

object SetSecretQuestion extends DofusDeserializer {
  def opcode = "AQ"
  def deserialize(in: In) = Some(SetSecretQuestion(in.replace("+", " ")))
}

case class AuthenticationSuccessMessage(hasRights: Boolean) extends DofusMessage {
  def definition = AuthenticationSuccessMessage
  def serialize(out: Out) = out ++= (if (hasRights) "1" else "0")
}

object AuthenticationSuccessMessage extends DofusDeserializer {
  val opcode = "AlK"
  def deserialize(in: In) = Some(AuthenticationSuccessMessage(in == "1"))
}

case object PlayerListRequestMessage extends DofusStaticMessage {
  val opcode = "Ax"
  val data = ""
}

object Test {
  implicit class SerializablesExt[T <: StringSerializable](val c: Seq[T]) extends AnyVal {
    def serialize(out: StringBuilder, sep: String = "", start: String = "", end: String = "") {
      out ++= start
      var first: Boolean = true
      for (s <- c) {
        if (first) first = false
        else out ++= sep
        s.serialize(out)
      }
    }
  }
}

case class ServerListMessage(servers: Seq[Server]) extends DofusMessage {
  import Test._

  def definition = ServerListMessage
  def serialize(out: Out) = servers.serialize(out, sep = "|")
}

object ServerListMessage extends DofusDeserializer {
  val opcode = "AH"
  def deserialize(in: In) = None
}

case class PlayerListMessage(subscriptionEnd: Time, players: Seq[PlayersOfServer]) extends DofusMessage {
  import Test._

  def definition = PlayerListMessage
  def serialize(out: Out) {
    out ++= subscriptionEnd.untilNow.inMilliseconds.toString
    players.serialize(out, start = "|", sep = "|")
  }
}

object PlayerListMessage extends DofusDeserializer {
  val opcode = "AxK"
  def deserialize(in: In) = None
}

case class ServerSelectionRequestMessage(serverId: Int) extends DofusMessage {
  def definition = ServerSelectionRequestMessage
  def serialize(out: Out) = out ++= serverId.toString
}

object ServerSelectionRequestMessage extends DofusDeserializer {
  val opcode = "AX"
  def deserialize(in: In) = try Some(ServerSelectionRequestMessage(in.toInt)) catch {
    case _: NumberFormatException => None
  }
}

case class ServerSelectionMessage(address: String, port: Int, ticket: String) extends DofusMessage {
  def definition = ServerSelectionMessage
  def serialize(out: Out) = out ++= s"$address:$port;$ticket"
}

object ServerSelectionMessage extends DofusDeserializer {
  val opcode = "AYK"
  def deserialize(in: In) = None
}

case object ServerSelectionErrorMessage extends DofusStaticMessage {
  val opcode = "AYE"
  val data = ""
}
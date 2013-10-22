package org.photon.protocol.login

import org.photon.protocol.{DofusProtocol, DofusStaticMessage, DofusDeserializer, DofusMessage}

case class VersionMessage(version: String) extends DofusMessage {
  def definition = VersionMessage

  def serialize(out: Out) {
    out ++= version
  }
}

object VersionMessage extends DofusDeserializer {
  def opcode = ""
  def deserialize(in: In) = VersionMessage(in)
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
  def deserialize(in: In) = HelloConnectMessage(in)
}

case class AuthenticationMessage(username: String, password: String) extends DofusMessage {
  def definition = AuthenticationMessage

  def serialize(out: Out) {
    out ++= username ++= "\n" ++= password
  }
}

object AuthenticationMessage extends DofusDeserializer {
  val opcode = ""
  def deserialize(in: In) = in.split("\\n") match {
    case Array(username, password) => AuthenticationMessage(username, password)
  }
}

case object QueueStatusRequest extends DofusStaticMessage {
  val opcode = "Af"
  val data = ""
}
package org.photon.protocol.login

import org.photon.protocol.{DofusDeserializer, DofusMessage}

case class HelloConnectMessage(ticket: String) extends DofusMessage {
  def definition = HelloConnectMessage

  def serialize(out: HelloConnectMessage#Out) {
    out ++= ticket
  }
}

object HelloConnectMessage extends DofusDeserializer {
  val opcode = "HC"
  def deserialize(in: HelloConnectMessage.In) = HelloConnectMessage(in)
}

case class AuthenticationMessage(username: String, password: String) extends DofusMessage {
  def definition = AuthenticationMessage

  def serialize(out: AuthenticationMessage#Out) {
    out ++= username ++= "\n" ++= password
  }
}

object AuthenticationMessage extends DofusDeserializer {
  val opcode = ""
  def deserialize(in: AuthenticationMessage.In) = in.split("\\n") match {
    case Array(username, password) => AuthenticationMessage(username, password)
  }
}

trait QueueStatusRequest extends DofusMessage {
  def serialize(out: Out) {}
}

case object QueueStatusRequest extends QueueStatusRequest with DofusDeserializer {
  val opcode = "Af"
  def definition = this
  def deserialize(in: _root_.org.photon.protocol.login.QueueStatusRequest.In) = QueueStatusRequest
}
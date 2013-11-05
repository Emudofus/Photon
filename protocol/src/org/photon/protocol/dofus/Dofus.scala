package org.photon.protocol.dofus

import org.photon.protocol.dofus.login.{ServerSelectionRequestMessage, PlayerListRequestMessage, QueueStatusRequestMessage}
import org.photon.protocol.{MessageDefinition, Message, Deserializer, Serializable}

trait StringSerializable extends Serializable {
  type Out = StringBuilder
}

trait StringDeserializer extends Deserializer {
  type In = String
}

trait DofusMessage extends Message with StringSerializable {
  override def definition: DofusDeserializer
}

trait DofusDeserializer extends MessageDefinition with StringDeserializer {
  type Opcode = String
}

trait DofusStaticMessage extends DofusMessage with DofusDeserializer {
  val data: Any

  override def definition = this
  def serialize(out: Out) = out ++= data.toString
  def deserialize(in: In) = if (in == data) Some(this) else None
}

object DofusProtocol {
  val version = "1.29.1"

  val deserializers: Map[String, DofusDeserializer] = Map(
    QueueStatusRequestMessage.opcode -> QueueStatusRequestMessage,
    PlayerListRequestMessage.opcode -> PlayerListRequestMessage,
    ServerSelectionRequestMessage.opcode -> ServerSelectionRequestMessage
  )
}
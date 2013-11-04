package org.photon.protocol

import org.photon.protocol.login.{ServerListRequestMessage, QueueStatusRequestMessage}

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
    ServerListRequestMessage.opcode -> ServerListRequestMessage
  )
}
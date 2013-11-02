package org.photon.protocol

import org.photon.protocol.login.QueueStatusRequest

trait DofusMessage extends Message with Serializable {
  type Out = StringBuilder

  override def definition: DofusDeserializer
}

trait DofusDeserializer extends MessageDefinition with Deserializer {
  type In = String
  type Opcode = String
}

trait DofusStaticMessage extends DofusMessage with DofusDeserializer {
  val data: Any

  override def definition = this
  def serialize(out: Out) = out ++= data.toString
  def deserialize(in: In) = if (in.startsWith(opcode) && in.endsWith(data.toString)) Some(this) else None
}

object DofusProtocol {
  val version = "1.29.1"

  val deserializers = Map[String, DofusDeserializer](
    QueueStatusRequest.opcode -> QueueStatusRequest
  )
}
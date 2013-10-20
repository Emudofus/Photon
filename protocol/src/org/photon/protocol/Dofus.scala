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

object DofusProtocol {
  val deserializers = Map[String, DofusDeserializer](
    QueueStatusRequest.opcode -> QueueStatusRequest
  )
}
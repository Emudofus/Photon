package org.photon.protocol

trait DofusMessage extends Message with Serializable {
  type Out = StringBuilder
}

trait DofusDeserializer[T <: DofusMessage] extends MessageDefinition[T] with Deserializer[T] {
  type In = String
  type Opcode = String
}

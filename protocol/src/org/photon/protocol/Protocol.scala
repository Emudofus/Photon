package org.photon.protocol

trait Message

trait MessageDefinition[T <: Message] {
  type Opcode
  def opcode: Opcode
}

trait Serializable { self: Message =>
  type Out
  def serialize(out: Out)
}

trait Deserializer[T <: Message] { self: MessageDefinition[T] =>
  type In
  def deserialize(in: In): T
}

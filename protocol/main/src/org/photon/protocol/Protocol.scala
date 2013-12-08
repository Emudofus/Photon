package org.photon.protocol

trait Message {
	def definition: MessageDefinition
}

trait MessageDefinition {
	type Opcode
	def opcode: Opcode
}

trait Serializable {
	type Out
	def serialize(out: Out)
}

trait Deserializer {
	type In
	def deserialize(in: In): Option[Message]
}

package org.photon.protocol.dofus.items

import org.photon.protocol.dofus._

case class UpdateWeightMessage(current: Long, max: Long) extends DofusMessage {
	require(current <= max, s"$current should be <= $max")

	def definition = UpdateWeightMessage
	def serialize(out: Out) = out append (current) append '|' append (max)
}

object UpdateWeightMessage extends DofusDeserializer {
	val opcode = "Ow"
	def deserialize(in: In) = in.split("|", 2) match {
		case Array(Long(current), Long(max)) if current <= max =>
			Some(UpdateWeightMessage(current, max))

		case _ => None
	}
}

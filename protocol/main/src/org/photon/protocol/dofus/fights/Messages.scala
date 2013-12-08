package org.photon.protocol.dofus.fights

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer}

case class FightCountMessage(count: Int) extends DofusMessage {
	def definition = FightCountMessage
	def serialize(out: Out) = out append count
}

object FightCountMessage extends DofusDeserializer {
	val opcode = "fC"
	def deserialize(in: In) = None
}

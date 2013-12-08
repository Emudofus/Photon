package org.photon.protocol.dofus.spells

import org.photon.protocol.dofus._

case class SpellListMessage(spells: Seq[Spell]) extends DofusMessage {
	def definition = SpellListMessage
	def serialize(out: Out) {
		spells.serialize(out, sep = ";")
	}
}

object SpellListMessage extends DofusDeserializer {
	val opcode = "SL"
	def deserialize(in: In): Option[SpellListMessage] = ???
}

package org.photon.protocol.dofus.spells

import org.photon.protocol.dofus.StringSerializable

case class Spell(id: Int, level: Short, direction: String) extends StringSerializable {
	def serialize(out: Out) {
		out append id append '~'
		out append level append '~'
		out append direction
	}
}
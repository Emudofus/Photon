package org.photon.protocol.dofus.game

import org.photon.protocol.dofus.Int
import com.twitter.util.Try

object Context extends Enumeration(initial = 1) {
	type Context = Value

	val Solo, Fight = Value

	def unapply(s: String): Option[Context] = s match {
		case Int(id) => Try(Context(id)).toOption
		case _ => None
	}
}

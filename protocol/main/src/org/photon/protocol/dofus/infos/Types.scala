package org.photon.protocol.dofus.infos

import org.photon.protocol.dofus.StringSerializable
import com.twitter.util.{TimeFormat, Time}

trait Info extends StringSerializable {
	def id: String
}

abstract class BaseInfo(val id: String) extends Info

abstract class EmptyInfo(id: String) extends BaseInfo(id) {
	def serialize(out: Out) {}
}

case object WelcomeInfo extends EmptyInfo("189")

case class CurrentAddressInfo(address: String) extends BaseInfo("153") {
	def serialize(out: Out) {
		out += ';'
		out ++= address
	}
}

object LastConnectionInfo {
	val format = new TimeFormat("yyyy~MM~dd~HH~mm")
}

case class LastConnectionInfo(last: Time, address: String) extends BaseInfo("152") {
	import LastConnectionInfo._

	def serialize(out: Out) {
		out += ';' ++= format.format(last) += '~' ++= address
	}
}
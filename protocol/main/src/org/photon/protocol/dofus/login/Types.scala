package org.photon.protocol.dofus.login

import org.photon.protocol.dofus.StringSerializable

object ServerState extends Enumeration {
	type ServerState = Value
	val offline = Value
	val online = Value
	val saving = Value
}

case class Server(id: Int, state: ServerState.ServerState, completion: Int, joinable: Boolean) extends StringSerializable {

	def serialize(out: Out) = out ++= s"$id;${state.id};$completion;${if (joinable) "1" else "0"}"
}

case class PlayersOfServer(serverId: Int, nplayers: Int) extends StringSerializable {

	def serialize(out: Out) = out ++= s"$serverId,$nplayers"
}
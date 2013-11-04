package org.photon.protocol.login

object ServerState extends Enumeration {
  type ServerState = Value
  val offline = Value
  val online = Value
  val saving = Value
}

case class Server(id: Int, state: ServerState.ServerState, completion: Int, joinable: Boolean) {
  override def toString = s"$id;${state.id};$completion;${if (joinable) "1" else "0"}"
}

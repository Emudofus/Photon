package org.photon.protocol.photon

import org.photon.protocol.dofus.login.{Server => Infos}
import org.photon.protocol.dofus.login.ServerState.{ServerState => State}

sealed trait Message

case class HelloConnectMessage() extends Message
case object Ack extends Message

case class AuthMessage() extends Message
case class AuthSuccessMessage() extends Message
case class AuthErrorMessage() extends Message

case class InfosUpdateMessage(infos: Infos) extends Message
case class StateUpdateMessage(state: State) extends Message

case class PlayerListMessage(userId: Long) extends Message
case class PlayerListSuccessMessage(userId: Long, nplayers: Int) extends Message
case class PlayerListErrorMessage(userId: Long) extends Message

case class GrantAccessMessage(userId: Long, ticket: String) extends Message
case class GrantAccessSuccessMessage(userId: Long) extends Message
case class GrantAccessErrorMessage(userId: Long) extends Message
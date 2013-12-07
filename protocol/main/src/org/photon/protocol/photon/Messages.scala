package org.photon.protocol.photon

import org.photon.protocol.dofus.login.{Server => Infos}
import org.photon.protocol.dofus.login.ServerState.{ServerState => State}

sealed trait Message

case class HelloConnectMessage(salt: Array[Byte]) extends Message
case object Ack extends Message

case class AuthMessage(id: Int, credentials: Array[Byte], salt: Array[Byte]) extends Message
case object AuthSuccessMessage extends Message
case object AuthErrorMessage extends Message

case class PublicIdentityMessage(address: String, port: Int) extends Message
case class InfosUpdateMessage(infos: Infos) extends Message
case class StateUpdateMessage(state: State) extends Message

case class PlayerListMessage(userId: Long) extends Message
case class PlayerListSuccessMessage(userId: Long, nplayers: Int) extends Message
case class PlayerListErrorMessage(userId: Long) extends Message

case class GrantAccessMessage(userInfos: UserInfos, ticket: String) extends Message
case class GrantAccessSuccessMessage(userId: Long) extends Message
case class GrantAccessErrorMessage(userId: Long) extends Message
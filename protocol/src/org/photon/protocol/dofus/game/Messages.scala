package org.photon.protocol.dofus.game

import org.photon.protocol.dofus._

import Context.Context

case class GameContextCreationMessage(context: Context) extends DofusMessage {
  def definition = GameContextCreationMessage
  def serialize(out: Out) = out append context.id
}

object GameContextCreationMessage extends DofusDeserializer {
  val opcode = "GC"
  def deserialize(in: In) = in match {
    case Context(context) => Some(GameContextCreationMessage(context))
    case _ => None
  }
}
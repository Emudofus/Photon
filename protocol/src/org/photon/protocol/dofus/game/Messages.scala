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

case class GameContextCreationSuccessMessage(context: Context) extends DofusMessage {
  def definition = GameContextCreationSuccessMessage
  def serialize(out: Out) = out append '|' append (context.id) append '|'
}

object GameContextCreationSuccessMessage extends DofusDeserializer {
  val opcode = "GCK"
  def deserialize(in: In) = None
}

case class MapDataMessage(id: Int, date: String, key: String) extends DofusMessage {
  def definition = MapDataMessage
  def serialize(out: Out) = out append '|' append (id) append '|' append (date) append '|' append (key)
}

object MapDataMessage extends DofusDeserializer {
  val opcode = "GDM"
  def deserialize(in: In) = None
}

case object MapLoadSuccessMessage extends DofusStaticMessage {
  val opcode = "GDK"
  val data = ""
}

case object GameContextDescriptionMessage extends DofusStaticMessage {
  val opcode = "GI"
  val data = ""
}
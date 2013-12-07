package org.photon.protocol.dofus.infos

import org.photon.protocol.dofus._

case class InfoMessage(info: Info) extends DofusMessage {
  def definition = InfoMessage
  def serialize(out: Out) {
    out ++= info.id
    info.serialize(out)
  }
}

object InfoMessage extends DofusDeserializer {
  val opcode = "Im"
  def deserialize(in: In) = None
}

case class ScreenResolutionMessage(width: Int, height: Int, tpe: Int) extends DofusMessage {
  def definition = ScreenResolutionMessage
  def serialize(out: Out) = ???
}

object ScreenResolutionMessage extends DofusDeserializer {
  val opcode = "Ir"
  def deserialize(in: In) = in.split("|", 3) match {
    case Array(Int(width), Int(height), Int(tpe)) => Some(ScreenResolutionMessage(width, height, tpe))
    case _ => None
  }
}
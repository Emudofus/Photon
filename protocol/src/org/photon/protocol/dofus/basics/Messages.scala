package org.photon.protocol.dofus.basics

import org.photon.protocol.dofus.{DofusMessage, DofusDeserializer, DofusStaticMessage}
import com.twitter.util.{Try, TimeFormat, Time}

case object CurrentDateRequestMessage extends DofusStaticMessage {
  val opcode = "BD"
  val data = ""
}

case class CurrentDateMessage(current: Time) extends DofusMessage {
  import CurrentDateMessage.format

  def definition = CurrentDateMessage
  def serialize(out: Out) = out ++= format.format(current)
}

object CurrentDateMessage extends DofusDeserializer {
  val format = new TimeFormat("yyyy|MM|dd")

  val opcode = "BD"
  def deserialize(in: In) = Try(format.parse(in)).toOption.map(CurrentDateMessage.apply)
}
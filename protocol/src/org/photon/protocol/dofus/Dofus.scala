package org.photon.protocol.dofus

import org.photon.protocol.dofus.login.{ServerSelectionRequestMessage, PlayerListRequestMessage, QueueStatusRequestMessage}
import org.photon.protocol.{MessageDefinition, Message, Deserializer, Serializable}
import scala.annotation.tailrec

trait StringSerializable extends Serializable {
  type Out = StringBuilder
}

trait StringDeserializer extends Deserializer {
  type In = String
}

trait DofusMessage extends Message with StringSerializable {
  override def definition: DofusDeserializer
}

trait DofusDeserializer extends MessageDefinition with StringDeserializer {
  type Opcode = String

  override def deserialize(in: In): Option[DofusMessage]
}

trait DofusStaticMessage extends DofusMessage with DofusDeserializer {
  val data: Any

  override def definition = this
  def serialize(out: Out) = out ++= data.toString
  def deserialize(in: In) = if (in == data) Some(this) else None
}

object DofusProtocol {
  val version = "1.29.1"

  val deserializers: Map[String, DofusDeserializer] = Map(
    QueueStatusRequestMessage.opcode -> QueueStatusRequestMessage,
    PlayerListRequestMessage.opcode -> PlayerListRequestMessage,
    ServerSelectionRequestMessage.opcode -> ServerSelectionRequestMessage
  )

  def deserialize(o: String): Option[DofusMessage] = o.splitAt(2) match {
    case (opcode, data) => deserializers.get(opcode).flatMap(_.deserialize(data))
  }

  def serialize(messages: List[DofusMessage]): String = {

    @tailrec
    def rec(o: List[DofusMessage])(implicit builder: StringBuilder): String = o match {
      case head :: tail =>
        builder ++= head.definition.opcode
        head.serialize(builder)
        builder += '\0'

        rec(tail)

      case Nil =>
        builder.result()
    }

    rec(messages)(StringBuilder.newBuilder)
  }
}
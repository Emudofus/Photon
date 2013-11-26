package org.photon.protocol

package object dofus {
  implicit class SerializablesExt[T <: StringSerializable](val c: Seq[T]) extends AnyVal {
    def serialize(out: StringBuilder, sep: String = "", start: String = "", end: String = "") {
      out ++= start
      var first: Boolean = true
      for (s <- c) {
        if (first) first = false
        else out ++= sep
        s.serialize(out)
      }
    }
  }

  def hex(c: Int) = if (c < 0) "-1" else Integer.toString(c, 16)
  def btoi(b: Boolean) = if (b) "1" else "0"
  def itob(i: String) = i == "1"

  object Int {
    def unapply(s: String): Option[Int] =
      try {
        Some(s.toInt)
      } catch {
        case _: NumberFormatException => None
      }
  }

  object Long {
    def unapply(s: String): Option[Long] =
      try {
        Some(s.toLong)
      } catch {
        case _: NumberFormatException => None
      }
  }
}

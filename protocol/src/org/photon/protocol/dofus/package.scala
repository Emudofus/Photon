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
}

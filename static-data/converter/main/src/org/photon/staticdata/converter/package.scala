package org.photon.staticdata

import java.sql.ResultSet
import scala.collection.generic.CanBuildFrom

package object converter {
	import scala.language.implicitConversions

	implicit def rset2stream(rset: ResultSet): Stream[ResultSet] =
		Stream.continually(rset).takeWhile(_.next())

	implicit class WithIndexes[T](val self: TraversableOnce[T]) extends AnyVal {
		def withIndexes[Result](implicit cbf: CanBuildFrom[Nothing, (Int, T), Result]): Result = {
			var index = 0
			val builder = cbf()
			self.foreach { elem =>
				val t = (index, elem)
				builder += t
				index += 1
			}
			builder.result()
		}
	}
}
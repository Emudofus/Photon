package org.photon.staticdata

import java.sql.ResultSet

package object converter {
	import scala.language.implicitConversions

	implicit def rset2stream(rset: ResultSet): Stream[ResultSet] =
		Stream.continually(rset).takeWhile(_.next())
}
package org.photon.common

import java.sql.{Connection, ResultSet, PreparedStatement}
import scala.collection.generic.CanBuildFrom

object JdbcExtension {
  def statement[R](query: String)(fn: PreparedStatement => R)(implicit co: Connection): R = {
    val stmt = co.prepareStatement(query)
    try {
      fn(stmt)
    } finally {
      stmt.close()
    }
  }

  def resultsOf[R, C](stmt: PreparedStatement)(fn: ResultSet => R)(implicit cbf: CanBuildFrom[_, R, C]): C = {
    val rset = stmt.executeQuery()
    try {
      val builder = cbf()
      while (rset.next()) {
        builder += fn(rset)
      }
      builder.result()
    } finally {
      rset.close()
    }
  }

  def resultOf[R](stmt: PreparedStatement)(fn: ResultSet => R): Option[R] = {
    val rset = stmt.executeQuery()
    try {
      if (!rset.next()) None
      else Some(fn(rset))
    } finally {
      rset.close()
    }
  }
}

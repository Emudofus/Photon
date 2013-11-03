package org.photon.common

import com.twitter.util.Future
import java.util.concurrent.Executor
import java.sql.{PreparedStatement, ResultSet, Connection}

trait Model[T <: Model[T]] {
  type PrimaryKey

  def id: PrimaryKey
  def persisted: Boolean

  protected def self = this.asInstanceOf[T]
  def persist(implicit r: Repository[T]) = r.persist(self)
  def remove(implicit r: Repository[T]) = r.remove(self)
}

trait Repository[T] {
  type PrimaryKey

  def find(id: PrimaryKey): Future[Option[T]]

  def persist(o: T): Future[T]
  def remove(o: T): Future[Unit]
}

trait BaseRepository[T <: Model[T]] extends Repository[T] {
  import org.photon.common.JdbcExtension._

  type PrimaryKey = T#PrimaryKey

  implicit val executor: Executor
  implicit val connection: Connection

  def tableName: String
  def columns: Seq[String]

  def selectQuery(query: String) = s"SELECT ${columns.mkString(", ")} FROM $tableName $query"
  lazy val updateQuery = s"UPDATE $tableName SET ${columns.tail.map(x => s"$x=?").mkString(", ")} WHERE ${columns.head}=?"
  lazy val insertQuery = s"INSERT INTO $tableName(${columns.tail.mkString(", ")}) VALUES(${columns.tail.map(_ => "?").mkString(", ")}) RETURNING ${columns.head}"
  lazy val deleteQuery = s"DELETE FROM $tableName WHERE ${columns.head}=?"

  protected def create(rset: ResultSet): T
  protected def setValues(s: PreparedStatement, o: T)
  protected def setPrimaryKey(s: PreparedStatement): (Int, PrimaryKey) => Unit
  protected def setPersisted(o: T, rset: ResultSet): T

  def find[V](column: String, value: V)(fn: PreparedStatement => (Int, V) => Unit): Future[Option[T]] = Async {
    statement(selectQuery(s"WHERE $column=?")) { s =>
      fn(s)(1, value)

      resultsOf(s)(create).headOption
    }
  }

  def find(id: PrimaryKey) = find(columns.head, id)(setPrimaryKey)

  def persist(o: T) = Async {
    if (o.persisted) {
      statement(updateQuery) { s =>
        setValues(s, o)
        setPrimaryKey(s)(columns.length, o.id)

        s.executeUpdate()
        o
      }
    } else {
      statement(insertQuery) { s =>
        setValues(s, o)

        resultOf(s)(setPersisted(o, _)).get
      }
    }
  }

  def remove(o: T) = Async {
    statement(deleteQuery) { s =>
      setPrimaryKey(s)(1, o.id)
      s.executeUpdate()
      () // force return Unit
    }
  }
}

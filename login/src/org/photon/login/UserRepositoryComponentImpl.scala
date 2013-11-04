package org.photon.login

import com.twitter.util.{Time, Future}
import org.photon.common.BaseRepository
import java.sql.{PreparedStatement, ResultSet}

trait UserRepositoryComponentImpl extends UserRepositoryComponent { self: DatabaseComponent with ExecutorComponent =>

  class UserRepositoryImpl extends BaseRepository[User] with UserRepository {

    implicit val executor = self.executor
    implicit val connection = self.database

    val tableName = "users"
    val columns = Seq("id", "name", "password", "nickname", "secret_question", "secret_answer", "community_id", "subscription_end")

    protected def create(rset: ResultSet) = User(
      rset.getLong("id"),
      rset.getString("name"),
      rset.getString("password"),
      rset.getString("nickname"),
      rset.getString("secret_question"),
      rset.getString("secret_answer"),
      rset.getInt("community_id"),
      Time(rset.getTimestamp("subscription_end")),
      persisted = true
    )

    protected def setValues(s: PreparedStatement, o: User) {
      s.setString(1, o.name)
      s.setString(2, o.password)
      s.setString(3, o.nickname)
      s.setString(4, o.secretQuestion)
      s.setString(5, o.secretAnswer)
      s.setInt(6, o.communityId)
      s.setTimestamp(7, new java.sql.Timestamp(o.subscriptionEnd.inMicroseconds))
    }

    protected def setPrimaryKey(s: PreparedStatement) = s.setLong

    protected def setPersisted(o: User, rset: ResultSet) = o.copy(id = rset.getLong("id"), persisted = true)

    def find(name: String): Future[User] = find("name", name)(s => s.setString) map {_ getOrElse (throw UnknownUserException())}
  }


  val users = new UserRepositoryImpl

}

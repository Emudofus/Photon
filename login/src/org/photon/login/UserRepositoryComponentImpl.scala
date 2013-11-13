package org.photon.login

import com.twitter.util.Time
import java.sql.{PreparedStatement, ResultSet}
import org.photon.common.components.{ExecutorComponent, DatabaseComponent}
import org.photon.common.persist.{ModelState, BaseRepository}

trait UserRepositoryComponentImpl extends UserRepositoryComponent { self: DatabaseComponent with ExecutorComponent =>
  import org.photon.common.persist.Parameters._
  import org.photon.common.persist.Connections._

  val users = new UserRepositoryImpl

  class UserRepositoryImpl extends BaseRepository[User](self.database) with UserRepository {
    lazy val table = "users"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("name", "password", "nickname", "secret_question", "secret_answer", "community_id", "subscription_end")

    def buildModel(rs: ResultSet) = User(
      rs.getLong("id"),
      rs.getString("name"),
      rs.getString("password"),
      rs.getString("nickname"),
      rs.getString("secret_question"),
      rs.getString("secret_answer"),
      rs.getInt("community_id"),
      rs.get[Time]("subscription_end")
    )

    def bindParams(ps: PreparedStatement, user: User) {
      ps.set(1, user.name)
      ps.set(2, user.password)
      ps.set(3, user.nickname)
      ps.set(4, user.secretQuestion)
      ps.set(5, user.secretAnswer)
      ps.set(6, user.communityId)
      ps.set(7, user.subscriptionEnd)
    }

    def setPersisted(user: User, newId: Long) = user.copy(id = newId, state = ModelState.Persisted)
    def setRemoved(user: User) = user.copy(state = ModelState.Removed)

    def find(name: String) = find("name", name)
  }
}

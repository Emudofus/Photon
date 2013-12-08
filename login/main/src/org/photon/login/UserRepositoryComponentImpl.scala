package org.photon.login

import com.twitter.util.Time
import java.sql.{PreparedStatement, ResultSet}
import org.photon.common.components.{ExecutorComponent, DatabaseComponent}
import org.photon.common.persist.{Incremented, ModelState, BaseRepository}

trait UserRepositoryComponentImpl extends UserRepositoryComponent { self: DatabaseComponent with ExecutorComponent =>
	import org.photon.common.persist.Parameters._
	import org.photon.common.persist.Connections._

	val users = new UserRepositoryImpl

	class UserRepositoryImpl extends BaseRepository[User](self.database) with UserRepository {
		lazy val table = "users"
		lazy val pkColumns = Seq("id")
		lazy val columns = Seq("name", "password", "salt", "nickname", "secret_question", "secret_answer", "community_id", "subscription_end")

		def buildModel(rs: ResultSet) = User(
			rs.getLong("id"),
			rs.getString("name"),
			rs.getString("password"),
			rs.getString("salt").trim,
			rs.getString("nickname"),
			rs.getString("secret_question"),
			rs.getString("secret_answer"),
			rs.getInt("community_id"),
			rs.get[Time]("subscription_end")
		)

		def bindParams(ps: PreparedStatement, user: User)(implicit index: Incremented[Int]) {
			ps.set(user.name)
			ps.set(user.password)
			ps.set(user.salt)
			ps.set(user.nickname)
			ps.set(user.secretQuestion)
			ps.set(user.secretAnswer)
			ps.set(user.communityId)
			ps.set(user.subscriptionEnd)
		}

		def setPersisted(user: User, newId: Long) = user.copy(id = newId, state = ModelState.Persisted)
		def setRemoved(user: User) = user.copy(state = ModelState.Removed)

		def find(name: String) = findBy("name", name)
	}
}

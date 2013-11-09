package org.photon.realm

import org.photon.common.components.{ExecutorComponent, DatabaseComponent}
import org.photon.common.BaseRepository
import com.twitter.util.Future
import java.sql.{PreparedStatement, ResultSet}

trait PlayerRepositoryComponentImpl extends PlayerRepositoryComponent {
  self: DatabaseComponent with ExecutorComponent =>

  implicit val playerRepository = new PlayerRepositoryImpl

  class PlayerRepositoryImpl extends PlayerRepository with BaseRepository[Player] {
    implicit val executor = self.executor
    implicit val connection = self.database

    val tableName = "players"
    val columns = Seq("id", "owner_id", "name")

    protected def create(rset: ResultSet) = Player(
      rset.getLong("id"),
      rset.getLong("owner_id"),
      rset.getString("name")
    )

    protected def setValues(s: PreparedStatement, o: Player) {
      s.setLong(1, o.id)
      s.setLong(2, o.ownerId)
      s.setString(3, o.name)
    }

    protected def setPrimaryKey(s: PreparedStatement) = s.setLong

    protected def setPersisted(o: Player, rset: ResultSet) = o.copy(id = rset.getLong("id"), persisted = true)

    def findByName(name: String) = find("name", name)(_.setString).map(_.get)

    def findByOwner(ownerId: Long) = where("owner_id=?") { _.setLong(1, ownerId) }
  }
}

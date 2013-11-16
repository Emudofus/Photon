package org.photon.realm

import org.photon.common.components.{ExecutorComponent, DatabaseComponent}
import java.sql.{PreparedStatement, ResultSet}
import org.photon.common.persist.{ModelState, BaseRepository}

trait PlayerRepositoryComponentImpl extends PlayerRepositoryComponent {
  self: DatabaseComponent with ExecutorComponent =>
  import org.photon.common.persist.Parameters._
  import org.photon.common.persist.Connections._

  implicit val playerRepository = new PlayerRepositoryImpl

  class PlayerRepositoryImpl extends BaseRepository[Player](self.database) with PlayerRepository {
    lazy val table = "players"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("owner_id", "name")

    def buildModel(rset: ResultSet) = Player(
      rset.getLong("id"),
      rset.getLong("owner_id"),
      rset.getString("name")
    )

    def bindParams(ps: PreparedStatement, user: Player) {
      ps.set(1, user.id)
      ps.set(2, user.ownerId)
      ps.set(3, user.name)
    }

    def setPersisted(o: Player, newId: Long) = o.copy(id = newId, state = ModelState.Persisted)
    def setRemoved(o: Player) = o.copy(state = ModelState.Removed)

    def findByName(name: String) = find("name", name)
    def findByOwner(ownerId: Long) = filter("owner_id", ownerId)
  }
}

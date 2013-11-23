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
    lazy val columns = Seq("owner_id", "name", "level", "skin", "color1", "color2", "color3")

    def buildModel(rset: ResultSet) = Player(
      rset.getLong("id"),
      rset.getLong("owner_id"),
      rset.getString("name"),
      rset.getShort("level"),
      new PlayerAppearence(
        rset.getShort("skin"),
        Colors(
          rset.getInt("color1"),
          rset.getInt("color2"),
          rset.getInt("color3")
        )
      ),
      ModelState.Persisted
    )

    def bindParams(ps: PreparedStatement, user: Player) {
      ps.set(1, user.id)
      ps.set(2, user.ownerId)
      ps.set(3, user.name)
      ps.set(4, user.level)
      ps.set(5, user.appearence.skin)
      ps.set(6, user.appearence.colors.first)
      ps.set(7, user.appearence.colors.second)
      ps.set(8, user.appearence.colors.third)
    }

    def setPersisted(o: Player, newId: Long) = o.copy(id = newId, state = ModelState.Persisted)
    def setRemoved(o: Player) = o.copy(state = ModelState.Removed)

    def findByName(name: String) = find("name", name)
    def findByOwner(ownerId: Long) = filter("owner_id", ownerId)
  }
}

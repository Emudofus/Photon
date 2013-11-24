package org.photon.realm

import org.photon.common.components.{ExecutorComponent, DatabaseComponent}
import java.sql.{PreparedStatement, ResultSet}
import org.photon.common.persist.{Incremented, ModelState, BaseRepository}

trait PlayerRepositoryComponentImpl extends PlayerRepositoryComponent {
  self: DatabaseComponent with ExecutorComponent =>
  import org.photon.common.persist.Parameters._
  import org.photon.common.persist.Connections._

  implicit val playerRepository = new PlayerRepositoryImpl

  class PlayerRepositoryImpl extends BaseRepository[Player](self.database) with PlayerRepository {
    lazy val table = "players"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("owner_id", "name", "breed", "level", "skin", "color1", "color2", "color3")

    def buildModel(rset: ResultSet) = Player(
      rset.getLong("id"),
      rset.getLong("owner_id"),
      rset.getString("name"),
      rset.getShort("breed"),
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

    def bindParams(ps: PreparedStatement, user: Player)(implicit index: Incremented[Int]) {
      ps.set(user.ownerId)
      ps.set(user.name)
      ps.set(user.breed)
      ps.set(user.level)
      ps.set(user.appearence.skin)
      ps.set(user.appearence.colors.first)
      ps.set(user.appearence.colors.second)
      ps.set(user.appearence.colors.third)
    }

    def setPersisted(o: Player, newId: Long) = o.copy(id = newId, state = ModelState.Persisted)
    def setRemoved(o: Player) = o.copy(state = ModelState.Removed)

    def findByName(name: String) = findBy("name", name)
    def findByOwner(ownerId: Long) = filter("owner_id", ownerId)

    def create(ownerId: Long, name: String, breed: Short, gender: Boolean, color1: Int, color2: Int, color3: Int) =
      persist(Player(
        -1L,
        ownerId,
        name,
        breed,
        1,
        new PlayerAppearence(
          (10 * breed + (if (gender) 1 else 0)).toShort,
          Colors(
            color1,
            color2,
            color3
          )
        )
      ))
  }
}

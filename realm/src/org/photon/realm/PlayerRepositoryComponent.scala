package org.photon.realm

import com.twitter.util.Future
import org.photon.common.persist.{Repository, Model, ModelState}
import org.photon.common.persist.ModelState.ModelState
import org.photon.protocol.dofus.account.{Player => PlayerTemplate}

case class Colors(first: Int, second: Int, third: Int)

class PlayerAppearence(val skin: Short, val colors: Colors) {
  def accessories: Seq[Int] = Seq.fill(5)(0)
}

case class Player(
  id: Long,
  ownerId: Long,
  name: String,
  level: Short,
  appearence: PlayerAppearence,
  state: ModelState = ModelState.None
) extends Model {
  type PrimaryKey = Long

  def toPlayerTemplate = PlayerTemplate(
    id,
    name,
    level,
    appearence.skin,
    appearence.colors.first,
    appearence.colors.second,
    appearence.colors.third,
    appearence.accessories,
    merchant = false,
    serverId = -1,
    dead = false,
    deathCount = 0,
    levelMax = 200
  )
}

trait PlayerRepository extends Repository[Player] {
  def findByName(name: String): Future[Player]
  def findByOwner(ownerId: Long): Future[Seq[Player]]
}

trait PlayerRepositoryComponent {
  implicit val playerRepository: PlayerRepository
}

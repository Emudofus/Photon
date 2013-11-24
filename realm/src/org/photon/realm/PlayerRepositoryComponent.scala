package org.photon.realm

import com.twitter.util.Future
import org.photon.common.persist.{Cachable, Repository, Model, ModelState}
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
  breed: Short,
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

final case class SubscriptionOutException() extends RuntimeException
final case class UnavailableSpaceException() extends RuntimeException
final case class ExistingPlayerNameException() extends RuntimeException
final case class BadPlayerNameException() extends RuntimeException

trait PlayerRepository extends Repository[Player] with Cachable[Player] {
  def findByName(name: String): Future[Player]
  def findByOwner(ownerId: Long): Future[Seq[Player]]

  def create(ownerId: Long, name: String, breed: Short, gender: Boolean, color1: Int, color2: Int, color3: Int): Future[Player]
}

trait PlayerRepositoryComponent {
  implicit val playerRepository: PlayerRepository
}

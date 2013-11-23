package org.photon.realm

import com.twitter.util.Future
import org.photon.common.persist.{Repository, Model, ModelState}
import org.photon.common.persist.ModelState.ModelState
import org.photon.protocol.dofus.account.{Player => PlayerTemplate}

case class Player(
  id: Long,
  ownerId: Long,
  name: String,
  state: ModelState = ModelState.None
) extends Model {
  type PrimaryKey = Long

  def toPlayerTemplate: PlayerTemplate = ???
}

trait PlayerRepository extends Repository[Player] {
  def findByName(name: String): Future[Player]
  def findByOwner(ownerId: Long): Future[Seq[Player]]
}

trait PlayerRepositoryComponent {
  implicit val playerRepository: PlayerRepository
}

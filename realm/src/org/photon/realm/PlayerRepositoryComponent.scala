package org.photon.realm

import org.photon.common.{Model, Repository}
import com.twitter.util.Future

case class Player(
  id: Long,
  ownerId: Long,
  name: String,
  persisted: Boolean = true
) extends Model[Player] {
  type PrimaryKey = Long
}

trait PlayerRepository extends Repository[Player] {
  def findByName(name: String): Future[Player]
  def findByOwner(ownerId: Long): Future[Seq[Player]]
}

trait PlayerRepositoryComponent {
  implicit val playerRepository: PlayerRepository
}

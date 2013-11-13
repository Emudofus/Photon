package org.photon.login

import com.twitter.util.{Time, Future}
import org.photon.common.persist.{Repository, Model, ModelState}
import org.photon.common.persist.ModelState.ModelState

case class User(
  id: Long,
  name: String,
  password: String,
  nickname: String,
  secretQuestion: String,
  secretAnswer: String,
  communityId: Int,
  subscriptionEnd: Time,
  state: ModelState = ModelState.None
) extends Model {
  type PrimaryKey = Long
}

trait UserRepository extends Repository[User] {
  def find(name: String): Future[User]
}

trait UserRepositoryComponent {
  sealed abstract class RepositoryException extends RuntimeException
  case class UnknownUserException() extends RepositoryException

  implicit val users: UserRepository
}
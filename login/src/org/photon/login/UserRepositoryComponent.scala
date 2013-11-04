package org.photon.login

import com.twitter.util.{Time, Future}
import org.photon.common.{Repository, Model}

case class User(
  id: Long,
  name: String,
  password: String,
  nickname: String,
  secretQuestion: String,
  secretAnswer: String,
  communityId: Int,
  subscriptionEnd: Time,
  persisted: Boolean = false
) extends Model[User] {
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
package org.photon.login

import com.twitter.util.Future

trait Model[T] {
  protected def self(implicit ev: this.type <:< T) = ev(this)

  def persist(implicit r: Repository[T]) = r.persist(self)
  def remove(implicit r: Repository[T]) = r.remove(self)
}

trait Repository[T] {
  type PrimaryKey

  def find(id: PrimaryKey): Future[T]

  def persist(o: T): Future[Unit]
  def remove(o: T): Future[Unit]
}

case class User(
  id: Long,
  name: String,
  password: String
) extends Model[User] {

}

trait UserRepository extends Repository[User] {
  type PrimaryKey = Long

  def find(name: String): Future[User]
}

trait UserRepositoryComponent {
  sealed abstract class RepositoryException extends RuntimeException
  case class UnknownUserException() extends RepositoryException

  val users: UserRepository
}
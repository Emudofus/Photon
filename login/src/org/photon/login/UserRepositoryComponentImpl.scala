package org.photon.login

import com.twitter.util.Future

trait UserRepositoryComponentImpl extends UserRepositoryComponent {
  class UserRepositoryImpl extends UserRepository {
    def find(id: PrimaryKey): Future[User] = ???
    def find(name: String): Future[User] = ???

    def persist(o: User): Future[Unit] = ???
    def remove(o: User): Future[Unit] = ???
  }


  val users = new UserRepositoryImpl

}

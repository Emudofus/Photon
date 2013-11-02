package org.photon.login

import com.twitter.util.Future

trait UserRepositoryComponentImpl extends UserRepositoryComponent {
  class UserRepositoryImpl extends UserRepository {
    def find(id: PrimaryKey): Future[User] = ???
    def find(name: String): Future[User] = Future(User(1, name, "fake", name, "fake", "fake")) // TODO give real data

    def persist(o: User): Future[Unit] = ???
    def remove(o: User): Future[Unit] = ???
  }


  val users = new UserRepositoryImpl

}

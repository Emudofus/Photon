package org.photon.login

import com.twitter.util.{Future, Throw, Return}

trait UserAuthenticationComponentImpl extends UserAuthenticationComponent {
  self: UserRepositoryComponent =>

  import Future.{exception => throws}

  def authenticate(s: NetworkSession, username: String, password: String) = users.find(username).transform {
    case Return(user) =>
      if (user.password != password)
        throws(AccessDeniedException())

      // TODO check if user is banned, already connected, ...

      else
        Future(user)

    case Throw(UnknownUserException()) =>
      throws(AccessDeniedException())

    case Throw(ex) => throws(ex)
  }
}

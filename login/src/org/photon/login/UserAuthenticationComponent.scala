package org.photon.login

import com.twitter.util.Future

trait UserAuthenticationComponent {
  sealed abstract class AuthenticationException extends RuntimeException
  case class BannedUserException() extends AuthenticationException
  case class AlreadyConnectedException() extends AuthenticationException
  case class AccessDeniedException() extends AuthenticationException

  def authenticate(s: NetworkSession, username: String, password: String): Future[Unit]
}

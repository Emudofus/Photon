package org.photon.login

import com.twitter.util.{Future, Throw, Return}
import org.photon.common.Strings

trait UserAuthenticationComponentImpl extends UserAuthenticationComponent {
	self: UserRepositoryComponent =>

	import Future.{exception => throws}
	import Strings.{decryptDofusPassword => decrypt}

	def validPassword(user: User, password: String, key: String) = user.password == decrypt(password, key)

	def authenticate(s: NetworkSession, username: String, password: String) = users.find(username).transform {
		case Return(user) =>
			if (!validPassword(user, password, s.ticket))
				throws(AccessDeniedException())

			// TODO check if user is banned, already connected, ...

			else
				Future(user)

		case Throw(UnknownUserException()) =>
			throws(AccessDeniedException())

		case Throw(ex) => throws(ex)
	}
}

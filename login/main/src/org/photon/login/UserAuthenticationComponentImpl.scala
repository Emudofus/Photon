package org.photon.login

import com.twitter.util.{Future, Throw, Return}
import org.photon.common.Strings
import java.security.MessageDigest
import java.nio.charset.Charset

trait UserAuthenticationComponentImpl extends UserAuthenticationComponent {
	self: UserRepositoryComponent with ConfigurationComponent =>

	import Future.{exception => throws}
	import Strings.{decryptDofusPassword => decrypt}
	import UserAuthenticationComponentImpl.encrypt

	val thisConfig = config.getConfig("photon.database.users")
	implicit val digest = MessageDigest.getInstance(thisConfig.getString("password-digest"))
	implicit val charset = Charset.forName(thisConfig.getString("password-encoding"))

	def validPassword(user: User, password: String, key: String) = {
		val expected = user.password
		println(expected)

		var tested = decrypt(password, key)
		println(UserAuthenticationComponentImpl.tohex(tested.getBytes(charset)))
		println(UserAuthenticationComponentImpl.tohex(user.salt.getBytes(charset)))
		tested = encrypt(tested, user.salt)
		println(tested)

		expected == tested
	}

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

object UserAuthenticationComponentImpl {
	import scala.language.postfixOps
	import java.security.MessageDigest
	import java.nio.charset.Charset

	def tohex[Bytes <% Seq[Byte]](bytes: Bytes): String = {
		bytes map { it => (0xFF & it).formatted("%02x") } mkString
	}

	def hash(in: String)(implicit digest: MessageDigest, charset: Charset): String = tohex(digest.digest(in.getBytes(charset)))

	def encrypt(clear: String, salt: String)(implicit digest: MessageDigest, charset: Charset): String =
		hash(salt + hash(clear) + salt)
}

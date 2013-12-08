package org.photon.realm

import org.photon.common.{network => base}
import java.nio.charset.Charset
import com.twitter.util.Future
import org.photon.protocol.photon.UserInfos

final case class GrantAccessException(reason: String = "", underlying: Throwable = null)
	extends RuntimeException(reason, underlying)

final case class AuthException(reason: String = "", underlying: Throwable = null)
	extends RuntimeException(reason, underlying)

trait NetworkService extends base.NetworkService {
	def grantUser(user: UserInfos, ticket: String): Future[Unit]
	def auth(ticket: String): Future[UserInfos]
}

trait NetworkSession extends base.NetworkSession {
	var userOption: Option[UserInfos]
	def user = userOption.get

	var playerOption: Option[Player]
	def player = playerOption.get
}

trait NetworkComponent {
	self: ConfigurationComponent =>

	val networkConfig = config.getConfig("photon.network.realm")
	val networkPort = networkConfig.getInt("port")
	val networkCharset = Charset.forName(networkConfig.getString("charset"))

	val networkService: NetworkService
}

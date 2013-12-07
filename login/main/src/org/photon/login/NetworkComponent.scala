package org.photon.login

import java.nio.charset.Charset
import org.photon.common.{network => base, Observable}

trait NetworkService extends base.NetworkService

trait NetworkSession extends base.NetworkSession {
  import NetworkSession._

  var state: State

  var realmUpdatedLid: Option[Observable.Lid]

  var userOption: Option[User]
  def user = userOption.get

  def ticket: String
}

object NetworkSession {
  sealed trait State
  case object VersionCheckState extends State
  case object AuthenticationState extends State
  case object ServerSelectionState extends State
}

trait NetworkComponent { self: ConfigurationComponent =>
  val networkConfig = config.getConfig("photon.network.login")

  val networkPort = networkConfig.getInt("port")
  val networkCharset = Charset.forName(networkConfig.getString("charset"))


  val networkService: NetworkService
}
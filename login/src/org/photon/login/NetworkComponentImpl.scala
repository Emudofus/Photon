package org.photon.login

trait NetworkComponentImpl extends NetworkComponent { self: ConfigurationComponent with HandlerComponent =>
  val networkService: NetworkService = ???
}
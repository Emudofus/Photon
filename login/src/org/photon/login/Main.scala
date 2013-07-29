package org.photon.login

import java.net.InetSocketAddress

import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.util.Future
import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]) {
    val config = ConfigFactory.parseString("""
      photon {
        login {
          port = 5555
        }
      }
      """)

    val server = ServerBuilder()
      .codec(StringCodec)
      .bindTo(new InetSocketAddress(config.getInt("photon.login.port")))
      .name("PhotonLoginFTW")
      .build(service)
  }
}

object DofusService extends Service[String, String] {
  def apply(req: String) = {
    Future("lol")
  }
}


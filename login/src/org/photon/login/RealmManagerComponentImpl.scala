package org.photon.login

import com.twitter.util.Future
import org.photon.protocol.login.{Server, ServerState}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable
import org.apache.mina.transport.socket.nio.{NioProcessor, NioSocketAcceptor}
import org.photon.common.Async
import java.net.InetSocketAddress
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.write.WriteRequest
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent with ExecutorComponent with ServiceManagerComponent =>
  import scala.collection.JavaConversions._

  val realmManager = new RealmManagerImpl
  services += realmManager

  private val realmServerSessionAttributeKey = "photon.network.realm.session"

  class RealmManagerImpl extends RealmManager with Logging {
    val servers = mutable.Map.empty[Int, RealmServerImpl]

    val acceptor = new NioSocketAcceptor(executor, new NioProcessor(executor))
    acceptor.setDefaultLocalAddress(new InetSocketAddress(realmManagerPort))
    acceptor.setHandler(new RealmManagerHandlerImpl)

    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory))
    acceptor.getFilterChain.addLast("logging", new RealmManagerLoggingImpl)

    def find(serverId: Int) = servers.get(serverId)
    def availableServers = servers.values.toStream filter (_.isAvailable) map (_.infos)
    def playerList(user: User) = Future collect (servers.values.toSeq map (_.fetchPlayers(user)))

    def boot() = Async {
      acceptor.bind()
      logger.debug(s"listening on $realmManagerPort")
      logger.info("successfully booted")
    }

    def kill() = Async {
      acceptor.unbind()
      for (s <- acceptor.getManagedSessions.values()) s.close(true).await()
      acceptor.dispose()
      logger.info("successfully killed")
    }
  }

  class RealmServerImpl(val address: String, val port: Int, session: IoSession) extends RealmServer {
    var infosOption: Option[Server] = None
    def infos = infosOption.get

    def isAvailable = infos.state == ServerState.online

    def fetchPlayers(user: User) = ???
    def grantAccess(user: User, ticket: String) = ???
  }

  class RealmManagerLoggingImpl extends IoFilterAdapter with Logging {
    override def messageSent(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) {
      logger.debug(s"send ${writeRequest.getMessage} to ${session.getRemoteAddress}")
      nextFilter.messageSent(session, writeRequest)
    }

    override def messageReceived(nextFilter: NextFilter, session: IoSession, message: Any) {
      logger.debug(s"receive $message from ${session.getRemoteAddress}")
      nextFilter.messageReceived(session, message)
    }
  }

  class RealmManagerHandlerImpl extends IoHandlerAdapter {
    override def sessionOpened(session: IoSession) {

    }

    override def sessionClosed(session: IoSession) {

    }

    override def messageReceived(session: IoSession, message: Any) {
      val realm = Option(session.getAttribute(realmServerSessionAttributeKey).asInstanceOf[RealmServerImpl])
    }
  }
}

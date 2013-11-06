package org.photon.login

import com.twitter.util.{Promise, Future}
import org.photon.protocol.dofus.login.{PlayersOfServer, Server, ServerState}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.mutable
import org.apache.mina.transport.socket.nio.{NioProcessor, NioSocketAcceptor}
import org.photon.common.{Event, Async}
import java.net.InetSocketAddress
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.write.WriteRequest
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.photon.protocol.photon._
import scala.Some

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent with ExecutorComponent with ServiceManagerComponent =>
  import scala.collection.JavaConversions._
  import MinaConversion._

  val realmManager = new RealmManagerImpl
  services += realmManager

  implicit private val realmServerSessionAttributeKey = Attr[RealmServerImpl]

  class RealmManagerImpl extends RealmManager with Logging {
    val servers = mutable.Map.empty[Int, RealmServerImpl]

    val acceptor = new NioSocketAcceptor(executor, new NioProcessor(executor))
    acceptor.setDefaultLocalAddress(new InetSocketAddress(realmManagerPort))
    acceptor.setHandler(new RealmManagerHandlerImpl)

    acceptor.getFilterChain.addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory))
    acceptor.getFilterChain.addLast("logging", new RealmManagerLoggingImpl)

    val updated = Event.newEvent
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
    private[RealmManagerComponentImpl] val playerListRequests = mutable.Map.empty[Long, Promise[PlayersOfServer]]
    private[RealmManagerComponentImpl] val grantAccessRequests = mutable.Map.empty[Long, Promise[Unit]]

    var infosOption: Option[Server] = None
    def infos = infosOption.get

    def isAvailable = infos.state == ServerState.online

    def fetchPlayers(user: User) = playerListRequests.get(user.id) getOrElse {
      val p = Promise[PlayersOfServer]
      playerListRequests(user.id) = p
      (session ! PlayerListMessage(user.id)) flatMap { _ => p }
    }

    def grantAccess(user: User, ticket: String) = grantAccessRequests.get(user.id) getOrElse {
      val userInfos = UserInfos(
        user.id,
        user.nickname,
        user.secretAnswer,
        user.subscriptionEnd
      )

      val p = Promise[Unit]
      grantAccessRequests(user.id) = p
      (session ! GrantAccessMessage(userInfos, ticket)) flatMap { _ => p }
    }
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
      handle(Connect(session))
    }

    override def sessionClosed(session: IoSession) {
      handle(Disconnect(session))
    }

    override def messageReceived(session: IoSession, message: Any) {
      handle(Message(session, message))
    }
  }

  protected sealed trait Req
  protected case class Connect(s: IoSession) extends Req
  protected case class Disconnect(s: IoSession) extends Req
  protected case class Message(s: IoSession, o: Any) extends Req
  protected type RealmServerHandler = PartialFunction[Req, Future[_]]

  protected def handle: RealmServerHandler = {
    case Connect(s) => s ! HelloConnectMessage()

    case Disconnect(s) => Future.Done

    case Message(s, AuthMessage()) => ???

    case Message(s, InfosUpdateMessage(infos)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.infosOption = Some(infos)
      realmManager.updated(realm) flatMap { _ => s ! Ack }

    case Message(s, StateUpdateMessage(state)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.infosOption = Some(realm.infos.copy(state = state))
      realmManager.updated(realm) flatMap { _ => s ! Ack }

    case Message(s, PlayerListSuccessMessage(userId, nplayers)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.playerListRequests.remove(userId) foreach (_ setValue PlayersOfServer(realm.infos.id, nplayers))
      Future.Done

    case Message(s, PlayerListErrorMessage(userId)) =>
      s.attr[RealmServerImpl].get.playerListRequests
          .remove(userId) foreach (_ setException PlayerListException())
      Future.Done

    case Message(s, GrantAccessSuccessMessage(userId)) =>
      s.attr[RealmServerImpl].get.grantAccessRequests
          .remove(userId) foreach (_.setDone())
      Future.Done

    case Message(s, GrantAccessErrorMessage(userId)) =>
      s.attr[RealmServerImpl].get.grantAccessRequests
          .remove(userId) foreach (_ setException GrantAccessException())
      Future.Done
  }
}

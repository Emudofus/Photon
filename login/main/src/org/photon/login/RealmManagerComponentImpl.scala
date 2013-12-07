package org.photon.login

import com.twitter.util._
import org.photon.protocol.dofus.login.ServerState
import com.typesafe.scalalogging.slf4j.{Logger, Logging}
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
import org.photon.protocol.photon._
import org.photon.common.components.{ServiceManagerComponent, ExecutorComponent}
import java.security.{MessageDigest, SecureRandom}
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory
import org.photon.protocol.photon.InfosUpdateMessage
import org.photon.protocol.photon.PlayerListErrorMessage
import scala.Some
import org.photon.protocol.photon.GrantAccessMessage
import org.photon.protocol.photon.StateUpdateMessage
import org.photon.protocol.dofus.login.Server
import org.photon.protocol.photon.GrantAccessSuccessMessage
import org.photon.protocol.dofus.login.PlayersOfServer
import org.photon.protocol.photon.PublicIdentityMessage
import org.photon.protocol.photon.AuthMessage
import org.photon.protocol.photon.GrantAccessErrorMessage
import org.photon.protocol.photon.PlayerListSuccessMessage
import org.photon.protocol.photon.PlayerListMessage
import com.twitter.util.Throw
import org.photon.protocol.photon.HelloConnectMessage
import org.photon.protocol.photon.UserInfos

trait RealmManagerComponentImpl extends RealmManagerComponent {
  self: ConfigurationComponent with ExecutorComponent with ServiceManagerComponent =>
  import scala.collection.JavaConversions._
  import MinaConversion._

  private val logger = Logger(LoggerFactory getLogger classOf[RealmManagerComponentImpl])

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

  class RealmServerImpl(session: IoSession) extends RealmServer {
    private[RealmManagerComponentImpl] val playerListRequests = mutable.Map.empty[Long, Promise[PlayersOfServer]]
    private[RealmManagerComponentImpl] val grantAccessRequests = mutable.Map.empty[Long, Promise[Unit]]

    var infosOption: Option[Server] = None
    def infos = infosOption.get
    
    var identity = PublicIdentity()

    def isAvailable = infos.state == ServerState.online
    
    def notifyUpdated(): Unit = realmManager.emit('updated, this)

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

  def longToBytes(long: Long): Array[Byte] = (ByteBuffer allocate 8).putLong(long).array

  val rand = new SecureRandom(longToBytes(System.nanoTime))

  def nextBytes(len: Int = realmManagerSaltLen): Array[Byte] = {
    val salt = Array.ofDim[Byte](len)
    rand.nextBytes(salt)
    salt
  }

  def expectedCredentials(salt: Array[Byte]) = realmManagerPasswordDigest.digest(realmManagerPassword ++ salt)

  def auth(session: IoSession, id: Int, credentials: Array[Byte], salt: Array[Byte]): Future[Unit] = Async {
    if (!MessageDigest.isEqual(credentials, expectedCredentials(salt))) {
      throw RealmAuthException(reason = "invalid credentials")
    }

    if (realmManager.servers.contains(id)) {
      throw RealmAuthException(reason = s"realm $id already authenticated")
    }

    val realm = new RealmServerImpl(session)
    realmManager.servers(id) = realm
    session.attr(Some(realm))
  }

  protected def handle: RealmServerHandler = {
    case Connect(s) => s ! HelloConnectMessage(nextBytes())

    case Disconnect(s) =>
      realmManager.servers -= s.attr[RealmServerImpl].get.infos.id
      realmManager.emit('updated)
      Future.Done

    case Message(s, AuthMessage(id, credentials, salt)) =>
      auth(s, id, credentials, salt) transform {
        case Return(_) =>
          logger.info(s"successfully logged realm $id")
          s ! AuthSuccessMessage

        case Throw(RealmAuthException(reason, nested)) =>
          logger.error(s"cannot auth realm $id because `$reason'", nested)
          s ! AuthErrorMessage
      }

    case Message(s, PublicIdentityMessage(newAddress, newPort)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.identity = realm.identity.copy(address = newAddress, port = newPort)
      s ! Ack

    case Message(s, InfosUpdateMessage(infos)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.infosOption = Some(infos)
      realm.notifyUpdated()
      s ! Ack

    case Message(s, StateUpdateMessage(state)) =>
      val realm = s.attr[RealmServerImpl].get
      realm.infosOption = Some(realm.infos.copy(state = state))
      realm.notifyUpdated()
      s ! Ack

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

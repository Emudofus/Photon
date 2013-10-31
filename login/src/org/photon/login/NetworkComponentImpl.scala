package org.photon.login

import io.netty.channel._
import com.twitter.util.Future
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress
import scala.collection.mutable.ArrayBuffer
import io.netty.channel.socket.SocketChannel
import io.netty.util.AttributeKey
import io.netty.handler.codec.{Delimiters, DelimiterBasedFrameDecoder}
import org.photon.protocol.{Message, DofusDeserializer, DofusMessage, DofusProtocol}
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import com.typesafe.scalalogging.slf4j.Logging
import org.photon.common.Strings
import java.util.Random
import org.photon.protocol.login.{AuthenticationMessage, VersionMessage}
import io.netty.buffer.Unpooled

trait NetworkComponentImpl extends NetworkComponent { self: ConfigurationComponent with HandlerComponent =>
  import NettyConversion._

  val networkService = new NetworkServiceImpl


  class NetworkServiceImpl extends NetworkService with Logging {
    type NetworkSession = NetworkSessionImpl

    private val server = new ServerBootstrap()
      .group(new NioEventLoopGroup, new NioEventLoopGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new NetworkHandlerImpl)
      .register().sync().channel


    private[NetworkComponentImpl] val sessionAttr = new AttributeKey[NetworkSession]("photon.network.login.session")
    private[NetworkComponentImpl] val sessions = ArrayBuffer.empty[NetworkSession]


    def connected = sessions.toSeq

    def boot(): Future[NetworkService] = server.bind(new InetSocketAddress(networkPort)) toTw this onSuccess { _ =>
      logger.info(s"successfully bound on $networkPort")
    } onFailure { t =>
      logger.error(s"got an error while binding on $networkPort", t)
    }


    def kill(): Future[NetworkService] = server.close() toTw this onSuccess { _ =>
      logger.info("successfully closed")
    } onFailure { t =>
      logger.error("got an error while closing", t)
    }
  }


  class NetworkSessionImpl(channel: SocketChannel, val ticket: String) extends NetworkSession {
    import NetworkSession._
    type NetworkService = networkService.type

    private val closePromise = channel.closeFuture() toTw this

    var state: State = VersionCheckState

    def service = networkService
    def closeFuture = closePromise
    def remoteAddress = channel.remoteAddress()

    def write(o: Any): Future[NetworkSession] = channel write o toTw this
    def flush(): Future[NetworkSession] = {
      channel.flush()
      Future(this)
    }
    def close(): Future[NetworkSession] = {
      channel.close()
      closeFuture
    }

    override def !(o: Any): Future[NetworkSession] = channel writeAndFlush o toTw this
  }


  class NetworkHandlerImpl extends ChannelInitializer[SocketChannel] {
    implicit val random = new Random(System.nanoTime)

    def initChannel(ch: SocketChannel) {
      val session = new NetworkSessionImpl(ch, Strings.next(32))
      ch.attr(networkService.sessionAttr).set(session)

      ch.pipeline
        .addLast(new DelimiterBasedFrameDecoder(64, Unpooled.wrappedBuffer(Array[Byte]('\n', '\0'))), new StringDecoder(networkCharset))
        .addLast(new StringEncoder(networkCharset))
        .addLast("codec", new NetworkCodecImpl)
        .addLast("logger", new NetworkLoggerImpl)
        .addLast("dispatcher", new NetworkDispatcherImpl)
    }
  }
  
  
  class NetworkLoggerImpl extends ChannelDuplexHandler with Logging {
    override def channelRegistered(ctx: ChannelHandlerContext) {
      logger.debug(s"connect ${ctx.channel.remoteAddress}")

      super.channelRegistered(ctx)
    }

    override def channelUnregistered(ctx: ChannelHandlerContext) {
      super.channelUnregistered(ctx)

      logger.debug(s"disconnect ${ctx.channel.remoteAddress}")
    }
    
    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      logger.debug(s"failure ${ctx.channel.remoteAddress}", cause)

      super.exceptionCaught(ctx, cause)
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      logger.debug(msg match {
        case msg: DofusMessage => s"receive $msg from ${ctx.channel.remoteAddress}"
        case other => s"receive $other from ${ctx.channel.remoteAddress}"
      })

      super.channelRead(ctx, msg)
    }

    override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
      super.write(ctx, msg, promise)

      logger.debug(msg match {
        case msg: DofusMessage => s"send $msg to ${ctx.channel.remoteAddress}"
        case other => s"send $other to ${ctx.channel.remoteAddress}"
      })
    }
  }


  class NetworkDecoderImpl extends ChannelInboundHandlerAdapter with Logging {
    import NetworkSession.{VersionCheckState, AuthenticationState, ServerSelectionState}

    override def channelRead(ctx: ChannelHandlerContext, o: Any) {
      val session = ctx.channel.attr(networkService.sessionAttr).get

      o match {
        case data: String =>
          session.state match {
            case VersionCheckState =>
              ctx.fireChannelRead(VersionMessage.deserialize(data))

            case AuthenticationState =>
              ctx.fireChannelRead(AuthenticationMessage.deserialize(data))

            case ServerSelectionState =>
              val (opcode, rest) = data.splitAt(2)

              DofusProtocol.deserializers.get(opcode) match {
                case Some(d) => ctx.fireChannelRead(d.deserialize(rest))
                case None => logger.trace(s"unknown opcode $opcode")
              }
          }
      }
    }
  }

  class NetworkEncoderImpl extends ChannelOutboundHandlerAdapter {
    override def write(ctx: ChannelHandlerContext, o: Any, promise: ChannelPromise) {
      o match {
        case msg: DofusMessage =>
          val builder = StringBuilder.newBuilder
          builder ++= msg.definition.opcode
          msg.serialize(builder)
          builder += '\u0000'

          ctx.write(builder.toString(), promise)
      }
    }
  }

  class NetworkCodecImpl extends CombinedChannelDuplexHandler(new NetworkDecoderImpl, new NetworkEncoderImpl)


  class NetworkDispatcherImpl extends ChannelInboundHandlerAdapter {
    import HandlerComponent._

    override def channelRegistered(ctx: ChannelHandlerContext) {
      val session = ctx.channel.attr(networkService.sessionAttr).get
      networkHandler(Connect(session))
    }

    override def channelUnregistered(ctx: ChannelHandlerContext) {
      val session = ctx.channel.attr(networkService.sessionAttr).get
      networkHandler(Disconnect(session))
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val session = ctx.channel.attr(networkService.sessionAttr).get
      networkHandler(Message(session, msg))
    }
  }
}
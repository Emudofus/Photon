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
import org.photon.protocol.{DofusMessage, DofusProtocol}
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import com.typesafe.scalalogging.slf4j.Logging

trait NetworkComponentImpl extends NetworkComponent with Logging { self: ConfigurationComponent with HandlerComponent =>
  import NettyConversion._

  val networkService = new NetworkServiceImpl


  class NetworkServiceImpl extends NetworkService {
    type NetworkSession = NetworkSessionImpl

    private val server = new ServerBootstrap()
      .group(new NioEventLoopGroup, new NioEventLoopGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new NetworkHandlerImpl)
      .register().sync().channel


    private[NetworkComponentImpl] val sessionAttr = new AttributeKey[NetworkSession]("photon.network.login.session")
    private[NetworkComponentImpl] val sessions = ArrayBuffer.empty[NetworkSession]


    def connected = sessions.toSeq
    def boot(): TFut = server.bind(new InetSocketAddress(networkPort)) toTw(this)
    def kill(): TFut = server.close() toTw(this)
  }


  class NetworkSessionImpl(channel: SocketChannel) extends NetworkSession {
    type NetworkService = networkService.type

    private val closePromise = channel.closeFuture() toTw(this)

    def service = networkService
    def closeFuture: TFut = closePromise


    def write(o: Any): TFut = channel.write(o) toTw(this)
    def flush(): TFut = {
      channel.flush()
      Future(this)
    }
    def close(): TFut = {
      channel.close()
      closeFuture
    }

    override def !(o: Any): TFut = channel.writeAndFlush(o) toTw(this)
  }


  class NetworkHandlerImpl extends ChannelInitializer[SocketChannel] {
    def initChannel(ch: SocketChannel) {
      val session = new NetworkSessionImpl(ch)
      ch.attr(networkService.sessionAttr).set(session)

      ch.pipeline
        .addLast(new DelimiterBasedFrameDecoder(64, Delimiters.nulDelimiter(): _*), new StringDecoder(networkCharset))
        .addLast(new StringEncoder(networkCharset))
        .addLast("codec", new NetworkCodecImpl)
        .addLast("logger", new NetworkLoggerImpl)
        .addLast("dispatcher", new NetworkDispatcherImpl)
    }
  }
  
  
  class NetworkLoggerImpl extends ChannelDuplexHandler {
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
        case msg: DofusMessage => s"receive ${msg.definition.opcode} from ${ctx.channel.remoteAddress}"
        case other => s"receive $other from ${ctx.channel.remoteAddress}"
      })

      super.channelRead(ctx, msg)
    }

    override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
      super.write(ctx, msg, promise)

      logger.debug(msg match {
        case msg: DofusMessage => s"send ${msg.definition.opcode} to ${ctx.channel.remoteAddress}"
        case other => s"send $other to ${ctx.channel.remoteAddress}"
      })
    }
  }


  class NetworkDecoderImpl extends ChannelInboundHandlerAdapter {
    override def channelRead(ctx: ChannelHandlerContext, o: Any) {
      o match {
        case data: String =>
          val msg = for {
            (opcode, rest) <- data.splitAt(2)
            deserializer <- DofusProtocol.deserializers.get(opcode)
          } yield deserializer.deserialize(rest)

          ctx.fireChannelRead(msg)
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

  class NetworkCodecImpl extends CombinedChannelDuplexHandler[NetworkDecoderImpl, NetworkEncoderImpl]


  class NetworkDispatcherImpl extends ChannelInboundHandlerAdapter {
    import HandlerComponent._

    override def channelRegistered(implicit ctx: ChannelHandlerContext) {
      val session = ctx.attr(networkService.sessionAttr).get
      networkHandler(Connect(session))
    }

    override def channelUnregistered(ctx: ChannelHandlerContext) {
      val session = ctx.attr(networkService.sessionAttr).get
      networkHandler(Disconnect(session))
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      val session = ctx.attr(networkService.sessionAttr).get
      networkHandler(Message(session, msg))
    }
  }
}
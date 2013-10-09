package org.photon.protocol

// PoC - Context
class Context { def !(msg: Message) }
// end PoC - Context

class PongMessage extends Message

// PoC - Handlers
trait Handler {
  def call(ctx: Context, msg: String)
}
object PingHandler extends Handler {
  def call(ctx: Context, msg: String) {
    ctx ! new PongMessage
  }
}
// end PoC - Handlers

object PacketHandler {
  val handlers = Map[String, Handler](
    "PI" -> PingHandler
  )

  case class PacketDispatcher(ctx: Context) {
    def handle(msg: String) {
      for (handler <- handlers get msg.substr(0, 2))
        handler(ctx, msg substr 2)
    }
  }
}
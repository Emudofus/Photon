package org.photon.protocol.dofus.account

import org.photon.protocol.dofus._

case class Player(
  id: Long,
  name: String,
  level: Short,
  skin: Short,
  color1: Int,
  color2: Int,
  color3: Int,
  accessories: Seq[Int],
  merchant: Boolean,
  serverId: Int,
  dead: Boolean,
  deathCount: Int,
  levelMax: Int
) extends StringSerializable {

  def serialize(out: Player#Out) {
    out append id append ';'
    out append name append ';'
    out append level append ';'
    out append skin append ';'
    out append hex(color1) append ';'
    out append hex(color2) append ';'
    out append hex(color3) append ';'
    accessories.addString(out, "", ",", ";")
    out append btoi(merchant) append ';'
    out append serverId append ';'
    out append btoi(dead) append ';'
    out append deathCount append ';'
    out append levelMax append ';'
  }
}

package org.photon.common

import java.util.Random

object Strings {
  val alphanum = 'a' to 'z'
  def rand(implicit rnd: Random): Char = alphanum(rnd.nextInt(alphanum.length))
  def next(n: Int)(implicit rnd: Random): String = (1 to n).foldLeft("") { (res, _) => res + rand }


  def decryptDofusPassword(input: String, key: String): String = {
    val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
    val len = alphabet.length

    val decrypted = StringBuilder.newBuilder
    decrypted.sizeHint(input.length / 2)

    for (i <- Range(0, input.length, step = 2)) {
      val PKey = key(i / 2)

      val ANB = alphabet indexOf input(i)
      val ANB2 = alphabet indexOf input(i + 1)

      var APass = ANB + len - PKey
      if (APass < 0) APass += 64
      APass <<= 4

      var AKey = ANB2 + len - PKey
      if (AKey < 0) AKey += 64

      val PPass = (APass + AKey).toChar
      decrypted += PPass
    }

    decrypted.result
  }
}

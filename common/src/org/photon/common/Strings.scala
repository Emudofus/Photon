package org.photon.common

import java.util.Random

object Strings {
  val alphanum = 'a' to 'z'
  def rand(implicit rnd: Random): Char = alphanum(rnd.nextInt(alphanum.length))
  def next(n: Int)(implicit rnd: Random): String = (1 to n).foldLeft("") { (res, _) => res + rand }
}

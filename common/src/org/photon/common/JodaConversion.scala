package org.photon.common

import org.joda.time.Instant
import java.sql.Timestamp

object JodaConversion {
  implicit def timestamp2instant(t: java.sql.Timestamp) = new Instant(t.getTime)
  implicit def instant2timestamp(i: org.joda.time.Instant) = new Timestamp(i.getMillis)
}

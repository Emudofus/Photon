package org.photon.static_data.converter.ancestra

import org.photon.static_data.converter.Converter
import org.photon.static_data.ItemData
import scala.collection.generic.CanBuildFrom
import com.twitter.util.Future
import org.photon.common.persist.Async
import java.sql.Connection
import java.util.concurrent.Executor

class ItemDataConverter(connection: Connection)(implicit e: Executor) extends Converter[ItemData] {
  import org.photon.common.persist.Connections._

  def convert[Result](implicit cbf: CanBuildFrom[_, ItemData, Result]): Future[Result] = Async {
    connection.prepare("SELECT * FROM item_template") { s =>
      s.executeQuery() map { rs =>
        new ItemData(
          rs.getLong("id"),
          None // TODO
        )
      }
    }
  }
}

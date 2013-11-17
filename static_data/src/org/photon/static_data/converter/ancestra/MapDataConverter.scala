package org.photon.static_data.converter.ancestra

import org.photon.static_data.converter.Converter
import org.photon.static_data.MapData
import scala.collection.generic.CanBuildFrom
import com.twitter.util.Future
import java.sql.Connection
import org.photon.common.persist.Async
import java.util.concurrent.Executor

class MapDataConverter(connection: Connection)(implicit e: Executor) extends Converter[MapData] {
  import org.photon.common.persist.Connections._

  def convert[Result](implicit cbf: CanBuildFrom[_, MapData, Result]): Future[Result] = Async {
    connection.prepare("SELECT * FROM maps") { s =>
      s.executeQuery() map { rs =>
        new MapData(
          rs.getInt("id"),
          rs.getString("mappos").split("", 2) match {
            case Array(x, y) => new MapData.Position(x.toInt, y.toInt)
          },
          rs.getString("key"),
          rs.getString("date"),
          rs.getString("mapData")
        )
      }
    }
  }
}

package org.photon.staticdata.converter.ancestra

import org.photon.staticdata.converter.Loader
import org.photon.staticdata.{MapTrigger, MapData}
import java.sql.{ResultSet, Connection}
import java.util.concurrent.Executor
import org.photon.common.persist.Async
import scala.collection.mutable

class MapDataLoader(connection: Connection)(implicit e: Executor) extends Loader[MapData] {
	import org.photon.common.persist.Connections.RichConnection

	implicit def rset2stream(rset: ResultSet): Stream[ResultSet] =
		Stream.continually(rset).takeWhile(_.next())

	def load() = Async {
		val mapBuilds = connection.statement("select * from maps") {
			_.getResultSet.map[(Int, MapData.Builder), mutable.Map[Int, MapData.Builder]] { rset =>
				val build = MapData.newBuilder
					.withId(rset getInt "id")

				(build.id, build)
			}
		}

		connection.statement("select * from map_triggers") {
			_.getResultSet foreach { rset =>
				var origin = mapBuilds(rset.getInt("map_id"))
				var originCell = origin.cells.apply(rset.getInt("cell_id") - 1)
				val target = mapBuilds(rset.getInt("next_map_id"))
				val targetCell = target.cells.apply(rset.getInt("next_cell_id") - 1)

				originCell = originCell.withTrigger(MapTrigger.newBuilder
					.withOrigin(origin)
					.withOriginCell(originCell)
					.withTarget(target)
					.withTargetCell(targetCell))

				origin = origin.withCells(origin.cells.updated(originCell.id - 1, originCell))

				mapBuilds(origin.id, origin)
			}
		}

		mapBuilds.values.map(_.result).toSeq
	}
}
package org.photon.staticdata.converter.ancestra

import org.photon.staticdata.converter._
import org.photon.staticdata.{MapTrigger, MapData}
import java.sql.Connection
import java.util.concurrent.Executor
import org.photon.common.persist.Async

class MapDataLoader(connection: Connection)(implicit e: Executor) extends Loader[MapData] {
	import org.photon.common.persist.Connections.RichConnection

	def load() = Async {
		val mapBuilds = connection.statement("select * from maps") {
			_.getResultSet.map { rset =>
				val build = MapData.newBuilder
					.withId(rset getInt "id")

				(build.id, build)
			}.toMap
		}

		connection.statement("select * from map_triggers") {
			_.getResultSet foreach { rset =>
				// extract triggers values
				val origin = mapBuilds(rset.getInt("map_id"))
				val originCell = origin.cells.apply(rset.getInt("cell_id") - 1)
				val target = mapBuilds(rset.getInt("next_map_id"))
				val targetCell = target.cells.apply(rset.getInt("next_cell_id") - 1)

				// build actual trigger
				originCell.withTrigger(Some(MapTrigger.newBuilder
					.withOrigin(origin)
					.withOriginCell(originCell)
					.withTarget(target)
					.withTargetCell(targetCell)))
			}
		}

		mapBuilds.values.map(_.result).toSeq
	}
}
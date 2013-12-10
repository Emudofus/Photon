package org.photon.staticdata.converter.ancestra

import org.photon.staticdata.converter._
import org.photon.staticdata._
import java.sql.Connection
import java.util.concurrent.Executor
import org.photon.common.persist.Async
import org.photon.staticdata.MapPosition
import scala.Some

class MapDataLoader(connection: Connection)(implicit e: Executor) extends Loader[MapData] {
	import org.photon.common.persist.Connections.RichConnection

	def load() = Async {
		// firstly, load maps
		val mapBuilds = connection.statement("select * from maps") {
			_.getResultSet.map { rset =>
				val build = MapData.newBuilder
					.withId(rset getInt "id")
					.withWidth(rset getShort "width")
					.withHeight(rset getShort "heigth")
					.withDate(rset.getString("date").getBytes)
					.withKey(rset.getString("key").getBytes)
					.withPos(rset.getString("mappos").split(",", 2) match {
						case Array(x, y) =>	MapPosition(x.toInt, y.toInt)
					})
					.withCells(rset.getString("mapData").sliding(10).withIndexes.map { case (i, data) =>
						val hashCodes = data.map(MapDataLoader.alphabet.indexOf(_))

						MapCell.newBuilder
							.withId(i + 1)
							.withLos(hashCodes(0) & 1 == 1)
							.withGroundLevel(hashCodes(1) & 15)
							.withMovementType(MovementType.of(hashCodes(2) & 56 >> 3))
							.withGroundSlope(hasshCodes(4) & 60 >> 2)
					})

				(build.id, build)
			}.toMap
		}

		// then, load triggers
		connection.statement("select * from scripted_cells where EventID=1 order by MapID asc") {
			_.getResultSet foreach { rset =>
				// extract triggers values
				val origin = mapBuilds(rset.getInt("MapID"))
				val originCell = origin.cells.apply(rset.getInt("CellID") - 1)

				val (target, targetCell) = rset.getString("ActionArgs").split(",", 2) match {
					case Array(t, tc) =>
						val tt = mapBuilds(t.toInt)
						val ttc = tt.cells.apply(tc.toInt - 1)
						(tt, ttc)
				}

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

object MapDataLoader {
	val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
}
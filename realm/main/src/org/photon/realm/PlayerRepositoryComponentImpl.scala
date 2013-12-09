package org.photon.realm

import org.photon.common.components.{ServiceManagerComponent, Service, ExecutorComponent, DatabaseComponent}
import java.sql.{PreparedStatement, ResultSet}
import org.photon.common.persist._
import com.twitter.util.Future
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logger
import org.photon.staticdata.MapData
import org.photon.common.persist.ModelState

trait PlayerRepositoryComponentImpl extends PlayerRepositoryComponent {
	self: DatabaseComponent
		with ExecutorComponent
		with ServiceManagerComponent
		with ConfigurationComponent
		with DataRepositoryComponent =>
	import org.photon.common.persist.Parameters._
	import org.photon.common.persist.Connections._

	implicit val playerRepository = new PlayerRepositoryImpl
	services += playerRepository

	private val logger = Logger(LoggerFactory getLogger classOf[PlayerRepositoryComponentImpl])
	private val maxPlayersPerUser = config.getInt("photon.realm.max-players-per-user")
	private lazy val startMap = dataRepository[MapData](config.getInt("photon.realm.start-map")).get
	private lazy val startCell = startMap.cells(config.getInt("photon.realm.start-cell") - 1)

	class PlayerRepositoryImpl extends BaseRepository[Player](self.database)
		with Caching[Player]
		with PlayerRepository
		with Service
	{
		lazy val table = "players"
		lazy val pkColumns = Seq("id")
		lazy val columns = Seq("owner_id", "name", "breed", "gender", "level", "skin", "color1", "color2", "color3", "current_map", "current_cell")

		def boot() = hydrate() onSuccess { _ =>
			logger.info(s"${cache.size} players loaded")
		} onFailure {
			case ex: Exception => logger.error("can't load players", ex)
		}

		def kill() = Async {
			cache.values.foreach(persist)
			logger.info(s"${cache.size} players saved")
		}
		
		def buildLocation(mapId: Int, cellId: Int): PlayerLocation = {
			val map = dataRepository[MapData](mapId).getOrElse(throw new IllegalStateException(s"unknown map $mapId"))
			val cell = map.cells.applyOrElse(cellId - 1, throw new IllegalStateException(s"unknown cell $cellId on map $mapId"))
			
			new PlayerLocation(map, cell)
		}

		def buildModel(rset: ResultSet) = Player(
			rset.getLong("id"),
			rset.getLong("ownerId"),
			rset.getString("name"),
			rset.getShort("breed"),
			rset.getBoolean("gender"),
			rset.getShort("level"),
			new PlayerAppearence(rset.getShort("skin"), Colors(rset.getInt("color1"), rset.getInt("color2"), rset.getInt("color3"))),
			buildLocation(rset.getInt("current_map"), rset.getInt("current_cell")),
			ModelState.Persisted
		)

		def bindParams(ps: PreparedStatement, player: Player)(implicit index: Incremented[Int]) {
			ps.set(player.ownerId)
			ps.set(player.name)
			ps.set(player.breed)
			ps.set(player.gender)
			ps.set(player.level)
			ps.set(player.appearence.skin)
			ps.set(player.appearence.colors.first)
			ps.set(player.appearence.colors.second)
			ps.set(player.appearence.colors.third)
			ps.set(player.location.map.id)
			ps.set(player.location.cell.id)
		}

		def setPersisted(o: Player, newId: Long) = o.copy(id = newId, state = ModelState.Persisted)
		def setRemoved(o: Player) = o.copy(state = ModelState.Removed)

		def findByName(name: String) = Future(find(x => x.name == name).get)
		def findByOwner(ownerId: Long) = Future(filter(x => x.ownerId == ownerId).toSeq)

		def create(ownerId: Long, name: String, breed: Short, gender: Boolean, color1: Int, color2: Int, color3: Int) = {
			if (find(x => x.name == name).isDefined) {
				Future.exception(ExistingPlayerNameException())
			} else if (filter(x => x.ownerId == ownerId).size >= maxPlayersPerUser) {
				Future.exception(UnavailableSpaceException())
			} else {
				persist(Player(
					-1L,
					ownerId,
					name,
					breed,
					gender,
					1,
					new PlayerAppearence(
						(10 * breed + (if (gender) 1 else 0)).toShort,
						Colors(
							color1,
							color2,
							color3
						)
					),
					new PlayerLocation(startMap, startCell)
				))
			}
		}
	}
}

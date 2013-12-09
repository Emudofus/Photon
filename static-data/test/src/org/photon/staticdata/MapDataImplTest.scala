package org.photon.staticdata

import org.scalatest.{ShouldMatchers, FreeSpec}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class MapDataImplTest extends FreeSpec with ShouldMatchers {
	trait Fixture {
		val mapper = new ObjectMapper with ScalaObjectMapper
		mapper.registerModule(DefaultScalaModule)
	}

	"A MapData" - {
		import MapData.newBuilder

		"should be serializable" in new Fixture {
			val map = newBuilder
				.withId(1)
				.withPos(MapPosition(42, 24))
				.withWidth(42).withHeight(24)
				.withSubareaId(None)
				.withCells(Seq.empty)
				.withKey("such key".getBytes)
				.withDate("such date".getBytes)
				.withPremium(premium = true)
				.result

			mapper.writeValueAsString(map) should === ("{\"id\":1,\"width\":42,\"height\":24,\"cells\":[],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true,\"pos\":[42,24],\"subareaId\":null}")
		}

		"should be deserializable" in new Fixture {
			val map: MapData = mapper.readValue[MapDataImpl]("{\"id\":1,\"width\":42,\"height\":24,\"subareaId\":null,\"cells\":[],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true,\"pos\":[42,24]}")

			map.id should === (1)
			map.pos.x should === (42)
			map.pos.y should === (24)
			map.width should === (42)
			map.height should === (24)
			map.subareaId should be ('empty)
			map.cells should be ('empty)
			map.key should === ("such key".getBytes)
			map.date should === ("such date".getBytes)
			map.premium should be (true)
		}
	}

	"A MapCell" - {
		import MapCell.newBuilder

		"should be serializable" in new Fixture {
			val cell = newBuilder
				.withId(42)
				.withMap(null)
				.withLos(true)
				.withGroundLevel(42).withGroundSlope(24)
				.withMovementType(MovementType.Walkable)
				.withInteractiveObject(Some(84))
				.result

			mapper.writeValueAsString(cell) should === ("{\"id\":42,\"los\":true,\"groundLevel\":42,\"groundSlope\":24,\"movementType\":3,\"interactiveObject\":84}")
		}

		"should be deserializable" in new Fixture {
			val cell: MapCell = mapper.readValue[MapDataImpl.Cell]("{\"id\":42,\"los\":true,\"groundLevel\":42,\"groundSlope\":24,\"movementType\":3,\"interactiveObject\":84}")

			cell.id should === (42)
			cell.map should be (null)
			cell.los should be (true)
			cell.groundLevel should === (42)
			cell.groundSlope should === (24)
			cell.movementType should === (MovementType.Walkable)
			cell.interactiveObject should not be 'empty
			cell.interactiveObject.get should === (84)
		}
	}

	"A MapData that owns MapCell" - {
		"should be serializable" in new Fixture {
			val mapBuild = MapData.newBuilder
				.withId(1)
				.withPos(MapPosition(42, 24))
				.withWidth(42).withHeight(24)
				.withSubareaId(None)
				.withKey("such key".getBytes)
				.withDate("such date".getBytes)
				.withPremium(premium = true)

			val cell = MapCell.newBuilder
				.withId(42)
				.withMap(mapBuild.lazyResult)
				.withLos(true)
				.withGroundLevel(42).withGroundSlope(24)
				.withMovementType(MovementType.Walkable)
				.withInteractiveObject(Some(84))
				.result

			val map = mapBuild
				.withCells(Seq(cell))
				.result

			mapper.writeValueAsString(map) should === ("{\"id\":1,\"width\":42,\"height\":24,\"cells\":[{\"id\":42,\"los\":true,\"groundLevel\":42,\"groundSlope\":24,\"movementType\":3,\"interactiveObject\":84}],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true,\"pos\":[42,24],\"subareaId\":null}")
		}

		"should be deserializable" in new Fixture {
			val map: MapData = mapper.readValue[MapDataImpl]("{\"id\":1,\"width\":42,\"height\":24,\"cells\":[{\"id\":42,\"los\":true,\"groundLevel\":42,\"groundSlope\":24,\"movementType\":3,\"interactiveObject\":84}],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true,\"pos\":[42,24],\"subareaId\":null}")

			map.id should === (1)
			map.pos.x should === (42)
			map.pos.y should === (24)
			map.width should === (42)
			map.height should === (24)
			map.subareaId should be ('empty)
			map.key should === ("such key".getBytes)
			map.date should === ("such date".getBytes)
			map.premium should be (true)

			map.cells should have size (1)
			val cell = map.cells.head
			cell.id should === (42)
			cell.map should === (map)
			cell.los should be (true)
			cell.groundLevel should === (42)
			cell.groundSlope should === (24)
			cell.movementType should === (MovementType.Walkable)
			cell.interactiveObject should not be 'empty
			cell.interactiveObject.get should === (84)
		}
	}
}

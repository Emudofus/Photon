package org.photon.staticdata

import org.scalatest.{ShouldMatchers, FreeSpec}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.photon.jackson.flatjson.FlatJsonModule

class MapDataImplTest extends FreeSpec with ShouldMatchers {
	trait Fixture {
		val mapper = new ObjectMapper with ScalaObjectMapper
		mapper.registerModule(DefaultScalaModule)
		mapper.registerModule(new FlatJsonModule)
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

			mapper.writeValueAsString(map) should === ("{\"id\":1,\"pos\":[42,24],\"width\":42,\"height\":24,\"subareaId\":null,\"cells\":[],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true}")
		}

		"should be deserializable" in new Fixture {
			val map: MapData = mapper.readValue[MapDataImpl]("{\"id\":1,\"pos\":[42,24],\"width\":42,\"height\":24,\"subareaId\":null,\"cells\":[],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true}")

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

			mapper.writeValueAsString(cell) should === ("{\"id\":42,\"los\":true,\"groundLevel\":42,\"movementType\":3,\"groundSlope\":24,\"interactiveObject\":84,\"trigger\":null}")
		}

		"should be deserializable" in new Fixture {
			val cell: MapCell = mapper.readValue[MapDataImpl.Cell]("{\"id\":42,\"los\":true,\"groundLevel\":42,\"movementType\":3,\"groundSlope\":24,\"interactiveObject\":84,\"trigger\":null}")

			cell.id should === (42)
			cell.map should be (null)
			cell.los should be (true)
			cell.groundLevel should === (42)
			cell.groundSlope should === (24)
			cell.movementType should === (MovementType.Walkable)
			cell.interactiveObject should not be 'empty
			cell.interactiveObject.get should === (84)
			cell.trigger should be ('empty)
		}
	}

	"A MapTrigger" - {
		"should be serializable" in new Fixture {
			val trigger = MapTrigger.newBuilder
				.withOrigin(MapData.newBuilder.withId(42))
				.withOriginCell(MapCell.newBuilder.withId(24))
				.withTarget(MapData.newBuilder.withId(84))
				.withTargetCell(MapCell.newBuilder.withId(48))
				.result

			mapper.writeValueAsString(trigger) should === ("{\"origin\":42,\"originCell\":24,\"target\":84,\"targetCell\":48}")
		}

		"should be deserializable" in new Fixture {
			val (_, trigger) = mapper.readValue[(Seq[MapDataImpl], MapDataImpl.Trigger)]("[[{\"id\":42,\"cells\":[{\"id\":24}]},{\"id\":84,\"cells\":[{\"id\":48}]}],{\"origin\":42,\"originCell\":24,\"target\":84,\"targetCell\":48}]")

			trigger.origin.id should === (42)
			trigger.originCell.id should === (24)
			trigger.target.id should === (84)
			trigger.targetCell.id should === (48)
		}
	}

	"A MapData that owns MapCell that owns MapTrigger" - {
		"should be serializable" in new Fixture {
			val mapBuild = MapData.newBuilder
				.withId(1)
				.withPos(MapPosition(42, 24))
				.withWidth(42).withHeight(24)
				.withSubareaId(None)
				.withKey("such key".getBytes)
				.withDate("such date".getBytes)
				.withPremium(premium = true)

			val cellBuild = MapCell.newBuilder
				.withId(42)
				.withMap(mapBuild.lazyResult)
				.withLos(true)
				.withGroundLevel(42).withGroundSlope(24)
				.withMovementType(MovementType.Walkable)
				.withInteractiveObject(Some(84))

			cellBuild.withTrigger(Some(MapTrigger.newBuilder
				.withOrigin(mapBuild)
				.withOriginCell(cellBuild)
				.withTarget(mapBuild)
				.withTargetCell(cellBuild)))

			val map = mapBuild
				.withCells(Seq(cellBuild))
				.result

			mapper.writeValueAsString(map) should === ("{\"id\":1,\"pos\":[42,24],\"width\":42,\"height\":24,\"subareaId\":null,\"cells\":[{\"id\":42,\"los\":true,\"groundLevel\":42,\"movementType\":3,\"groundSlope\":24,\"interactiveObject\":84,\"trigger\":{\"origin\":1,\"originCell\":42,\"target\":1,\"targetCell\":42}}],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true}")
		}

		"should be deserializable" in new Fixture {
			val map: MapData = mapper.readValue[MapDataImpl]("{\"id\":1,\"pos\":[42,24],\"width\":42,\"height\":24,\"subareaId\":null,\"cells\":[{\"id\":42,\"los\":true,\"groundLevel\":42,\"movementType\":3,\"groundSlope\":24,\"interactiveObject\":84,\"trigger\":{\"origin\":1,\"originCell\":42,\"target\":1,\"targetCell\":42}}],\"key\":\"c3VjaCBrZXk=\",\"date\":\"c3VjaCBkYXRl\",\"premium\":true}")

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

			cell.trigger should not be 'empty
			cell.trigger.get.origin should === (map)
			cell.trigger.get.originCell should === (cell)
			cell.trigger.get.target should === (map)
			cell.trigger.get.targetCell should === (cell)
		}
	}
}

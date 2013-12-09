package org.photon.staticdata


sealed trait MovementType {
	def id: Int
}

object MovementType extends Enumeration {
	val Unwalkable, Door, Trigger, Walkable, Paddock, Road = new Val with MovementType

	def of(id: Int): MovementType = super.apply(id).asInstanceOf[MovementType]
}

case class MapPosition(x: Int, y: Int)

trait MapTrigger {
	val origin: MapData
	val originCell: MapCell
	val target: MapData
	val targetCell: MapCell
}

trait MapCell {
	val id: Short
	val map: MapData
	val los: Boolean
	val groundLevel: Short
	val movementType: MovementType
	val groundSlope: Short
	val interactiveObject: Option[Int]
	val trigger: Option[MapTrigger]
}

trait MapData {
	 val id: Int
	 val pos: MapPosition
	 val width: Short
	 val height: Short
	 val subareaId: Option[Int]
	 val cells: Seq[MapCell]
	 val key: Array[Byte]
	 val date: Array[Byte]
	 val premium: Boolean
}

object MapTrigger {
	trait Builder {
		def origin: MapData.Builder
		def withOrigin(origin: MapData.Builder): this.type
		def originCell: MapCell.Builder
		def withOriginCell(originCell: MapCell.Builder): this.type
		def target: MapData.Builder
		def withTarget(target: MapData.Builder): this.type
		def targetCell: MapCell.Builder
		def withTargetCell(targetCell: MapCell.Builder): this.type

		def result: Builder
		def lazyResult: Builder
	}

	def newBuilder: Builder = new MapDataImpl.Trigger()
}

object MapCell {
	trait Builder {
		def id: Short
		def withId(id: Short): this.type
		def map: MapData
		def withMap(map: MapData): this.type
		def los: Boolean
		def withLos(los: Boolean): this.type
		def groundLevel: Short
		def withGroundLevel(groundLevel: Short): this.type
		def movementType: MovementType
		def withMovementType(movementType: MovementType): this.type
		def groundSlope: Short
		def withGroundSlope(groundSlope: Short): this.type
		def interactiveObject: Option[Int]
		def withInteractiveObject(interactiveObject: Option[Int]): this.type
		def trigger: Option[MapTrigger.Builder]
		def withTrigger(trigger: Option[MapTrigger.Builder]): this.type
		
		def result: MapCell
		def lazyResult: MapCell
	}
	
	def newBuilder: Builder = new MapDataImpl.Cell()
}

object MapData {
	trait Builder {
		def id: Int
		def withId(id: Int): this.type
		def pos: MapPosition
		def withPos(pos: MapPosition): this.type
		def width: Short
		def withWidth(width: Short): this.type
		def height: Short
		def withHeight(height: Short): this.type
		def subareaId: Option[Int]
		def withSubareaId(subareaId: Option[Int]): this.type
		def cells: Seq[MapCell.Builder]
		def withCells(cells: Seq[MapCell.Builder]): this.type
		def key: Array[Byte]
		def withKey(key: Array[Byte]): this.type
		def date: Array[Byte]
		def withDate(date: Array[Byte]): this.type
		def premium: Boolean
		def withPremium(premium: Boolean): this.type

		def result: MapData
		def lazyResult: MapData
	}

	def newBuilder: Builder = new MapDataImpl()
}

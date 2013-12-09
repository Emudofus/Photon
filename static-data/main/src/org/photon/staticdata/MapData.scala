package org.photon.staticdata


sealed trait MovementType {
	def id: Int
}

object MovementType extends Enumeration {
	val Unwalkable, Door, Trigger, Walkable, Paddock, Road = new Val with MovementType

	def of(id: Int): MovementType = super.apply(id).asInstanceOf[MovementType]
}

case class MapPosition(x: Int, y: Int)

trait MapCell {
	val id: Short
	val map: MapData
	val los: Boolean
	val groundLevel: Short
	val movementType: MovementType
	val groundSlope: Short
	val interactiveObject: Option[Int]
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

object MapCell {
	trait Builder {
		def withId(id: Short): this.type
		def withMap(map: MapData): this.type
		def withLos(los: Boolean): this.type
		def withGroundLevel(groundLevel: Short): this.type
		def withMovementType(movementType: MovementType): this.type
		def withGroundSlope(groundSlope: Short): this.type
		def withInteractiveObject(interactiveObject: Option[Int]): this.type
		
		def result: MapCell
		def lazyResult: MapCell
	}
	
	def newBuilder: Builder = new MapDataImpl.Cell()
}

object MapData {
	trait Builder {
		def withId(id: Int): this.type
		def withPos(pos: MapPosition): this.type
		def withWidth(width: Short): this.type
		def withHeight(height: Short): this.type
		def withSubareaId(subareaId: Option[Int]): this.type
		def withCells(cells: Seq[MapCell]): this.type
		def withKey(key: Array[Byte]): this.type
		def withDate(date: Array[Byte]): this.type
		def withPremium(premium: Boolean): this.type

		def result: MapData
		def lazyResult: MapData
	}

	def newBuilder: Builder = new MapDataImpl()
}

package org.photon.staticdata

import com.fasterxml.jackson.annotation.{JsonBackReference, JsonManagedReference, ObjectIdGenerators, JsonIdentityInfo}
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, SerializerProvider, JsonSerializer}
import com.fasterxml.jackson.core.{JsonParser, JsonGenerator}
import com.fasterxml.jackson.databind.node.ValueNode
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}

case class MapPosition(x: Int, y: Int)

object MovementType extends Enumeration {
	val Unwalkable, Door, Trigger, Walkable, Paddock, Road = Value

	class Serializer extends JsonSerializer[MovementType] {
		def serialize(value: MovementType, gen: JsonGenerator, p: SerializerProvider) {
			gen.writeNumber(value.id)
		}
	}

	class Deserializer extends JsonDeserializer[MovementType] {
		def deserialize(jp: JsonParser, ctx: DeserializationContext): MovementType = {
			val tree = jp.getCodec.readTree[ValueNode](jp)
			MovementType(tree.intValue())
		}
	}
}

class MapCell(
	 val id: Short,
	 @JsonBackReference
	 val map: MapData,
	 val los: Boolean,
	 val groundLevel: Short,
	 @JsonSerialize(using = classOf[MovementType.Serializer])
	 @JsonDeserialize(using = classOf[MovementType.Deserializer])
	 val movementType: MovementType,
	 val groundSlope: Short,
	 val interactiveObject: Option[Int]
 )

@JsonIdentityInfo(generator = classOf[ObjectIdGenerators.PropertyGenerator], property = "id", scope = classOf[MapData])
class MapData(
	 val id: Int,
	 val pos: MapPosition,
	 val width: Short,
	 val height: Short,
	 val subareaId: Option[Int],
	 @JsonManagedReference
	 val cells: Seq[MapCell],
	 val key: String,
	 val date: String,
	 val premium: Boolean
 )

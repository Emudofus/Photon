package org.photon.staticdata

import com.fasterxml.jackson.databind.{SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object MapDataTest {
  def main(args: Array[String]) {
    val mapper = new ObjectMapper with ScalaObjectMapper
    mapper.registerModule(new DefaultScalaModule)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)

    val cell = new MapCell(1, null, false, 0, MovementType.Walkable, 0, None)

    val map = new MapData(1, MapPosition(1, 1), 1, 1, None, Seq(cell), "lol", "hihi", true)

    val str = mapper.writeValueAsString(map)

    val deserialized = mapper.readValue[MapData](str)
  }
}

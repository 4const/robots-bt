package ru.nstu.cs.robots.view

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}
import ru.nstu.cs.robots.map.Direction

class DirectionSerializer extends JsonSerializer[Direction] {
  override def serialize(value: Direction, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    jgen.writeString(value.toString)
  }
}

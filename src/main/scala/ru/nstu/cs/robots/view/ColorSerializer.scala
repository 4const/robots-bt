package ru.nstu.cs.robots.view

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import ru.nstu.cs.robots.map.Direction
import ru.nstu.cs.robots.system.state.Color

class ColorSerializer extends JsonSerializer[Color] {
  override def serialize(value: Color, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    jgen.writeString(value.toString)
  }
}

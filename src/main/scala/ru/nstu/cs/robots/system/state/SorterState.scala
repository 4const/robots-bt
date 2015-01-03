package ru.nstu.cs.robots.system.state

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ru.nstu.cs.robots.view.ColorSerializer

import scala.annotation.meta.getter


case class SorterState(
  @(JsonIgnore @getter)
  lastColor: Color,

  queues: Map[Color, Int] = Map(Red -> 0, Green -> 0, Blue -> 0))

@JsonSerialize(using = classOf[ColorSerializer])
trait Color

case object NoColor extends Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color
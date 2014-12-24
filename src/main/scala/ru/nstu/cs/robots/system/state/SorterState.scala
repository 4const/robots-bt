package ru.nstu.cs.robots.system.state


case class SorterState(
  lastColor: Color,
  queues: Map[Color, Int] = Map(Red -> 0, Green -> 0, Blue -> 0))

trait Color

case object NoColor extends Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color
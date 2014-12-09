package ru.nstu.cs.robots.map

case class Point(id: Int, links: Seq[Link])

case class Link(from: Int, to: Int, direction: Direction, open: Boolean = true)

trait Direction

case object Top extends Direction
case object Right extends Direction
case object Bottom extends Direction
case object Left extends Direction

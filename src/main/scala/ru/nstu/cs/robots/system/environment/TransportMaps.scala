package ru.nstu.cs.robots.system.environment

import ru.nstu.cs.robots.map._
import ru.nstu.cs.robots.system.state._

trait TransportMap {
  def parkingPorts: Seq[Port]

  def sorterPorts: Map[Color, Port]
  def packerPorts: Map[Color, Port]

  def crossroads: Map[Int, Point]
}

case class Port(point: Int, direction: Direction)

object TransportMaps {

  def apply(id: Int): TransportMap = {
    id match {
      case 1 => First
      case _ => throw new IllegalArgumentException("Transport map does not exist!")
    }
  }

  private object First extends TransportMap {

    def parkingPorts = Seq(Port(8, Right))

    def sorterPorts = Map(
      Red -> Port(2, Top),
      Green -> Port(4, Top),
      Blue -> Port(6, Top)
    )

    def packerPorts = Map(
      Red -> Port(14, Bottom),
      Green -> Port(12, Bottom),
      Blue -> Port(10, Bottom)
    )

    def crossroads = Map(
      1 -> Point(1, Seq(
        Link(1, 2, Top),
        Link(1, 3, Right, open = false),
        Link(1, 13, Left))),

      2 -> Point(2, Seq(Link(2 , 1, Bottom))),

      3 -> Point(3 , Seq(
        Link(3, 4, Top),
        Link(3, 5, Right, open = false),
        Link(3, 1, Left))),

      4 -> Point(4, Seq(Link(4, 3, Bottom))),

      5 -> Point(5, Seq(
        Link(5, 6, Top),
        Link(5, 7, Right, open = false),
        Link(5, 3, Left))),

      6 -> Point(6, Seq(Link(6, 5, Bottom))),

      7 -> Point(7, Seq(
        Link(7, 5, Top),
        Link(7, 8, Left),
        Link(7, 9, Bottom, open = false))),

      8 -> Point(8, Seq(Link(8, 7, Right))),

      9 -> Point(9, Seq(
        Link(9, 7, Right),
        Link(9, 10, Bottom),
        Link(9, 11, Left, open = false))),

      10 -> Point(10, Seq(Link(10, 9, Top))),

      11 -> Point(11, Seq(
        Link(11, 9, Right),
        Link(11, 12, Bottom),
        Link(11, 13, Left, open = false))),

      12 -> Point(12, Seq(Link(12, 11, Top))),

      13 -> Point(13, Seq(
        Link(13, 11, Right),
        Link(13, 14, Bottom),
        Link(13, 1, Left, open = false))),

      14 -> Point(14, Seq(Link(14, 13, Top))),

      15 -> Point(15, Seq(
        Link(15, 1, Top),
        Link(15, 16, Right),
        Link(15, 13, Bottom, open = false))),

      16 -> Point(16, Seq(Link(16, 15, Left)))
    )
  }
}

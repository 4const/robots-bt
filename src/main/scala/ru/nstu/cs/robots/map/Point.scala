package ru.nstu.cs.robots.map

case class Point(id: Int, links: Seq[Link])

case class Link(from: Int, to: Int, direction: Direction, open: Boolean = true)

trait Direction {
  def relativeDirection(to: Direction): Direction
}

object Direction {
  def revertDirection(direction: Direction): Direction = {
    direction match {
      case Top => Bottom
      case Right => Left
      case Bottom => Top
      case Left => Right
      case _ => NoDirection
    }
  }
}

case object NoDirection extends Direction {
  override def relativeDirection(to: Direction): Direction = NoDirection
}

case object Top extends Direction {
  override def relativeDirection(to: Direction): Direction = to
}

case object Right extends Direction {
  override def relativeDirection(to: Direction): Direction = {
    to match {
      case Top => Left
      case Right => Top
      case Bottom => Right
      case Left => Bottom
      case _ => NoDirection
    }
  }
}

case object Bottom extends Direction {
  override def relativeDirection(to: Direction): Direction = Direction.revertDirection(to)
}

case object Left extends Direction {
  override def relativeDirection(to: Direction): Direction = {
    to match {
      case Top => Right
      case Right => Bottom
      case Bottom => Left
      case Left => Top
      case _ => NoDirection
    }
  }
}

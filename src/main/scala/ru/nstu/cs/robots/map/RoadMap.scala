package ru.nstu.cs.robots.map

class RoadMap(crossroads: Map[Int, Point]) {

  def getWay(source: Int, destination: Int): Seq[Point] = {
    findLocalWay(
      Seq(SearchPoint(crossroads(source), Seq())), Seq(), destination)
  }

  def getRelativeDirection(source: Int, destination: Int, bindingPoint: Int): Direction = {
    if (source == bindingPoint || destination == bindingPoint || source == destination) {
      return NoDirection 
    }
    val point = crossroads(bindingPoint)
    val sourceDirection = point.links.find(_.to == source).map(_.direction).getOrElse(NoDirection)
    val destinationDirection = point.links.find(_.to == destination).map(_.direction).getOrElse(NoDirection)
    
    Direction.revertDirection(sourceDirection.relativeDirection(destinationDirection))
  }

  private def findLocalWay(queue: Seq[SearchPoint], visited: Seq[Point], destination: Int): Seq[Point] = {
    if (queue.isEmpty) {
      return Seq()
    }

    val point = queue.head.point
    val path = queue.head.path

    if (visited.contains(point.id)) {
      return findLocalWay(queue.tail, visited, destination)
    }
    if (point.id == destination) {
      return path :+ point
    }

    val nextPoints = point.links
      .filter(_.open)
      .map(l => crossroads(l.to))
      .filterNot(visited.contains)
      .map(p => SearchPoint(p, path :+ point))

    findLocalWay(queue.tail ++ nextPoints, visited :+ point, destination)
  }

  case class SearchPoint(point: Point, path: Seq[Point])
}

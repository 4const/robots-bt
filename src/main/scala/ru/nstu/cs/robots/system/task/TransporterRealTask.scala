package ru.nstu.cs.robots.system.task

import ru.nstu.cs.robots.map.Direction

trait TransporterRealTask {
  def startDirection: Direction
  def endDirection: Direction
}

case class RStay(lookAt: Direction) extends TransporterRealTask {
  override def startDirection = lookAt
  override def endDirection = lookAt
}

case class RMove(startDirection: Direction, endDirection: Direction) extends TransporterRealTask

case class RDrop(lookAt: Direction) extends TransporterRealTask {
  override def startDirection = lookAt
  override def endDirection = lookAt
}



package ru.nstu.cs.robots.system.task

import ru.nstu.cs.robots.map.Direction

trait TransporterTask {
  def endPoint: Int
  def lookAt: Direction
}

case class Stay(in: Int, lookAt: Direction) extends TransporterTask {
  override def endPoint = in
}

case class Move(from: Int, to: Int, lookAt: Direction) extends TransporterTask {
  override def endPoint = to
}
case class Drop(in: Int, lookAt: Direction) extends TransporterTask {
  override def endPoint = in
}


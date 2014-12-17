package ru.nstu.cs.robots.system.task

import ru.nstu.cs.robots.map.Direction

trait TransporterQueueTask {
  def endPoint: Int
  def lookAt: Direction
}

case class QStay(in: Int, lookAt: Direction) extends TransporterQueueTask {
  override def endPoint = in
}

case class QMove(from: Int, to: Int, lookAt: Direction) extends TransporterQueueTask {
  override def endPoint = to
}
case class QDrop(in: Int, lookAt: Direction) extends TransporterQueueTask {
  override def endPoint = in
}


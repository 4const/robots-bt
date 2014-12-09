package ru.nstu.cs.robots.system.task

import ru.nstu.cs.robots.map.Direction

trait TransporterTask

case class Stay(in: Int, lookAt: Direction) extends TransporterTask

case class Move(from: Int, to: Int) extends TransporterTask
case class Drop(in: Int, lookAt: Direction) extends TransporterTask


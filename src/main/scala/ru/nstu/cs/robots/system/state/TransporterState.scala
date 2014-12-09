package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.system.task.TransporterTask

import scala.collection.immutable.Queue

case class TransporterState(
  currentTask: TransporterTask,
  queue: Seq[TransporterTask])

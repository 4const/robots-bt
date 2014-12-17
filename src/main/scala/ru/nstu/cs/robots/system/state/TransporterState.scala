package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.system.task.TransporterQueueTask

import scala.collection.immutable.Queue

case class TransporterState(
  currentTask: TransporterQueueTask,
  queue: Seq[TransporterQueueTask])

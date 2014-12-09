package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.map.{Point, RoadMap, Direction}
import ru.nstu.cs.robots.system.environment.{SorterParameters, TransportMap}
import ru.nstu.cs.robots.system.task.{Drop, Move, TransporterTask, Stay}

class SystemState(transportersIds: Seq[Int], transportMap: TransportMap) {

  var sorterState = SorterState(Seq())
  var transportersState: Map[Int, TransporterState] =
    transportersIds.map(_ ->
      TransporterState(Stay(transportMap.parkingPorts.head.point, transportMap.parkingPorts.head.direction), Seq())).toMap

  val roadMap = new RoadMap(transportMap.crossroads)

  def addBall(color: Color): Map[Int, TransporterTask] = {
    sorterState.copy(sorterState.queue :+ color)

    nextTasks()
  }

  def transporterReady(id: Int): Map[Int, TransporterTask] = {
    stayTransporter(id)

    nextTasks()
  }

  private def nextTasks(): Map[Int, TransporterTask] = {
    val (free, awaiting) = transportersState.partition { case (_, state) => isAwaiting(state) }
    awaiting.map { case (id, state) => nextTransporterTask(id, state) } ++
    assignGlobalTask(free)
  }

  private def isAwaiting(state: TransporterState): Boolean = {
    state.currentTask.isInstanceOf[Stay] && state.queue.nonEmpty
  }

  private def nextTransporterTask(id: Int, state: TransporterState): (Int, TransporterTask) = {
    val task = state.currentTask
    val queue = state.queue

    val topTask = queue.head
    val (nextTask, nextQueue) = if (canDo(id, topTask)) {
      val nextQueue = if (queue.isEmpty) Seq() else queue.tail
      (topTask, nextQueue)
    } else {
      val stay = Stay(taskEndPoint(task), taskEndLookAt(task))
      (stay, queue)
    }
    changeState(id, nextTask, nextQueue)

    (id, nextTask)
  }

  private def assignGlobalTask(freeTransporters: Map[Int, TransporterState]): Map[Int, TransporterTask] = {
    def refreshState(color: Color, state: SorterState): SorterState = {
      SorterState(
        state.queue.foldLeft((0, Seq[Color]())) {
          case ((count, r), c) =>
            if (c == color) {
              if (count <= SorterParameters.MAX_PACKAGE) {
                (count + 1, r)
              } else {
                (count, r :+ c)
              }
            } else {
              (count, r :+ c)
            }
        }._2)
    }

    freeTransporters.foldLeft(Map[Int, TransporterTask]()) {
      case (result, (id, state)) =>
        sorterState.queue.headOption match {
          case Some(color) =>
            val queue = createTaskQueue(color, taskEndPoint(state.currentTask))
            val firstTask = queue.head
            changeState(id, firstTask, queue.tail)

            sorterState = refreshState(color, sorterState)

            result + (id -> firstTask)
          case _ => result
        }
    }
  }

  private def createTaskQueue(color: Color, startPoint: Int): Seq[TransporterTask] = {
    val sorterPort = transportMap.sorterPorts(color)
    val packerPort = transportMap.packerPorts(color)
    val parkingPort = transportMap.parkingPorts.find { case port => isParkingFree(port.point) }.get

    val toSorterPoints = roadMap.getWay(startPoint, sorterPort.point)
    val toPackerPoints = roadMap.getWay(sorterPort.point, packerPort.point)
    val toParkingPoints = roadMap.getWay(packerPort.point, parkingPort.point)

    def makeQueue(points: Seq[Point]): Seq[TransporterTask] = {
      points
        .zip(points.tail)
        .map { case (from, to) => Move(from.id, from.id) }
    }

    makeQueue(toSorterPoints)
    .:+(Stay(sorterPort.point, sorterPort.direction)) ++
    makeQueue(toPackerPoints)
    .:+(Drop(packerPort.point, packerPort.direction)) ++
    makeQueue(toParkingPoints)
  }

  private def isParkingFree(parkingPort: Int): Boolean = {
    transportersState.values
      .find {
        case state =>
          taskEndPoint(state.queue.last) == parkingPort
      }
      .isEmpty
  }

  private def stayTransporter(id: Int): Unit = {
    val state = transportersState(id)
    val task = state.currentTask

    val stayTask = Stay(taskEndPoint(task), taskEndLookAt(task))
    changeState(id, stayTask, state.queue)
  }

  private def changeState(id: Int, task: TransporterTask, queue: Seq[TransporterTask]): Unit = {
    transportersState = transportersState + (id -> TransporterState(task, queue))
  }

  private def taskEndPoint(task: TransporterTask): Int = {
    task match {
      case Stay(in, _) => in
      case Move(_, to) => to
      case Drop(in, _) => in
    }
  }

  private def taskEndLookAt(task: TransporterTask): Direction = {
    task match {
      case Stay(_, lookAt) => lookAt
      case Move(from, to) => transportMap.crossroads(from).links.find(_.to == to).get.direction
      case Drop(_, lookAt) => lookAt
    }
  }

  private def canDo(id: Int, task: TransporterTask): Boolean = {
    val taskEnd = taskEndPoint(task)
    transportersState
      .filterNot { case (transporterId, _) => transporterId == id }
      .find {
        case (_, state) =>
          def willSoonBeOccupied(point: Int, tasks: Seq[TransporterTask]): Boolean = {
            tasks.indexWhere(taskEndPoint(_) == point) <= 2
          }

          taskEndPoint(state.currentTask) == taskEnd || willSoonBeOccupied(taskEnd, state.queue)
      }
      .isEmpty
  }
}

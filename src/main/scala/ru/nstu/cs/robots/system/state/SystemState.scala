package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.map.{Point, RoadMap}
import ru.nstu.cs.robots.system.environment.{SorterParameters, TransportMap}
import ru.nstu.cs.robots.system.task.{Drop, Move, TransporterTask, Stay}
import SystemState._

object SystemState {

  def initStates(transportersIds: Seq[Int], transportMap: TransportMap) = transportersIds.map(_ ->
    TransporterState(Stay(transportMap.parkingPorts.head.point, transportMap.parkingPorts.head.direction), Seq())).toMap
}

class SystemState(
  private var sorterState: SorterState,
  private var transportersState: Map[Int, TransporterState],
  private var lastColor: Color,
  private val transportMap: TransportMap) {

  def this(transportersIds: Seq[Int], transportMap: TransportMap) = {
    this(SorterState(), initStates(transportersIds, transportMap), NoColor, transportMap)
  }

  val roadMap = new RoadMap(transportMap.crossroads)

  def addBalls(balls: Map[Color, Int]): Map[Int, TransporterTask] = {
    sorterState.copy(sorterState.queues.map {
      case (c, count) =>
        c -> (count + balls(c))
    })

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
      val stay = Stay(task.endPoint, task.lookAt)
      (stay, queue)
    }
    changeState(id, nextTask, nextQueue)

    (id, nextTask)
  }

  private def assignGlobalTask(freeTransporters: Map[Int, TransporterState]): Map[Int, TransporterTask] = {
    def refreshState(color: Color, sorterState: SorterState): SorterState = {
      SorterState(
        sorterState.queues.map { case (c, count) =>
          if (c == color) {
            val res = count - SorterParameters.MAX_PACKAGE
            if (res > 0) {
              c -> res
            } else {
              c -> 0
            }
          } else {
            c -> count
          }
        })
    }

    def makeTask(color: Color, count: Int)(id: Int, state: TransporterState)(result: Map[Int, TransporterTask]): Map[Int, TransporterTask] = {
      val queue = createTaskQueue(color, state.currentTask.endPoint)
      val firstTask = queue.head
      changeState(id, firstTask, queue.tail)

      lastColor = color
      sorterState = refreshState(color, sorterState)

      result + (id -> firstTask)
    }

    freeTransporters.foldLeft(Map[Int, TransporterTask]()) {
      case (result, (id, state)) =>
        sorterState.queues
          .find { case (color, count) => (color != lastColor) && count != 0 }
          .map { case (color, count) => makeTask(color, count)(id, state)(result) }
          .orElse {
            val count = sorterState.queues(lastColor)
            if (count != 0) {
              Some(makeTask(lastColor, count)(id, state)(result))
            } else {
              None
            }
          }
          .getOrElse(result)
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
        .map {
          case (from, to) =>
            val direction = transportMap.crossroads(from.id).links.find(_.to == to.id).get.direction
            Move(from.id, to.id, direction)
        }
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
          state.queue.last.endPoint == parkingPort
      }
      .isEmpty
  }

  private def stayTransporter(id: Int): Unit = {
    val state = transportersState(id)
    val task = state.currentTask

    val stayTask = Stay(task.endPoint, task.lookAt)
    changeState(id, stayTask, state.queue)
  }

  private def changeState(id: Int, task: TransporterTask, queue: Seq[TransporterTask]): Unit = {
    transportersState = transportersState + (id -> TransporterState(task, queue))
  }

  private def canDo(id: Int, task: TransporterTask): Boolean = {
    val taskEnd = task.endPoint
    transportersState
      .filterNot { case (transporterId, _) => transporterId == id }
      .find {
        case (_, state) =>
          def willSoonBeOccupied(point: Int, tasks: Seq[TransporterTask]): Boolean = {
            tasks.indexWhere(_.endPoint == point) <= 2
          }

          state.currentTask.endPoint == taskEnd || willSoonBeOccupied(taskEnd, state.queue)
      }
      .isEmpty
  }
}

package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.map.{Point, RoadMap}
import ru.nstu.cs.robots.system.environment.{SorterParameters, TransportMap}
import ru.nstu.cs.robots.system.task._
import ru.nstu.cs.robots.map._
import SystemState._

object SystemState {

  def initStates(transportersIds: Seq[Int], transportMap: TransportMap) = transportersIds.map(_ ->
    TransporterState(QStay(transportMap.parkingPorts.head.point, transportMap.parkingPorts.head.direction), Seq())).toMap
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

  def addBalls(balls: Map[Color, Int]): Map[Int, TransporterQueueTask] = {
    sorterState = sorterState.copy(sorterState.queues.map {
      case (c, count) =>
        c -> (count + balls(c))
    })

    nextTasks()
  }

  def transporterReady(id: Int): Map[Int, TransporterQueueTask] = {
    stayTransporter(id)

    nextTasks()
  }

  private def nextTasks(): Map[Int, TransporterQueueTask] = {
    def inProgress(state: TransporterState): Boolean = {
      !state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty
    }

    def isAwaiting(state: TransporterState): Boolean = {
      state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty
    }

    val (awaiting, free) = transportersState
      .filterNot { case (_, state) => inProgress(state) }
      .partition { case (_, state) => isAwaiting(state) }
    awaiting.map { case (id, state) => nextTransporterTask(id, state) } ++
    assignGlobalTask(free)
  }

  private def nextTransporterTask(transporterId: Int, state: TransporterState): (Int, TransporterQueueTask) = {
    val task = state.currentTask
    val queue = state.queue

    val topTaskOption = queue.headOption

    val (nextTask, nextQueue) = topTaskOption match {
      case Some(topTask) if canDo(transporterId, task.endPoint, topTask.endPoint) =>
        val nextQueue = if (queue.isEmpty) Seq() else queue.tail
        (topTask, nextQueue)
      case _ =>
        val stay = QStay(task.endPoint, task.lookAt)
        (stay, queue)
    }

    changeState(transporterId, nextTask, nextQueue)

    (transporterId, nextTask)
  }

  private def assignGlobalTask(freeTransporters: Map[Int, TransporterState]): Map[Int, TransporterQueueTask] = {
    def refreshState(color: Color, sorterState: SorterState): SorterState = {
      SorterState(
        sorterState.queues.map { case (c, count) =>
          if (c == color) {
            c -> Math.max(count - SorterParameters.MAX_PACKAGE, 0)
          } else {
            c -> count
          }
        })
    }

    def makeTask(color: Color, count: Int)(transporterId: Int, state: TransporterState)(result: Map[Int, TransporterQueueTask]): Map[Int, TransporterQueueTask] = {
      val queue = createTaskQueue(color, state.currentTask.endPoint, transporterId)
      val firstTask = queue.head
      changeState(transporterId, firstTask, queue.tail)

      lastColor = color
      sorterState = refreshState(color, sorterState)

      result + (transporterId -> firstTask)
    }

    freeTransporters.foldLeft(Map[Int, TransporterQueueTask]()) {
      case (result, (transporterId, state)) =>
        sorterState.queues
          .find { case (color, count) => (color != lastColor) && count != 0 }
          .map { case (color, count) => makeTask(color, count)(transporterId, state)(result) }
          .orElse {
            lastColor match {
              case NoColor => None
              case _ =>
                val count = sorterState.queues(lastColor)
                if (count != 0) {
                  Some(makeTask(lastColor, count)(transporterId, state)(result))
                } else {
                  None
                }
            }
          }
          .getOrElse(result)
    }
  }

  private def createTaskQueue(color: Color, startPoint: Int, transporterId: Int): Seq[TransporterQueueTask] = {
    val sorterPort = transportMap.sorterPorts(color)
    val packerPort = transportMap.packerPorts(color)
    val parkingPort = transportMap.parkingPorts.find { case port => isParkingFree(transporterId, port.point) }.get

    val toSorterPoints = roadMap.getWay(startPoint, sorterPort.point)
    val toPackerPoints = roadMap.getWay(sorterPort.point, packerPort.point)
    val toParkingPoints = roadMap.getWay(packerPort.point, parkingPort.point)

    def makeQueue(points: Seq[Point]): Seq[TransporterQueueTask] = {
      points
        .zip(points.tail)
        .map {
          case (from, to) =>
            val direction = transportMap.crossroads(from.id).links.find(_.to == to.id).get.direction
            QMove(from.id, to.id, direction)
        }
    }

    makeQueue(toSorterPoints)
    .:+(QStay(sorterPort.point, sorterPort.direction)) ++
    makeQueue(toPackerPoints)
    .:+(QDrop(packerPort.point, packerPort.direction)) ++
    makeQueue(toParkingPoints)
  }

  private def isParkingFree(transporterId: Int, parkingPort: Int): Boolean = {
    transportersState
      .find { case (id, state) =>
        id != transporterId &&
          state.queue.lastOption.map(_.endPoint == parkingPort)
            .getOrElse(state.currentTask.endPoint == parkingPort) }
      .isEmpty
  }

  private def stayTransporter(id: Int): Unit = {
    val state = transportersState(id)
    val task = state.currentTask

    val stayTask = QStay(task.endPoint, task.lookAt)
    changeState(id, stayTask, state.queue)
  }

  private def changeState(id: Int, task: TransporterQueueTask, queue: Seq[TransporterQueueTask]): Unit = {
    transportersState = transportersState + (id -> TransporterState(task, queue))
  }

  private def canDo(id: Int, currentPosition: Int, nextPosition: Int): Boolean = {
    transportersState
      .filterNot { case (transporterId, _) => transporterId == id }
      .find {
        case (_, state) =>
          def willSoonBeOccupied(point: Int, tasks: Seq[TransporterQueueTask]): Boolean = {
            val p = tasks.indexWhere(_.endPoint == point)
            p >= 0 && p <= 2
          }

          val endPoint = state.currentTask.endPoint
          val lowPriority = state.queue.headOption.exists { case next =>
            roadMap.getRelativeDirection(endPoint, currentPosition, next.endPoint) == Right
          }

          if (lowPriority) {
            false
          } else {
            endPoint == nextPosition || willSoonBeOccupied(nextPosition, state.queue)
          }
      }
      .isEmpty
  }
}

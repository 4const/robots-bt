package ru.nstu.cs.robots.system.state

import ru.nstu.cs.robots.map.{Point, RoadMap}
import ru.nstu.cs.robots.system.environment.{Port, SorterParameters, TransportMap}
import ru.nstu.cs.robots.system.task._
import ru.nstu.cs.robots.map._
import SystemState._

object SystemState {

  private def initStates(transporters: Map[Int, Port], transportMap: TransportMap) = transporters.mapValues {
    case port => TransporterState(QStay(port.point, port.direction), Seq())
  }
}

class SystemState(
  private val sorterState: SorterState,
  private val transportersState: Map[Int, TransporterState],
  private val lastColor: Color,
  private val transportMap: TransportMap) {

  def this(transporters: Map[Int, Port], transportMap: TransportMap) = {
    this(SorterState(), initStates(transporters, transportMap), NoColor, transportMap)
  }

  val roadMap = new RoadMap(transportMap.crossroads)

  def tasks: Map[Int, TransporterQueueTask] = transportersState.mapValues(_.currentTask)

  def addBalls(balls: Map[Color, Int]): SystemState = {
    val updatedSorter = sorterState.copy(sorterState.queues.map { case (c, count) => c -> (count + balls(c)) })

    nextState(updatedSorter, transportersState)
  }

  def transporterReady(id: Int): SystemState = {
    val state = transportersState(id)
    val task = state.currentTask

    val updatedSorter = task match {
      case QMove(_, to, _) =>
        transportMap.sorterPorts
          .find {
          case (_, p) => p.point == to
        } match {
          case Some((c, p)) =>
            val queues = sorterState.queues
            SorterState(queues.updated(c, Math.max(queues(c), 0)))
          case _ => sorterState
        }
      case _ => sorterState
    }

    val stayTask = QStay(task.endPoint, task.lookAt)
    val updatedTransporters = transportersState + (id -> TransporterState(stayTask, state.queue))

    nextState(updatedSorter, updatedTransporters)
  }

  private def nextState(sorterState: SorterState, transportersState: Map[Int, TransporterState]): SystemState = {
    val (awaiting, free) = transportersState
      .filterNot { case (_, state) => !state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty }
      .partition { case (_, state) => state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty }

    val awaitingNextState = awaiting.foldLeft(Map[Int, TransporterState]()) {
      case (result, (id, state)) => result + nextTransporterState(id, state, awaiting, result)
    }
    val nextStates = free.foldLeft(awaitingNextState) {
      case (result, (id, state)) => result + nextTransporterState(id, state, free, result)
    }

    new SystemState(sorterState)
  }

  private def nextTransporterState(transporterId: Int, state: TransporterState,
                                   awaitingTransporters: Map[Int, TransporterState],
                                   refreshedTransporters: Map[Int, TransporterState]): (Int, TransporterState) = {
    val task = state.currentTask
    val queue = state.queue

    val (nextTask, nextQueue) = queue.headOption match {
      case Some(topTask) if canDo(transporterId, topTask.endPoint, awaitingTransporters, refreshedTransporters) =>
        val nextQueue = if (queue.isEmpty) Seq() else queue.tail
        (topTask, nextQueue)
      case _ =>
        val stay = QStay(task.endPoint, task.lookAt)
        (stay, queue)
    }

    (transporterId, TransporterState(nextTask, nextQueue))
  }

  private def canDo(transporterId: Int, nextPosition: Int,
                    awaitingTransporters: Map[Int, TransporterState],
                    refreshedTransporters: Map[Int, TransporterState]): Boolean = {
    def soonBeOccupied(state: TransporterState): Boolean = {
      val end = state.currentTask.endPoint
      state.queue.nonEmpty && (
        transportMap.sorterPorts.exists { case (_, port) => port.point == end} ||
          transportMap.packerPorts.exists { case (_, port) => port.point == end}) ||
        end == nextPosition
    }

    awaitingTransporters
      .filterNot { case (id, _) => refreshedTransporters.contains(id) && id == transporterId }
      .find { case (_, state) => soonBeOccupied(state) }
      .isEmpty &&
    refreshedTransporters
      .filterNot { case (id, _) => id == transporterId }
      .find { case (_, state) => soonBeOccupied(state) }
      .isEmpty
  }

  private def assignGlobalTask(transporterId: Int, state: TransporterState,
                               freeTransporters: Map[Int, TransporterState],
                               refreshedTransporters: Map[Int, TransporterState]): (Int, TransporterState) = {
    def refreshState(color: Color, sorterState: SorterState): SorterState = {
      val queues = sorterState.queues
      SorterState(queues.updated(color, queues(color) - SorterParameters.MAX_PACKAGE))
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
              if (count > 0) {
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

//  private def nextTasks(): Map[Int, TransporterQueueTask] = {
//    def inProgress(state: TransporterState): Boolean = {
//      !state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty
//    }
//
//    def isAwaiting(state: TransporterState): Boolean = {
//      state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty
//    }
//
//    val (awaiting, free) = transportersState
//      .filterNot { case (_, state) => inProgress(state) }
//      .partition { case (_, state) => isAwaiting(state) }
//    awaiting.map { case (id, state) => nextTransporterTask(id, state) } ++
//    assignGlobalTask(free)
//  }
//
//  private def nextTransporterTask(transporterId: Int, state: TransporterState): (Int, TransporterQueueTask) = {
//    val task = state.currentTask
//    val queue = state.queue
//
//    val topTaskOption = queue.headOption
//
//    val (nextTask, nextQueue) = topTaskOption match {
//      case Some(topTask) if canDo(transporterId, task.endPoint, topTask.endPoint) =>
//        val nextQueue = if (queue.isEmpty) Seq() else queue.tail
//        (topTask, nextQueue)
//      case _ =>
//        val stay = QStay(task.endPoint, task.lookAt)
//        (stay, queue)
//    }
//
//    changeState(transporterId, nextTask, nextQueue)
//
//    (transporterId, nextTask)
//  }
//
//
//  private def changeState(id: Int, task: TransporterQueueTask, queue: Seq[TransporterQueueTask]): Unit = {
//    transportersState = transportersState + (id -> TransporterState(task, queue))
//  }
}

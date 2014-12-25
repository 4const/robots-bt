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
  private val transportersTasks: Map[Int, TransporterQueueTask],
  private val transportMap: TransportMap) {

  def this(transporters: Map[Int, Port], transportMap: TransportMap) = {
    this(SorterState(NoColor), initStates(transporters, transportMap), Map(), transportMap)
  }

  val roadMap = new RoadMap(transportMap.crossroads)

  def tasks: Map[Int, TransporterQueueTask] = transportersTasks

  def addBalls(balls: Map[Color, Int]): SystemState = {
    val updatedSorter = sorterState.copy(queues = sorterState.queues.map { case (c, count) => c -> (count + balls(c)) })

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
            sorterState.copy(queues = queues.updated(c, Math.max(queues(c), 0)))
          case _ => sorterState
        }
      case _ => sorterState
    }

    val stayTask = QStay(task.endPoint, task.lookAt)
    val updatedTransporters = transportersState + (id -> TransporterState(stayTask, state.queue))

    nextState(updatedSorter, updatedTransporters)
  }

  private def nextState(sorterState: SorterState, transportersState: Map[Int, TransporterState]): SystemState = {
    val busy = transportersState
      .filter { case (_, state) => !state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty }
    val (awaiting, free) = (transportersState -- busy.keys)
      .partition { case (_, state) => state.currentTask.isInstanceOf[QStay] && state.queue.nonEmpty }

    val awaitingNextState = awaiting.foldLeft(Map[Int, TransporterState]()) {
      case (result, (id, state)) => result + (id -> nextTransporterState(id, state, busy, awaiting, result))
    }
    val (nextSState, nextTStates) = free.foldLeft(sorterState, awaitingNextState) {
      case ((s, t), (id, state)) =>
        val (newSorterState, newTransporterState) = assignGlobalTask(id, state, s, busy, free, t)
        (newSorterState, t + (id -> newTransporterState))
    }

    new SystemState(nextSState, busy ++ nextTStates, nextTStates.mapValues(_.currentTask), transportMap)
  }

  private def nextTransporterState(transporterId: Int, state: TransporterState,
                                   busyTransporters: Map[Int, TransporterState],
                                   awaitingTransporters: Map[Int, TransporterState],
                                   refreshedTransporters: Map[Int, TransporterState]): TransporterState = {
    val task = state.currentTask
    val queue = state.queue

    val (nextTask, nextQueue) = queue.headOption match {
      case Some(topTask)
        if canDo(transporterId, topTask.endPoint, busyTransporters,awaitingTransporters, refreshedTransporters) =>
        val nextQueue = if (queue.isEmpty) Seq() else queue.tail
        (topTask, nextQueue)
      case _ =>
        val stay = QStay(task.endPoint, task.lookAt)
        (stay, queue)
    }

    TransporterState(nextTask, nextQueue)
  }

  private def canDo(transporterId: Int, nextPosition: Int,
                    busyTransporters: Map[Int, TransporterState],
                    notRefreshedTransporters: Map[Int, TransporterState],
                    refreshedTransporters: Map[Int, TransporterState]): Boolean = {
    def soonBeOccupied(state: TransporterState): Boolean = {
      val end = state.currentTask.endPoint
      state.queue.nonEmpty && (
        transportMap.sorterPorts.exists { case (_, port) => port.point == end} ||
        transportMap.packerPorts.exists { case (_, port) => port.point == end}) ||
        end == nextPosition
    }
    val busyed = busyTransporters
      .find { case (_, state) => soonBeOccupied(state) }
      .isEmpty
    val notRefreshed = notRefreshedTransporters
      .filterNot { case (id, _) => refreshedTransporters.contains(id) || id == transporterId }
      .find { case (_, state) => soonBeOccupied(state) }
      .isEmpty
    val refreshed = refreshedTransporters
      .filterNot { case (id, _) => id == transporterId }
      .find { case (_, state) => soonBeOccupied(state) }
      .isEmpty

    busyed && notRefreshed && refreshed
  }

  private def assignGlobalTask(transporterId: Int, state: TransporterState,
                               sorterState: SorterState,
                               busyTransporters: Map[Int, TransporterState],
                               freeTransporters: Map[Int, TransporterState],
                               refreshedTransporters: Map[Int, TransporterState]): (SorterState, TransporterState) = {
    def assignQueue(color: Color, count: Int, transporterId: Int, state: TransporterState): (SorterState, TransporterState) = {
      val currentTask = state.currentTask

      val queue = createTaskQueue(color, currentTask.endPoint, transporterId, freeTransporters ++ refreshedTransporters)
      val firstTask = queue.head
      val nextTState = if (canDo(transporterId, firstTask.endPoint, busyTransporters, freeTransporters, refreshedTransporters)) {
        TransporterState(firstTask, queue.tail)
      } else {
        TransporterState(QStay(currentTask.endPoint, currentTask.lookAt), queue)
      }

      val queues = sorterState.queues
      val nextSState = SorterState(color, queues.updated(color, queues(color) - SorterParameters.MAX_PACKAGE))

      (nextSState, nextTState)
    }

    val lastColor = sorterState.lastColor
    sorterState.queues
      .find { case (color, count) => (color != lastColor) && count != 0 }
      .map { case (color, count) => assignQueue(color, count, transporterId, state) }
      .orElse {
        lastColor match {
          case NoColor => None
          case _ =>
            val count = sorterState.queues(lastColor)
            if (count > 0) {
              Some(assignQueue(lastColor, count, transporterId, state))
            } else {
              None
            }
        }
      }
      .getOrElse(sorterState, state)
  }

  private def createTaskQueue(color: Color, startPoint: Int, transporterId: Int,
                              transportersState: Map[Int, TransporterState]): Seq[TransporterQueueTask] = {
    val sorterPort = transportMap.sorterPorts(color)
    val packerPort = transportMap.packerPorts(color)
    val parkingPort =
      transportMap.parkingPorts.find { case port => isParkingFree(transporterId, port.point, transportersState) }.get

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

  private def isParkingFree(transporterId: Int, parkingPort: Int, transportersState: Map[Int, TransporterState]): Boolean = {
    transportersState
      .find { case (id, state) =>
        id != transporterId &&
        state.queue.lastOption.map(_.endPoint == parkingPort)
          .getOrElse(state.currentTask.endPoint == parkingPort)
      }
      .isEmpty
  }
}

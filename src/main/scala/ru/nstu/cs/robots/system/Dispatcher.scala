package ru.nstu.cs.robots.system

import akka.actor.{Actor, ActorRef, Props}
import ru.nstu.cs.robots.map.Direction
import ru.nstu.cs.robots.system.Dispatcher.{Balls, TransporterReady}
import ru.nstu.cs.robots.system.Transporter.Do
import ru.nstu.cs.robots.system.environment.TransportMap
import ru.nstu.cs.robots.system.state.{Color, SystemState}
import ru.nstu.cs.robots.system.task._


object Dispatcher {

  def props(map: TransportMap, sorterParams: SorterInitParams, transportersParams: Seq[TransporterInitParams]): Props =
    Props(new Dispatcher(map, sorterParams, transportersParams))

  case class Balls(balls: Map[Color, Int])

  case class TransporterReady(id: Int)
}

class Dispatcher(map: TransportMap, sorterParams: SorterInitParams, transportersParams: Seq[TransporterInitParams]) extends Actor {

  val sorter = context.actorOf(Sorter.props(sorterParams.port, sorterParams.mock))
  val transporters: Map[Int, ActorRef] =
    transportersParams.map {
      case TransporterInitParams(port, parking, mock) =>
        port -> context.actorOf(Transporter.props(port, map.parkingPorts(parking).direction, mock))
    }
    .toMap

  var systemState = new SystemState(
    transportersParams.map(params => params.port -> map.parkingPorts(params.parking)).toMap, map)

  override def receive: Receive = {
    case Balls(balls) =>
      systemState = systemState.addBalls(balls)
      dispatchNextTasks(systemState.tasks)

    case TransporterReady(id) =>
      systemState = systemState.transporterReady(id)
      dispatchNextTasks(systemState.tasks)
  }


  private def dispatchNextTasks(tasks: Map[Int, TransporterQueueTask]): Unit = {
    tasks
      .mapValues(mapQueueTaskToReal)
      .foreach { case (id, task) => transporters(id) ! Do(task) }
  }

  private def mapQueueTaskToReal(task: TransporterQueueTask): TransporterRealTask = {
    task match {
      case QStay(_, lookAt) => RStay(lookAt)
      case QDrop(_, lookAt) => RDrop(lookAt)
      case QMove(from, to, lookAt) =>
        val directionFromToToFrom = map.crossroads(to).links.find(_.to == from).get.direction
        val endDirection = Direction.revertDirection(directionFromToToFrom)

        RMove(lookAt, endDirection)
    }
  }
}
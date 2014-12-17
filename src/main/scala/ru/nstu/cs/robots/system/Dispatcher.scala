package ru.nstu.cs.robots.system

import akka.actor.{Actor, ActorRef, Props}
import ru.nstu.cs.robots.map.Direction
import ru.nstu.cs.robots.system.Dispatcher.{Balls, TransporterReady}
import ru.nstu.cs.robots.system.Transporter.Do
import ru.nstu.cs.robots.system.environment.TransportMap
import ru.nstu.cs.robots.system.state.{Color, SystemState}
import ru.nstu.cs.robots.system.task._


object Dispatcher {

  def props(map: TransportMap, sorterPort: Int, transportersPortPoint: Map[Int, Int]): Props =
    Props(new Dispatcher(map, sorterPort, transportersPortPoint))

  case class Balls(balls: Map[Color, Int])

  case class TransporterReady(id: Int)
}

class Dispatcher(map: TransportMap, sorterId: Int, transportersIds: Map[Int, Int]) extends Actor {

  val sorter = context.actorOf(Sorter.props(sorterId))
  val transporters: Map[Int, ActorRef] =
    transportersIds.map { case (port, parking) =>
      port -> context.actorOf(Transporter.props(port, map.parkingPorts(parking).direction))
    }

  var systemState = new SystemState(transportersIds.keys.toSeq, map)

  override def receive: Receive = {
    case Balls(balls) =>
      dispatchNextTasks(systemState.addBalls(balls))

    case TransporterReady(id) =>
      dispatchNextTasks(systemState.transporterReady(id))
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
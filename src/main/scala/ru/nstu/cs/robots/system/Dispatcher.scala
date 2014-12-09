package ru.nstu.cs.robots.system

import akka.actor.{ActorRef, Props, Actor}
import ru.nstu.cs.robots.system.Dispatcher.{TransporterReady, Ball}
import ru.nstu.cs.robots.system.environment.TransportMaps
import ru.nstu.cs.robots.system.state.{SystemState, Color}
import ru.nstu.cs.robots.system.task._


object Dispatcher {

  def props(mapId: Int, sorter: Int, transporters: List[Int]): Props = Props(new Dispatcher(mapId, sorter, transporters))

  case class Ball(color: Color)

  case class TransporterReady(id: Int)
}

class Dispatcher(mapId: Int, sorterId: Int, transportersIds: List[Int]) extends Actor {

  val sorter = context.actorOf(Sorter.props(sorterId))
  val transporters: Map[Int, ActorRef] =
    transportersIds.map(id => id -> context.actorOf(Transporter.props(id))).toMap

  val systemState = new SystemState(transportersIds, TransportMaps(mapId))

  override def receive: Receive = {
    case Ball(color) =>
      dispatchNextTasks(systemState.addBall(color))

    case TransporterReady(id) =>
      dispatchNextTasks(systemState.transporterReady(id))
  }


  private def dispatchNextTasks(tasks: Map[Int, TransporterTask]): Unit = {
    tasks.foreach { case (id, task) => transporters(id) ! task }
  }
}

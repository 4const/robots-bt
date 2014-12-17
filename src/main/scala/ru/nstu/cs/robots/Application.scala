package ru.nstu.cs.robots

import akka.actor.ActorSystem
import ru.nstu.cs.robots.system.Dispatcher
import ru.nstu.cs.robots.system.environment.TransportMaps


object Application extends App {

  override def main(args: Array[String]) {
    val system = ActorSystem("system")

    val map = TransportMaps(1)
    val transporters: Map[Int, Int] = Map(
      11 -> 0
    )
    system.actorOf(Dispatcher.props(map, 9, transporters), "dispatcher")
  }
}
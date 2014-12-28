package ru.nstu.cs.robots

import akka.actor.ActorSystem
import ru.nstu.cs.robots.system.{TransporterInitParams, SorterInitParams, Dispatcher}
import ru.nstu.cs.robots.system.environment.TransportMaps


object Application { // extends App {

//  override def main(args: Array[String]) {
//    val system = ActorSystem("system")
//
//    val map = TransportMaps(1)
//
//    val sorterInitParams = new SorterInitParams(9, true)
//    val transportersInitParams = Seq(
//      new TransporterInitParams(3, 0, true)
////      new TransporterInitParams(13, 1, true)
////      13 -> 1
////      11 -> 1
//    )
//
//    system.actorOf(Dispatcher.props(map, sorterInitParams, transportersInitParams), "dispatcher")
//  }
}
package ru.nstu.cs.robots

import _root_.akka.actor.ActorSystem
import org.scalatra._
import ru.nstu.cs.robots.system.Dispatcher
import ru.nstu.cs.robots.system.environment.TransportMaps
import javax

class WebBootstrap extends LifeCycle {

  val system = ActorSystem()

  val map = TransportMaps(1)
  val transporters: Map[Int, Int] = Map(
    11 -> 0
  )
  val dispatcher = system.actorOf(Dispatcher.props(map, 9, transporters), "dispatcher")

  val system = ActorSystem()
  val myActor = system.actorOf(Props[MyActor])

  override def init(context: ServletContext) {
    context.mount(new PageRetriever(system), "/*")
    context.mount(new MyActorApp(system, myActor), "/actors/*")
  }

  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}
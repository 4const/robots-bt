package ru.nstu.cs.robots

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.twitter.finatra._
import ru.nstu.cs.robots.system.Dispatcher.GetState
import ru.nstu.cs.robots.system.environment.TransportMaps
import ru.nstu.cs.robots.system.state.SystemState
import ru.nstu.cs.robots.system.{SorterInitParams, Dispatcher, TransporterInitParams}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.dispatch.ExecutionContexts._




object Application extends FinatraServer {

  val system = ActorSystem("system")
  val map = TransportMaps(1)
  val sorterInitParams = new SorterInitParams(9, true)
  val transportersInitParams = Seq(
    new TransporterInitParams(3, 0, true)
    //      new TransporterInitParams(13, 1, true)
    //      13 -> 1
    //      11 -> 1
  )
  val dispatcher = system.actorOf(Dispatcher.props(map, sorterInitParams, transportersInitParams), "dispatcher")
  implicit val timeout = Timeout(5 seconds)

  class ExampleApp extends Controller {
    get("/") { request =>
      val future = dispatcher ? GetState
      val state = Await.result(future, 10 seconds).asInstanceOf[SystemState]

      render.body(state.toString).toFuture
    }
  }

  register(new ExampleApp())
}


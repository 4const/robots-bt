package ru.nstu.cs.robots

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.twitter.finatra._
import ru.nstu.cs.robots.system.Dispatcher.{Balls, GetState}
import ru.nstu.cs.robots.system.environment.TransportMaps
import ru.nstu.cs.robots.system.state.{Blue, Green, Red, SystemState}
import ru.nstu.cs.robots.system.{SorterInitParams, Dispatcher, TransporterInitParams}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.dispatch.ExecutionContexts._


object Application extends FinatraServer {

  val system = ActorSystem("system")
  val map = TransportMaps(1)
  val sorterInitParams = new SorterInitParams(9, mock = true)
  val transportersInitParams = Seq(
    new TransporterInitParams(3, 0, mock = false),
//    new TransporterInitParams(11, 1, mock = false)
    new TransporterInitParams(13, 1, mock = false)
  )
  val dispatcher = system.actorOf(Dispatcher.props(map, sorterInitParams, transportersInitParams), "dispatcher")
  implicit val timeout = Timeout(5 seconds)

  class ExampleApp extends Controller {
    get("/") { request =>
      val future = dispatcher ? GetState
      val state = Await.result(future, 10 seconds).asInstanceOf[SystemState]

      render.body(state.toString).toFuture
    }

    get("/balls") { request =>
      val red = request.params.getOrElse("red", "0").toInt
      val blue = request.params.getOrElse("blue", "0").toInt
      val green = request.params.getOrElse("green", "0").toInt

      dispatcher ! Balls(Map(Red -> red, Green -> green, Blue -> blue))

      render.body("Ok").toFuture
    }
  }

  register(new ExampleApp())
}


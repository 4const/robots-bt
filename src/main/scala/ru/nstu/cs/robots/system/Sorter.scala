package ru.nstu.cs.robots.system

import akka.actor.{Props, Actor}
import ru.nstu.cs.robots.bluetooth.BtConnectorImpl
import ru.nstu.cs.robots.system.Sorter._
import ru.nstu.cs.robots.system.Dispatcher._
import ru.nstu.cs.robots.system.state._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

object Sorter {

  def props(id: Int): Props = Props(new Sorter(id))

  case object Ask
}

class Sorter(id: Int) extends Actor {

  val btConnector = new BtConnectorImpl(id)

  override def receive: Receive = {
    case Ask =>
      val state = btConnector.readState()
      if (state != 0) {
        context.parent ! Ball(color(state))
      }
  }

  scheduleAsk(10 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def color(state: Int): Color = {
    state match {
      case 1 => Red
      case 2 => Green
      case 3 => Blue
    }
  }
}

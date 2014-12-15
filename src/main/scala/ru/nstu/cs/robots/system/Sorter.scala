package ru.nstu.cs.robots.system

import akka.actor.{Props, Actor}
import ru.nstu.cs.robots.bluetooth.{Message, BtConnector}
import ru.nstu.cs.robots.system.Sorter._
import ru.nstu.cs.robots.system.Dispatcher._
import ru.nstu.cs.robots.system.state._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

object Sorter {

  def props(id: Int): Props = Props(new Sorter(id))

  case object Ask

  val askMessage = new Message(Array[Byte](0x00, 0x09, 0x00, 0x05, 0x03, 0x00, 0x00, 0x00))
}

class Sorter(id: Int) extends Actor {

  val btConnector = new BtConnector(id)

  override def receive: Receive = {
    case Ask =>
      btConnector.send(askMessage)
      val balls = mapBalls(btConnector.read())
      if (balls.exists(_._2 != 0)) {
        context.parent ! Balls(balls)
      }
  }

  scheduleAsk(10 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapBalls(bytes: Array[Byte]): Map[Color, Int] = {
    Map(Red -> bytes(12).toInt, Green -> bytes(13).toInt, Blue -> bytes(11))
  }

  private def color(state: Int): Color = {
    state match {
      case 1 => Red
      case 2 => Green
      case 3 => Blue
    }
  }
}

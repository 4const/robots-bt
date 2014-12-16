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
  val answerLength = 3
}

class Sorter(id: Int) extends Actor {

  val btConnector = new BtConnector(id)

  override def receive: Receive = {
    case Ask =>
      btConnector.send(askMessage)
      val balls = mapAnswer(btConnector.read(answerLength))
      if (balls.exists(_._2 != 0)) {
        context.parent ! Balls(balls)
      }
      scheduleAsk(5 seconds)
  }

  scheduleAsk(5 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapAnswer(bytes: Array[Byte]): Map[Color, Int] = {
    Map(Red -> bytes(1).toInt, Green -> bytes(2).toInt, Blue -> bytes(0))
  }
}

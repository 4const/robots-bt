package ru.nstu.cs.robots.system

import akka.actor.{Props, Actor}
import ru.nstu.cs.robots.bluetooth.{Message, BtConnector}
import ru.nstu.cs.robots.system.Sorter._
import ru.nstu.cs.robots.system.Dispatcher._
import ru.nstu.cs.robots.system.state._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

import akka.event.Logging

object Sorter {

  def props(id: Int): Props = Props(new Sorter(id))

  case object Ask

  val askMessage = new Message(Array[Byte](0x00, 0x09, 0x00, 0x05, 0x03, 0x00, 0x00, 0x00))
  val answerLength = 3
}

class Sorter(id: Int) extends Actor {

  val log = Logging(context.system, this)

  val btConnector = new BtConnector(id)

  override def receive: Receive = {
    case Ask =>
      log.info("Read sorter state")
      btConnector.send(askMessage)
      val balls = mapAnswer(btConnector.read(answerLength))

      log.info("Sorter has {}", balls)
      if (balls.exists(_._2 != 0)) {
        context.parent ! Balls(balls)
      }
      scheduleAsk(30 seconds)
  }

  scheduleAsk(10 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapAnswer(bytes: Array[Byte]): Map[Color, Int] = {
    Map(Red -> bytes(1).toInt, Green -> bytes(2).toInt, Blue -> bytes(0))
  }
}

package ru.nstu.cs.robots.system

import akka.actor.{Actor, Props}
import akka.event.Logging
import ru.nstu.cs.robots.bluetooth.{Message, BtConnector}
import ru.nstu.cs.robots.map._
import ru.nstu.cs.robots.system.Dispatcher.TransporterReady
import ru.nstu.cs.robots.system.Transporter._
import ru.nstu.cs.robots.system.environment.Port
import ru.nstu.cs.robots.system.task.{Move, Drop, Stay, TransporterTask}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Transporter {

  def props(id: Int, start: Port): Props = Props(new Transporter(id, start))

  case object Ask
  case class Do(task: TransporterTask)

  val askMessage = new Message(Array[Byte](0x00, 0x09, 0x00, 0x05, 0x09, 0x00, 0x00, 0x00))
  val answerLength = 1
  val completeAnswer: Byte = 5

  def taskMessage(n: Byte) = new Message(Array[Byte](0x00, 0x09, 0x00, 0x05, n, 0x00, 0x00, 0x00))
}

class Transporter(id: Int, start: Port) extends Actor {

  val log = Logging(context.system, this)

  val btConnector = new BtConnector(id)

  var current: TransporterTask = Stay(start.point, start.direction)

  override def receive: Receive = {
    case Do(task) =>
      btConnector.send(makeMessage(task))
      current = task

    case Ask =>
      log.info("Read Transporter {} state", id)
      btConnector.send(askMessage)

      val isComplete = mapAnswer(btConnector.read(1))
      log.info("Transporter {} ready? - {}", id, isComplete)

      if (isComplete) {
        context.parent ! TransporterReady(id)
      }

      scheduleAsk(10 seconds)
  }

  scheduleAsk(10 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapAnswer(bytes: Array[Byte]): Boolean = bytes(0) == completeAnswer

  private def makeMessage(task: TransporterTask): Message = {
    val keyByte: Byte = task match {
      case Drop(_, _) => 3
      case Stay(_, _) => 2
      case Move(_, _, lookAt) =>
        val relative = current.lookAt.relativeDirection(lookAt)
        log.info("make Transporter {} do {} - relative: {}", id, task, relative)

        relative match {
          case Top => 5
          case Right => 6
          case Bottom => 7
          case Left => 8
        }
    }

    taskMessage(keyByte)
  }
}
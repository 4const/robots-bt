package ru.nstu.cs.robots.system

import akka.actor.{Actor, Props}
import akka.event.Logging
import ru.nstu.cs.robots.map._
import ru.nstu.cs.robots.nxt.connection._
import ru.nstu.cs.robots.nxt.connection.mock.NxtConnectorTransporterMock
import ru.nstu.cs.robots.system.Dispatcher.TransporterReady
import ru.nstu.cs.robots.system.Transporter._
import ru.nstu.cs.robots.system.task._


import scala.concurrent.duration._

object Transporter {

  def props(id: Int, lookDirection: Direction, mock: Boolean): Props = Props(new Transporter(id, lookDirection, mock))

  case object Ask
  case class Do(task: TransporterRealTask)
  
  val completeAnswer: Byte = 5

  private def getConnector(id: Int, mock: Boolean): NxtConnector = {
    if (mock) {
      new NxtConnectorTransporterMock
    } else {
      new NxtConnectorImpl(id)
    }
  }
}

class Transporter(id: Int, lookDirection: Direction, mock: Boolean) extends Actor {

  val log = Logging(context.system, this)

  val connector = getConnector(id, mock)

  var logState: TransporterRealTask = RStay(lookDirection)
  var current: TransporterRealTask = RStay(lookDirection)

  implicit val executor = akka.dispatch.ExecutionContexts.global

  /**
   * Transporter always ignore RStay message
   */
  override def receive: Receive = {
    case Do(task: TransporterRealTask) =>
      val message = makeMessage(task)
      message.foreach { case m =>
        connector.send(m)
        current = task
      }
      logState = task

    case Ask =>
//      log.info("Read Transporter {} state", id)
      connector.send(AskMessage)

      val isReady = mapAnswer(connector.read(1))
      log.info("Transporter {} ready? - {}", id, isReady)

      if (isReady) {
        context.parent ! TransporterReady(id)
      }

      scheduleAsk(5.seconds)
  }

  scheduleAsk(15.seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapAnswer(bytes: Array[Byte]): Boolean = bytes(0) == completeAnswer

  private def makeMessage(task: TransporterRealTask):Option[NxtMessage] = {
    log.info("Transporter {} current state {}", id, logState)
    log.info("make Transporter {} do {}", id, task)

    task match {
      case RDrop(_) =>
        Some(DropMessage)
      case RMove(start, _) =>
        val relative = current.endDirection.relativeDirection(start)
        log.info("Transporter {} move relative: {}", id, relative)

        Some(
          relative match {
            case Top => ForwardMessage
            case Right => RightwardMessage
            case Bottom => BackwardMessage
            case Left => LeftwardMessage
          })
      case _ => None
    }
  }
}
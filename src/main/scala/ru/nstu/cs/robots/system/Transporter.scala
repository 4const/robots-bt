package ru.nstu.cs.robots.system

import akka.actor.{Actor, Props}
import akka.event.{LoggingAdapter, Logging}
import org.slf4j.LoggerFactory
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
      new NxtConnectorTransporterMock(id)
    } else {
      new NxtConnectorImpl(id)
    }
  }
}

class Transporter(id: Int, lookDirection: Direction, mock: Boolean) extends Actor {

  val log = LoggerFactory.getLogger(classOf[Transporter])

  val connector = getConnector(id, mock)

  var current: TransporterRealTask = RStay(lookDirection)

  implicit val executor = akka.dispatch.ExecutionContexts.global()


  override def receive: Receive = {
    case Do(task: TransporterRealTask) =>
      val message = makeMessage(task)
      message.foreach { case m =>
        connector.send(m)
      }
      current = task

    case Ask =>
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
//    log.info("Transporter {} current state {}", id, current)
    log.info("make Transporter {} do {}", id, task)

    task match {
      case RDrop(_) =>
        Some(DropMessage)
      case RMove(start, _) =>
        val relative = current.endDirection.relativeDirection(start)
//        log.info("Transporter {} move relative: {}", id, relative)

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
package ru.nstu.cs.robots.system

import akka.actor.{Props, Actor}
import org.slf4j.LoggerFactory
import ru.nstu.cs.robots.nxt.connection.mock.{NxtConnectorSorterMock, NxtConnectorTransporterMock}
import ru.nstu.cs.robots.nxt.connection.{NxtConnector, AskMessage, NxtConnectorImpl}
import ru.nstu.cs.robots.system.Sorter._
import ru.nstu.cs.robots.system.Dispatcher._
import ru.nstu.cs.robots.system.state._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

import akka.event.Logging

object Sorter {

  def props(id: Int, mock: Boolean): Props = Props(new Sorter(id, mock))

  case object Ask


  private val answerLength = 3

  private def getConnector(id: Int, mock: Boolean): NxtConnector = {
    if (mock) {
      new NxtConnectorSorterMock
    } else {
      new NxtConnectorImpl(id)
    }
  }
}

class Sorter(id: Int, mock: Boolean) extends Actor {

  val log = LoggerFactory.getLogger(classOf[Sorter])

  val connector = getConnector(id, mock)

  override def receive: Receive = {
    case Ask =>
      connector.send(AskMessage)
      val balls = mapAnswer(connector.read(answerLength))

      log.info("Sorter has {}", balls)
      if (balls.exists(_._2 != 0)) {
        context.parent ! Balls(balls)
      }
      scheduleAsk(10.seconds)
  }

  scheduleAsk(10.seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }

  private def mapAnswer(bytes: Array[Byte]): Map[Color, Int] = {
    Map(Red -> bytes(1).toInt, Green -> bytes(2).toInt, Blue -> bytes(0))
  }
}

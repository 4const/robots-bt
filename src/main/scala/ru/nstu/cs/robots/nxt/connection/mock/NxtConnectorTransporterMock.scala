package ru.nstu.cs.robots.nxt.connection.mock

import org.slf4j.LoggerFactory
import ru.nstu.cs.robots.nxt.connection._

import scala.concurrent.duration._

class NxtConnectorTransporterMock(id: Int) extends NxtConnector {

  val log = LoggerFactory.getLogger(classOf[NxtConnectorTransporterMock])

  var currentActionStartTime: Long = 0
  var currentActionDuration: Long = 0


  override def send(message: NxtMessage): Unit = {
    NxtConnector.increase()

    log.info("{} <- {}", id, message)
    message match {
      case AskMessage =>
      case _ => refreshState(message)
    }
  }

  override def read(length: Int): Array[Byte] = {
    val timeDelta = System.currentTimeMillis() - currentActionStartTime
    if (currentActionDuration <= timeDelta) {
      Array[Byte](5)
    } else {
      Array[Byte](9)
    }
  }

  private def refreshState(message: NxtMessage): Unit = {
    currentActionDuration = (message match {
      case ForwardMessage => 5
      case RightwardMessage => 7
      case BackwardMessage => 7
      case LeftwardMessage => 9
      case DropMessage => 2
    }).seconds.toMillis

    currentActionStartTime = System.currentTimeMillis()
  }
}

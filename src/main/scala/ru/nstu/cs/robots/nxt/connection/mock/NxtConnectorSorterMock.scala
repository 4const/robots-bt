package ru.nstu.cs.robots.nxt.connection.mock

import ru.nstu.cs.robots.nxt.connection._

import scala.concurrent.duration._
import scala.util.Random

class NxtConnectorSorterMock extends NxtConnector {

  val maxBalls = 0
  var currentlySorted = 0

  var lastBallDropTime: Long = 0

  override def send(message: NxtMessage): Unit = {
    message match {
      case _ =>
    }
  }

  override def read(length: Int): Array[Byte] = {
    val timeDelta = System.currentTimeMillis() - lastBallDropTime

    val res = Array[Byte](0, 0, 0)
    if (timeDelta >= 30.seconds.toMillis && currentlySorted < maxBalls) {
      val i = Random.nextInt(3)
      res.update(i, 1)

      currentlySorted += 1
    }

    res
  }
}

package ru.nstu.cs.robots.system

import akka.actor.{Actor, Props}
import ru.nstu.cs.robots.bluetooth.BtConnector
import ru.nstu.cs.robots.system.Dispatcher.TransporterReady
import ru.nstu.cs.robots.system.Transporter._
import ru.nstu.cs.robots.system.task.TransporterTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Transporter {

  def props(id: Int): Props = Props(new Transporter(id))

  case object Ask
  case class Do(task: TransporterTask)
}

class Transporter(id: Int) extends Actor {

  val btConnector = new BtConnector(id)

  override def receive: Receive = {
    case Do => ???

    case Ask =>
      val state = btConnector.read(1)
      if (state == 0) {
        context.parent ! TransporterReady(id)
      }
  }

  scheduleAsk(10 seconds)

  private def scheduleAsk(delay: FiniteDuration = 1.seconds): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, Ask)
  }
}

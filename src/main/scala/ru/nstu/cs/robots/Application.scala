package ru.nstu.cs.robots

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import example.LCPMessage
import jssc.{SerialPortException, SerialPort}
import ru.nstu.cs.robots.system.Dispatcher


object Application extends App {

//  override def main(args: Array[String]) {
//    val system = ActorSystem("system")
//
//    system.actorOf(Dispatcher.props(0, List(1, 2, 3)), "dispatcher")
//  }

  override def main(args: Array[String]) {
    // inicialization with selecting port for communication
    val serialPort = new SerialPort("COM9")

    try {
      // open port for communication
      serialPort.openPort()
      // baundRate, numberOfDataBits, numberOfStopBits, parity
      serialPort.setParams(9600, 8, 1, 0)
      // request nxt version
      val message = Array[Byte](0x00, 0x09, 0x00, 0x05, 0x01, 0x00, 0x00, 0x00)
      val messageLength = Array[Byte](message.length.toByte, 0x00)

      serialPort.writeBytes(messageLength)
      serialPort.writeBytes(message)

      val response = serialPort.readBytes(10, 5000)
      // close port
      val value = ByteBuffer.wrap(response.slice(4, 6).reverse)
      serialPort.closePort()
    } catch {
      case ex: SerialPortException => println(ex)
    }
  }
}

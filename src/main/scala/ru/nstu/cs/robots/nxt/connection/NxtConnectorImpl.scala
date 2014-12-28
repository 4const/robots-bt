package ru.nstu.cs.robots.nxt.connection

import jssc.SerialPort
import NxtConnectorImpl._

object NxtConnectorImpl {

  val metaLength = 3
  val metaByte: Byte = -128

  private def convertMessage(msg: NxtMessage): Array[Byte] =
    Array[Byte](0x00, 0x09, 0x00, 0x05, msg.keyByte, 0x00, 0x00, 0x00)
}

class NxtConnectorImpl(port: Int) extends NxtConnector {

  private val serial = new SerialPort("COM" + port)
  serial.openPort()

  override def send(message: NxtMessage): Unit = {
    val bytes = convertMessage(message)
    val length = Array[Byte](bytes.length.toByte, 0x00)
    serial.writeBytes(length)
    serial.writeBytes(bytes)
  }

  override def read(length: Int): Array[Byte] = {
    while (serial.readBytes(1)(0) != metaByte) {}

    val bytes = serial.readBytes(metaLength + length, 10000)

    bytes.slice(metaLength, bytes.length)
  }

  override def finalize(): Unit = {
    serial.closePort()
    super.finalize()
  }
}

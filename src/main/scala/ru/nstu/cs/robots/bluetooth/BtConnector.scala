package ru.nstu.cs.robots.bluetooth

import jssc.SerialPort

class BtConnector(port: Int) {

  val metaLength = 3

  private val serial = new SerialPort("COM" + port)
  serial.openPort()

  def send(message: Message): Unit = {
    serial.writeBytes(message.length)
    serial.writeBytes(message.msg)
  }

  def read(length: Int): Array[Byte] = {
    while (serial.readBytes(1)(0) != -128) {}

    val bytes = serial.readBytes(metaLength + length, 10000)
    bytes.slice(metaLength, bytes.length)
  }

  override def finalize(): Unit = {
    serial.closePort()
    super.finalize()
  }
}

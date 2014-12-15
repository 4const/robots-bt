package ru.nstu.cs.robots.bluetooth

import jssc.SerialPort

class BtConnector(port: Int) {

  val metaLength = 3
  val metaByte: Byte = -128

  private val serial = new SerialPort("COM" + port)
  serial.openPort()

  def send(message: Message): Unit = {
    serial.writeBytes(message.length)
    serial.writeBytes(message.msg)
  }

  def read(length: Int): Array[Byte] = {
    while (serial.readBytes(1)(0) != metaByte) {}

    val bytes = serial.readBytes(metaLength + length, 10000)
    serial.purgePort(0xFF)

    bytes.slice(metaLength, bytes.length)
  }

  override def finalize(): Unit = {
    serial.closePort()
    super.finalize()
  }
}

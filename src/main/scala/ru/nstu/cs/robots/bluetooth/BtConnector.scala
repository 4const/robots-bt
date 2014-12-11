package ru.nstu.cs.robots.bluetooth

import jssc.SerialPort

class BtConnector(port: Int) {
  private val serial = new SerialPort("COM" + port)
  serial.openPort()

  def send(message: Message): Unit = {
    serial.writeBytes(message.length)
    serial.writeBytes(message.msg)
  }

  def read(): Array[Byte] = {
    Array.empty
  }

  override def finalize(): Unit = {
    serial.closePort()
    super.finalize()
  }
}

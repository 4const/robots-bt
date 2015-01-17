package ru.nstu.cs.robots.nxt.connection

trait NxtConnector {
  def send(message: NxtMessage)

  def read(): Byte = read(1)(0)
  def read(length: Int): Array[Byte]
}

object NxtConnector {
  var messageCount = 0

  def increase(): Unit = messageCount += 1
}

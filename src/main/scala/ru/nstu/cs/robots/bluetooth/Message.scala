package ru.nstu.cs.robots.bluetooth

class Message(val msg: Array[Byte]) {

  def length = Array[Byte](msg.length.toByte, 0x00)
}

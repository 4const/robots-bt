package ru.nstu.cs.robots.nxt.connection

trait NxtMessage {
  def keyByte: Byte
}

case object AskMessage extends NxtMessage {
  override def keyByte: Byte = 9
}

case object ForwardMessage extends NxtMessage {
  override def keyByte: Byte = 5
}

case object RightwardMessage extends NxtMessage {
  override def keyByte: Byte = 6
}

case object BackwardMessage extends NxtMessage {
  override def keyByte: Byte = 7
}

case object LeftwardMessage extends NxtMessage {
  override def keyByte: Byte = 8
}

case object DropMessage extends NxtMessage {
  override def keyByte: Byte = 3
}



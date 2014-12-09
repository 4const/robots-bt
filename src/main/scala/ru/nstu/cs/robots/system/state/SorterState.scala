package ru.nstu.cs.robots.system.state


case class SorterState(queue: Seq[Color])

trait Color

case object Red extends Color
case object Green extends Color
case object Blue extends Color
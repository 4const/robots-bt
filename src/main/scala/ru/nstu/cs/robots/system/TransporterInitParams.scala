package ru.nstu.cs.robots.system

object TransporterInitParams {

  def unapply(params: TransporterInitParams): Option[(Int, Int, Boolean)] = Some((params.port, params.parking, params.mock))
}

class TransporterInitParams(val port: Int, val parking: Int, val mock: Boolean)
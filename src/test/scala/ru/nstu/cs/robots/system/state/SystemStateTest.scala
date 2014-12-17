package ru.nstu.cs.robots.system.state

import org.junit.Assert._
import org.junit.Test
import ru.nstu.cs.robots.map._
import ru.nstu.cs.robots.system.environment.TransportMaps
import ru.nstu.cs.robots.system.task._

class SystemStateTest {

  @Test
  def testGetNewTransporterTaskQueue(): Unit = {
    val id = 1

    val map = TransportMaps(1)
    val sorterState = SorterState(Map(Red -> 1, Green -> 0, Blue -> 0))

    val start = map.parkingPorts.head
    val transporters = Map(
      id -> TransporterState(QStay(start.point, start.direction), Seq.empty))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(1)

    assertEquals(QMove(start.point, 7, Right), tasks(id))
  }

  @Test
  def testWaitBeforeOneMove(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(Map(Red -> 1, Green -> 0, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(QStay(2, Top), Seq(QMove(2, 1, Bottom))),
      id2 -> TransporterState(QMove(5, 3, Left), Seq(QMove(3, 1, Left))))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(id2)

    assertEquals(QMove(2, 1, Bottom), tasks(id1))
  }

  @Test
  def testOneTaskForTwoRedBalls(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(Map(Red -> 3, Green -> 1, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(QStay(8, Right), Seq.empty),
      id2 -> TransporterState(QStay(16, Left), Seq.empty))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(id2)

    assertEquals(1, tasks.size)
    assertEquals(QMove(8, 7, Right), tasks(id1))
  }
}

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
      id -> TransporterState(Stay(start.point, start.direction), Seq.empty))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(1)

    assertEquals(Move(start.point, 7, Right), tasks(id))
  }

  @Test
  def testWaitBeforeOneMove(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(Map(Red -> 1, Green -> 0, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(Stay(2, Top), Seq(Move(2, 1, Bottom))),
      id2 -> TransporterState(Move(5, 3, Left), Seq(Move(3, 1, Left))))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(id2)

    assertEquals(Move(2, 1, Bottom), tasks(id1))
  }

  @Test
  def testOneTaskForTwoRedBalls(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(Map(Red -> 2, Green -> 0, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(Stay(8, Right), Seq.empty),
      id2 -> TransporterState(Stay(16, Left), Seq.empty))

    val system = new SystemState(sorterState, transporters, NoColor, map)

    val tasks = system.transporterReady(id2)

    assertEquals(1, tasks.size)
    assertEquals(Move(8, 7, Right), tasks(id1))
  }
}

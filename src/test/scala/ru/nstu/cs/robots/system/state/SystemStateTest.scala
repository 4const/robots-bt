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
    val sorterState = SorterState(NoColor, Map(Red -> 1, Green -> 0, Blue -> 0))

    val start = map.parkingPorts.head
    val transporters = Map(
      id -> TransporterState(QStay(start.point, start.direction), Seq.empty))

    val system = new SystemState(sorterState, transporters, Map(), map)

    val tasks = system.transporterReady(1).tasks
    assertEquals(QMove(start.point, 7, Right), tasks(id))
  }

  @Test
  def testWaitBeforeOneMove(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(NoColor, Map(Red -> 1, Green -> 0, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(QStay(2, Top), Seq(QMove(2, 1, Bottom))),
      id2 -> TransporterState(QMove(5, 3, Left), Seq(QMove(3, 1, Left))))

    val system = new SystemState(sorterState, transporters, Map(), map)

    val tasks = system.transporterReady(id2).tasks
    assertEquals(QMove(2, 1, Bottom), tasks(id1))
  }

  @Test
  def testOneTaskForTwoRedBalls(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(NoColor, Map(Red -> 3, Green -> 1, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(QStay(8, Right), Seq.empty),
      id2 -> TransporterState(QStay(16, Left), Seq.empty))

    val system = new SystemState(sorterState, transporters, Map(), map)

    val tasks = system.transporterReady(id2).tasks
    assertEquals(2, tasks.size)
    assertEquals(QMove(8, 7, Right), tasks(id1))
    assertEquals(QMove(16, 15, Left), tasks(id2))
  }

  @Test
  def testOneWaitWhileSecondDrop(): Unit = {
    val id1 = 1
    val id2 = 2

    val map = TransportMaps(1)
    val sorterState = SorterState(NoColor, Map(Red -> 0, Green -> 0, Blue -> 0))

    val transporters = Map(
      id1 -> TransporterState(QDrop(12, Bottom), Seq(QMove(12, 13, Top))),
      id2 -> TransporterState(QStay(13, Right), Seq(QMove(13, 11, Right), QMove(11, 12, Bottom))))

    val system = new SystemState(sorterState, transporters, Map(), map)

    val tasks = system.transporterReady(id2).tasks
    assertEquals(1, tasks.size)
    assertEquals(QStay(13, Right), tasks(id2))
  }
}

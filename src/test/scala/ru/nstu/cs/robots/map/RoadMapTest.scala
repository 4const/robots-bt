package ru.nstu.cs.robots.map

import org.junit.{Assert, Test}
import ru.nstu.cs.robots.system.environment.TransportMaps

class RoadMapTest {

  val crossroads = TransportMaps(1).crossroads

  @Test
  def testSourceAsDestination(): Unit = {
    val roadMap = new RoadMap(crossroads)

    val result = roadMap.getWay(2, 2)

    Assert.assertEquals(Seq(2), result.map(_.id))
  }

  @Test
  def testFromStartToSorter(): Unit = {
    val roadMap = new RoadMap(crossroads)

    val result = roadMap.getWay(8, 2)

    Assert.assertEquals(Seq(8, 7, 5, 3, 1 ,2), result.map(_.id))
  }

  @Test
  def testFromSorterToPackager(): Unit = {
    val roadMap = new RoadMap(crossroads)

    val result = roadMap.getWay(4, 12)

    Assert.assertEquals(Seq(4, 3, 1, 13, 11, 12), result.map(_.id))
  }
}

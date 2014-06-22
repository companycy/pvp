package server.pvp.config

import scala.xml.XML
import scala.collection.mutable.{ArrayBuffer, HashMap}
import server.pvp.NpcInfo


/**
 * Created by bjcheny on 6/21/14.
 */
object MapInfoConfig {
  val xml = XML.loadFile("/Users/bjcheny/Documents/lnu_online/lnuonline_fromserver/config/MapInfo.xml")

  lazy val item = "item"
  lazy val id = "id"

  val allMapInfo = readConfig()

  private def readConfig(): HashMap[Int, MapInfo] = {
    val items = xml.\(item).map { node =>
      val id = node.\(this.id).text.toInt
      new MapInfo(id)
    }

    val result = new HashMap[Int, MapInfo]()
    for (i <- items) {
      result(i.id) = i
    }

    return result
  }

  def getMapById(id: Int): MapInfo = {
    if (allMapInfo.contains(id)) {
      return allMapInfo(id)
    } else {
      return new MapInfo(0)
    }
  }

}


class MapInfo(val id: Int) {

  val maxPlayer: Byte = 0
  var wave = 0 // default to 1
  val npcInfoArr = new ArrayBuffer[Array[NpcInfo]] // get from config xml

  val intro: String = "" // todo: from config xml


  def getCurrentNpcArr(): Array[NpcInfo] = {
    wave += 1
    return npcInfoArr.apply(this.wave - 1)
  }

  def newWave(): Int = {
    wave += 1
    return wave
  }

}


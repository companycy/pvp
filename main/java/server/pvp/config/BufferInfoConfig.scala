package server.pvp.config

import scala.xml.XML
import scala.collection.mutable.HashMap
import server.pvp.{Buff, BufferInfo}

/**
 * Created by bjcheny on 6/19/14.
 */
object BufferInfoConfig {
  val xml = XML.loadFile("/Users/bjcheny/Documents/lnu_online/lnuonline_fromserver/config/BasicBuffInfo.xml")

  lazy val item = "item"
  lazy val id = "id"
  lazy val keyName = "keyName"
  lazy val buffType = "buffType"
  lazy val hitChance = "hitChance"
  lazy val duration = "duration"
  lazy val param1 = "param1"
  lazy val param2 = "param2"
  lazy val triggerInterval = "triggerInterval"

  val allBuffInfoMap = run()

  private def run(): HashMap[Byte, BufferInfo] = {
    val items = xml.\(item).map { node =>
      val id = node.\(this.id).text.toByte
      val keyName = node.\(this.keyName).text
      val buffType = node.\(this.buffType).text.toByte
      val hitChance = node.\(this.hitChance).text.toFloat
      val duration = node.\(this.duration).text.toFloat
      val param1 = node.\(this.param1).text
      val param2 = node.\(this.param2).text
      val triggerInterval = node.\(this.triggerInterval).text.toFloat
      new BufferInfo(id, keyName, buffType, hitChance, duration, param1, param2)
    }

    val result = new HashMap[Byte, BufferInfo]()
    for (i <- items) {
      result(i.id) = i
    }
    return result
  }

  def getBuffById(id: Byte): BufferInfo = {
    if (allBuffInfoMap.contains(id)) {
      return allBuffInfoMap(id)
    } else {
      return new BufferInfo(0, "default", 0, 0, 0, "", "")
    }
  }
}

package server.pvp

import scala.xml.XML

/**
 * Created by bjcheny on 6/20/14.
 */
class BasicEquipmentInfo {
  val xml = XML.loadFile("/Users/bjcheny/Documents/lnu_online/lnuonline_fromserver/config/BasicEquipmentInfo.xml")

  lazy val item = "item"
  lazy val id = "id"
  lazy val keyName = "keyName"
  lazy val prefabName = "prefabName"
  lazy val slotCount = "slotCount"
  lazy val canEquipedJobs = "canEquipedJobs"
  lazy val equipmentType = "equipmentType"
  lazy val equipmentQuality = "equipmentQuality"
  lazy val level = "level"
  lazy val minPhysicalAtk = "minPhysicalAtk"
  lazy val maxPhysicalAtk = "maxPhysicalAtk"

  val allEquipmentInfoMap = run()

  private def run() {
    val items = xml.\(item).map { node =>
      val id = node.\(this.id).text.toByte
      val keyName = node.\(this.keyName).text
      val prefabName = node.\(this.prefabName).text
      val slotCount = node.\(this.slotCount).text
      val canEquipedJobs = node.\(this.canEquipedJobs).text
      val equipmentType = node.\(this.equipmentType).text
      val equipmentQuality = node.\(this.equipmentQuality).text
      val level = node.\(this.level).text
      val minPhysicalAtk = node.\(this.minPhysicalAtk).text
      val maxPhysicalAtk = node.\(this.maxPhysicalAtk).text
    }
  }

}

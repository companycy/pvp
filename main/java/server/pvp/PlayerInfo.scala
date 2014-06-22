package server.pvp

import scala.collection.mutable.ArrayBuffer
import equipmentinfo.XEquipmentInfoThrift

/**
 * Created by bjcheny on 6/14/14.
 */
class PlayerInfo(override val userId: Int, override val name: Array[Byte]) extends CharInfo(name, userId) {
  var roomId = 0

  val equipmentArr = new ArrayBuffer[XEquipmentInfoThrift]()

  // calc from all equipments
  private var minPhysicalAtk: Int = 0
  private var maxPhysicalAtk: Int = 0
  private var armor: Int = 0
  private var elementDef: Int = 0
  private var elementAtk: Int = 0
  private var luckValue: Int = 0
  private var greed: Double = 0
  private var hpRatio: Double = 0
  private var armorRatio: Double = 0
  private var evasion: Double = 0
  private var moveSpeed: Double = 0
  private var attackSpeed: Double = 0
  private var hitRate: Double = 0
  private var mpRegen: Double = 0
  private var criticalRate: Double = 0
  private var criticalDmgRatio: Double = 0
  private var crushingBlowChance: Double = 0
  private var dealtDmgDiscount: Double = 0
  private var dmgImmortalChance: Double = 0
  private var hpDrain: Int = 0
  private var dmgReflective: Int = 0

  def getEquipmentInfo(): Unit = {

  }

  def getAttributes(): Unit = {
    for (i <- equipmentArr) {
      this.minPhysicalAtk += i.minPhysicalAtk
      this.maxPhysicalAtk += i.maxPhysicalAtk
      this.armor += i.armor
      this.elementDef += i.elementDef
      this.elementAtk += i.elementAtk
      this.luckValue += i.luckValue
      this.greed += i.greed
      this.hpRatio += i.hpRatio
      this.armorRatio += i.armorRatio
      this.evasion += i.evasion
      this.moveSpeed += i.moveSpeed
      this.attackSpeed += i.attackSpeed
      this.hitRate += i.hitRate
      this.mpRegen += i.mpRegen
      this.criticalRate += i.criticalRate
      this.criticalDmgRatio += i.criticalDmgRatio
      this.crushingBlowChance += i.crushingBlowChance
      this.dealtDmgDiscount += i.dealtDmgDiscount
      this.dmgImmortalChance += i.dmgImmortalChance
      this.hpDrain += i.hpDrain
      this.dmgReflective += i.dmgReflective
    }
  }


}

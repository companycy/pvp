package server.pvp

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

/**
 * Created by bjcheny on 6/14/14.
 */
class CharInfo(val name: Array[Byte], val userId: Int = 0) {
  var id: Byte = 1

  var joystickPos = new CharJoystickPosition(0, 0)
  val joystickPosArr = new ArrayBuffer[CharJoystickPosition]() // recved from client

  var position = new Position(0, 0, 0)
  var positionArr = new ArrayBuffer[CharPosition]() // recved from client

  var rotation = new Rotation(0)
  var rotationArr = new ArrayBuffer[CharRotation]() // recved from client

  var status = new Status(0)
  var statusArr = new ArrayBuffer[CharStatus]() // recved from client

  var startedSkillArr = new ArrayBuffer[StartedSkill]() // recved from client
  var triggeredSkillArr = new ArrayBuffer[TriggeredSkill]() // recved from client

  val buffList = new HashMap[Byte, Buff]() // all buff list on this player
  val appliedBuffList = new HashMap[Byte, Buff]() // calc dynamically

  var level = 0

  val maxHp: Float = 10000
  var hp: Float = maxHp
  val maxMp: Float = 200
  var mp: Float = maxMp

  def updateState(charId: Int, state: CharState) = {
  }

  def calcAppliedBuff() {
    for (i <- this.buffList) {
      if (i._2.isToApply()) {
        this.appliedBuffList(i._1) = i._2 // get appliedbufflist
      }
    }
  }

  // def isAppliedBuffValid(): Boolean = {
  //   this.calcAppliedBuff()
  //   return this.appliedBuffList.nonEmpty
  // }

  def updateByBuff() {
    this.updateHpByBuff()
    this.updateMpByBuff()

    // clear it manually by room
    // this.clearAppliedBuff()
  }

  def updateHpByBuff() {
    for (i <- this.appliedBuffList) {
      if (i._2.isHp()) {
        this.hp = this.hp - i._2.getBuffValue()
        i._2.updateDuration()
      }
    }
  }

  def updateMpByBuff() {
    for (i <- this.appliedBuffList) {
      if (i._2.isMp()) {
        this.mp = this.mp - i._2.getBuffValue()
        i._2.updateDuration()
      }
    }
  }

  def getId = this.id

  def updateBufflist(buff: Buff) {
    buffList(buff.id) = buff
  }

  // def updateBufflist(appliedSkillList: Array[AppliedSkill]): Unit = {
  //   for (i <- appliedSkillList) {
  //     val buffId = i.buff.id
  //     buffList(buffId) = i.buff // todo:
  //   }
  // }

  def isBuffListEmpty(): Boolean = {
    return this.getBufflist().isEmpty
  }

  def isBuffListNonEmpty(): Boolean = {
    return !this.isBuffListEmpty()
  }

  def getBufflist(): HashMap[Byte, Buff] = {
    return this.buffList
  }

  def getBufflistSize(): Byte = {
    return this.getBufflist().size.toByte
  }

  def getAppliedBuff(): HashMap[Byte, Buff] = {
    return this.appliedBuffList
  }

  def getAppliedBuffSize(): Byte = {
    return this.getAppliedBuff.size.toByte
  }

  def isAppliedBuffNonEmpty(): Boolean = {
    return this.getAppliedBuff().nonEmpty
  }

  def clearBufflist() = {
    this.buffList.retain((k, v) => v.isValid())
  }

  def clearAppliedBuff() = { // get applied bufflist every time from bufflist
    this.appliedBuffList.clear()
  }

  def checkPosition(): Boolean = {
    return position.isValid()
  }

  def updatePosition(position: Position) {
    this.position = position
  }

  def updateRotation(rotation: Rotation) {
    this.rotation = rotation
  }

}

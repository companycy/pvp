package server.pvp

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import server.pvp.config.MapInfoConfig

/**
 * Created by bjcheny on 6/14/14.
 */
case object RoomConst {
  val playerIdBase: Byte = 1
  val npcIdBase: Int = 101
}


class Room(val name: Array[Byte], val maxPlayer: Byte, val userId: Int, val mapId: Int = 2) {
  var id = 1

  var playerIdGenerator = RoomConst.playerIdBase
  var npcIdGenerator = RoomConst.npcIdBase

  val ctimestamp: Long = System.currentTimeMillis()
  val info = new Array[Byte](10)

  val mapInfo = MapInfoConfig.getMapById(mapId)

  val charInfoMap = new HashMap[Int, CharInfo]()
  charInfoMap(playerIdGenerator) = new PlayerInfo(userId, name)

  for (i <- mapInfo.getCurrentNpcArr()) {
    charInfoMap(npcIdGenerator) = i
    npcIdGenerator += 1
  }

  val readyStatus = false
  val hostPlayerId: Byte = playerIdGenerator
  playerIdGenerator = (playerIdGenerator + 1).toByte

  val hostUserId: Int = userId

  def isFull(): Boolean = {
    return this.charInfoMap.size == this.maxPlayer
  }

  def isEmpty(): Boolean = {
    return !this.nonEmpty()
  }

  def nonEmpty(): Boolean = {
    return this.charInfoMap.nonEmpty
  }

  def getCurrentPlayerCnt(): Byte = {
    return this.charInfoMap.size.toByte
  }

  def getMaxPlayerCnt(): Byte = {
    return this.maxPlayer
  }

  def isNpc(npcId: Int): Boolean = {
    return npcId > RoomConst.npcIdBase
  }

  def pushToPlayerInfoList(userId: Int, name: Array[Byte]): PlayerInfo = {
    val playerInfo = new PlayerInfo(userId, name)

    playerInfo.id = playerIdGenerator
    playerIdGenerator = (playerIdGenerator + 1).toByte

    charInfoMap(playerInfo.id) = playerInfo

    return playerInfo
  }

  def popFromPlayerInfoList(userId: Int) = {
    val result = this.charInfoMap.find(i => i._2.userId == userId)
    if (result.nonEmpty && this.charInfoMap.contains(result.head._1)) {
      this.charInfoMap.remove(result.head._1)
    }
  }

  def getPlayerInfoByUserId(userId: Int): CharInfo = {
    val result = this.charInfoMap.find(i => i._2.userId == userId) // filter(i=>i._2.userId == userId)
    // return result.getOrElse(new PlayerInfo(0, ("default").getBytes()))

    if (result.nonEmpty) {
      return result.head._2
    } else {
      return new PlayerInfo(0, ("default").getBytes())
    }
  }

  def getPlayerInfoByCharId(charId: Byte): CharInfo = {
    if (charInfoMap.contains(charId)) {
      return charInfoMap(charId)
    } else {
      return new PlayerInfo(0, ("default").getBytes())
    }
  }

  def getCtimestamp: Long = {
    return ctimestamp
  }

  def checkOthersPosition(charInfo: CharInfo): Boolean = {
    var isValid = true
    for (i <- this.charInfoMap if isValid) {
      isValid &= i._2.checkPosition()
    }
    return isValid
  }

  def updateAllPlayersByBuff() {
    for (i <- this.charInfoMap) {
      i._2.updateByBuff()
    }
    // this.updateHpByBuff()
    // this.updateMpByBuff()
  }

  def clearAllPlayersAppliedBuff() = {
    for (i <- this.charInfoMap) {
      i._2.clearAppliedBuff()
    }
  }

  def updateHpByBuff() {
    for (i <- this.charInfoMap) {
      i._2.updateHpByBuff()
    }
  }

  def updateMpByBuff() {
    for (i <- this.charInfoMap) {
      i._2.updateMpByBuff()
    }
  }

  def updateStartedSkill(arr: ArrayBuffer[StartedSkill], charInfo: CharInfo) {
    for (i <- arr) {
      // this.getPlayerInfoByCharId(i.charId).updateStartedSkill(i.startedSkill)
    }
  }

  def updateRotation(arr: ArrayBuffer[CharRotation], charInfo: CharInfo) {
    for (i <- arr) {
      this.getPlayerInfoByCharId(i.charId).updateRotation(i.rotation)
    }
  }

  def updateStatus(arr: ArrayBuffer[CharStatus], charInfo: CharInfo) {
    for (i <- arr) {
      this.getPlayerInfoByCharId(i.charId).status = i.status
    }
  }

  def updatePosition(arr: ArrayBuffer[CharPosition], charInfo: CharInfo) {
    for (i <- arr) {
      this.getPlayerInfoByCharId(i.charId).updatePosition(i.position)
    }
  }

  def updateBufflist(triggeredSkillArr: Array[TriggeredSkill]) {
    for (i <- triggeredSkillArr) {
      for (j <- i.appliedSkillList) {
        val playerInfo = this.getPlayerInfoByCharId(j.charId) // target
        playerInfo.updateBufflist(j.buff)                     // update target bufflist
      }
    }
  }

  def isBuffListNonEmpty(): Boolean = {
    var result = false
    for (i <- this.charInfoMap if !result) {
      result |= i._2.isBuffListNonEmpty()
    }

    return result
  }

  def calcAppliedBuff(): Boolean = { // figure out if appliedbuff is empty
    for (i <- this.charInfoMap) {
      i._2.calcAppliedBuff() // calc first
    }

    return this.isAppliedBuffNonEmpty() // return if they are empty
  }

  def clearAppliedBuff() = {
    for (i <- this.charInfoMap) {
      i._2.clearAppliedBuff()
    }
  }

  def clearBufflist() = {
    for (i <- this.charInfoMap) {
      i._2.clearBufflist()
    }
  }

  def isAppliedBuffNonEmpty(): Boolean = {
    var result = false
    for (i <- this.charInfoMap if !result) {
      result |= i._2.isAppliedBuffNonEmpty()
    }

    return  result
  }

}


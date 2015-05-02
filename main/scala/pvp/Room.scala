package server.pvp

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

/**
 * Created by bjcheny on 6/14/14.
 */
class Room(val name: Array[Byte], val maxPlayer: Byte, val userId: Int) {
  var id = 1
  val ctimestamp: Long = System.currentTimeMillis()
  val info = new Array[Byte](10)

  // todo: const
  val playerIdBase: Byte = 1
  var playerIdGenerator: Byte = 1
  val npcIdBase: Int = 101
  var npcIdGenerator: Byte = 101

  val playerInfoMap = new HashMap[Int, PlayerInfo]()
  playerInfoMap(playerIdGenerator) = new PlayerInfo(userId, name)

  val npcInfoArr = new ArrayBuffer[NpcInfo]()

  val readyStatus = false
  val hostPlayerId: Byte = playerIdGenerator
  playerIdGenerator = (playerIdGenerator + 1).toByte

  val hostUserId: Int = userId

  def isFull(): Boolean = {
    return this.playerInfoMap.size == this.maxPlayer
  }

  def isEmpty(): Boolean = {
    return !this.nonEmpty()
  }

  def nonEmpty(): Boolean = {
    return this.playerInfoMap.nonEmpty
  }

  def getCurrentPlayerCnt(): Byte = {
    return this.playerInfoMap.size.toByte
  }

  def getMaxPlayerCnt(): Byte = {
    return this.maxPlayer
  }

  def isNpc(npcId: Int): Boolean = {
    return npcId > this.npcIdBase
  }

  def pushToPlayerInfoList(userId: Int, name: Array[Byte]): PlayerInfo = {
    val playerInfo = new PlayerInfo(userId, name)

    playerInfo.id = playerIdGenerator
    playerIdGenerator = (playerIdGenerator + 1).toByte

    playerInfoMap(playerInfo.id) = playerInfo

    return playerInfo
  }

  def popFromPlayerInfoList(userId: Int) = {
    val result = this.playerInfoMap.find(i => i._2.userId == userId)
    if (result.nonEmpty && this.playerInfoMap.contains(result.head._1)) {
      this.playerInfoMap.remove(result.head._1)
    }
  }

  def getPlayerInfoByUserId(userId: Int): PlayerInfo = {
    val result = this.playerInfoMap.find(i => i._2.userId == userId) // filter(i=>i._2.userId == userId)
    if (result.nonEmpty) {
      return result.head._2
    } else {
      return new PlayerInfo(0, ("default").getBytes())
    }
  }

  def getPlayerInfoByCharId(charId: Byte): PlayerInfo = {
    if (playerInfoMap.contains(charId)) {
      return playerInfoMap(charId)
    } else {
      return new PlayerInfo(0, ("default").getBytes())
    }
  }

  def getCtimestamp: Long = {
    return ctimestamp
  }

  def getId(): Int = {
    return this.id
  }

  def setId(id: Int) {
    this.id = id
  }

  def checkOthersPosition(playerInfo: PlayerInfo): Boolean = {
    var isValid = true
    for (i <- this.playerInfoMap if isValid) {
      isValid &= i._2.checkPosition()
    }
    return isValid
  }

  def updateAllPlayersByBuff() {
    for (i <- this.playerInfoMap) {
      i._2.updateByBuff()
    }
    // this.updateHpByBuff()
    // this.updateMpByBuff()
  }

  def clearAllPlayersAppliedBuff() = {
    for (i <- this.playerInfoMap) {
      i._2.clearAppliedBuff()
    }
  }

  def updateHpByBuff() {
    for (i <- this.playerInfoMap) {
      i._2.updateHpByBuff()
    }
  }

  def updateMpByBuff() {
    for (i <- this.playerInfoMap) {
      i._2.updateMpByBuff()
    }
  }

  def updateStartedSkill(arr: ArrayBuffer[StartedSkill], playerInfo: PlayerInfo) {
    for (i <- arr) {
      // this.getPlayerInfoByCharId(i.charId).updateStartedSkill(i.startedSkill)
    }
  }

  def updateRotation(arr: ArrayBuffer[CharRotation], playerInfo: PlayerInfo) {
    for (i <- arr) {
      this.getPlayerInfoByCharId(i.charId).updateRotation(i.rotation)
    }
  }

  def updateStatus(arr: ArrayBuffer[CharStatus], playerInfo: PlayerInfo) {
    for (i <- arr) {
      this.getPlayerInfoByCharId(i.charId).status = i.status
    }
  }

  def updatePosition(arr: ArrayBuffer[CharPosition], playerInfo: PlayerInfo) {
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
    for (i <- this.playerInfoMap if !result) {
      result |= i._2.isBuffListNonEmpty()
    }

    return result
  }

  def calcAppliedBuff(): Boolean = { // figure out if appliedbuff is empty
    for (i <- this.playerInfoMap) {
      i._2.calcAppliedBuff() // calc first
    }

    return this.isAppliedBuffNonEmpty() // return if they are empty
  }

  def clearAppliedBuff() = {
    for (i <- this.playerInfoMap) {
      i._2.clearAppliedBuff()
    }
  }

  def clearBufflist() = {
    for (i <- this.playerInfoMap) {
      i._2.clearBufflist()
    }
  }

  def isAppliedBuffNonEmpty(): Boolean = {
    var result = false
    for (i <- this.playerInfoMap if !result) {
      result |= i._2.isAppliedBuffNonEmpty()
    }

    return  result
  }

}


package server.pvp

import java.nio.{ByteOrder, ByteBuffer}
import scala.collection.mutable.ArrayBuffer
import server.pvp.redisclient.PubsubUtil
import server.pvp.redisclient.PubsubUtil.pubJedis


/**
 * Created by bjcheny on 6/14/14.
 */
class Pvpserver {
  val lobby: Lobby = new Lobby()

  private def writeHeader(ret: ArrayBuffer[Byte], userId: Int = 0,
    msgType: Byte, timestamp: Long) = {
    if (userId != 0) {
      ret.++=(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(userId).array())
    }

    ret.+=((0xef).toByte)
    ret.+=((0xfe).toByte)

    ret.+=(msgType)
    val difference =
      ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(System.currentTimeMillis() - timestamp)
    ret.++=(difference.array())
  }

  //todo: ends with 0
  private def getName(recvedData: ByteBuffer): Array[Byte] = {
    val name = new Array[Byte](recvedData.remaining-1)
    recvedData.get(name, 0, recvedData.remaining-1)
    return name
  }

  private def getUserId(recvedData: ByteBuffer): Int = {
    return recvedData.getInt
  }

  def getRoomlist(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()

    writeHeader(
      ret,
      userId,
      ServerConst.RESPONSE_ROOMLIST.toByte,
      System.currentTimeMillis()
    )

    val roomList = lobby.getRoomlist()
    val roomCnt = lobby.getRoomCnt().toByte
    ret.+=(roomCnt)
    for (i <- roomList) {
      ret.++=(
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i._1).array()
      )
      ret.+=(i._2.getCurrentPlayerCnt())
      ret.+=(i._2.getMaxPlayerCnt())

      ret.++=(i._2.name) // room name
      ret.+=(0.toByte)

      ret.++=(i._2.info) // room info
      ret.+=(0.toByte)

      ret.+=(i._2.hostPlayerId)

      ret.++=(i._2.name) // todo: hostName
      ret.+=(0.toByte)
    }

    return ret.toArray
  }

  def getPositionList(recvedData: ByteBuffer, num: Byte, room: Room): ArrayBuffer[CharPosition] = {
    val ret = new ArrayBuffer[CharPosition]()
    try
    {
      for (i <- 0 until num) {
        val charId = recvedData.get()
        val x = recvedData.getShort()
        val y = recvedData.getShort()
        val z = recvedData.getShort()
        ret.+=(new CharPosition(charId, x, y, z))
      }
    } catch {
      case e => e.printStackTrace
    }

    return ret
  }

  def getRotationList(recvedData: ByteBuffer, num: Byte): ArrayBuffer[CharRotation] = {
    val ret = new ArrayBuffer[CharRotation]()
    try {
      for (i <- 0 until num) {
        val charId = recvedData.get()
        val rotation = recvedData.getShort()
        ret.+=(new CharRotation(charId, rotation))
      }
    } catch {
      case e => e.printStackTrace
    }
    return ret
  }

  def getStatusList(recvedData: ByteBuffer, num: Byte): ArrayBuffer[CharStatus] = {
    val ret = new ArrayBuffer[CharStatus]()
    try {
      for (i <- 0 until num) {
        val charId = recvedData.get()
        val status = recvedData.get()
        ret.+=(new CharStatus(charId, status))
      }
    } catch {
      case e => {
        e.printStackTrace()
      }
    }
    return ret
  }

  def getStartedSkillList(recvedData: ByteBuffer, num: Byte): ArrayBuffer[StartedSkill] = {
    val ret = new ArrayBuffer[StartedSkill]()
    try {
      for (i <- 0 until num) {
        val charId = recvedData.get()
        val skillId = recvedData.get()
        ret.+=(new StartedSkill(charId, skillId))
      }
    } catch {
      case e => e.printStackTrace
    }

    return ret
  }

  def getHpList(recvedData: ByteBuffer, num: Byte): ArrayBuffer[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    for (i <- 0 until num) {
    }

    return ret
  }

  /*
   struct character_apply_buff_info
   {
   byte character_id;

   byte buff_id; 
   float value; // 当该buff的buff_type为Damage时为伤害值
   }
   */
  def getAppliedSkillList(recvedData: ByteBuffer, num: Byte): Array[AppliedSkill] = {
    val ret = new ArrayBuffer[AppliedSkill]()
    for (i <- 0 until num) {
      val charId = recvedData.get()
      val buffId = recvedData.get()
      val buffValue = recvedData.getFloat()
      ret.+=(new AppliedSkill(charId, buffId, buffValue))
    }
    return ret.toArray
  }


  /*
   trigger_skill_list_field // 角色的技能击中目标事件列表
   {
   byte skills_num;
   struct character_trigger_skill_info[skills_num];
   }

   struct character_trigger_skill_info
   {
   byte actor_id; // 技能施放者ID
   byte skill_id; // 造成该buff的技能ID, 将喝药水视为特殊技能, 可以分配特定技能ID
   byte buffs_num;
   struct character_apply_buff_info[buffs_num];
   }
   */
  def getTriggeredSkillList(recvedData: ByteBuffer, num: Byte): ArrayBuffer[TriggeredSkill] = {
    val ret = new ArrayBuffer[TriggeredSkill]()
    try {
      for (i <- 0 until num) {
        val charId = recvedData.get()
        val skillId = recvedData.get()
        val num = recvedData.get()
        val arr = getAppliedSkillList(recvedData, num)
        ret.+=(new TriggeredSkill(charId, skillId, arr))
      }
    } catch {
      case e => e.printStackTrace
    }

    return ret
  }

  def writePositionList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    ret.+=((room.getCurrentPlayerCnt() - 1).toByte) // skip main player

    val newRet = new ArrayBuffer[Byte]()
    for (i <- room.charInfoMap if i._2.userId != charInfo.userId) {
      for (j <- i._2.positionArr) {
        ret.+=(j.charId)
        ret.++=(
          ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(j.position.x).array()
        )
        ret.++=(
          ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(j.position.y).array()
        )
        ret.++=(
          ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(j.position.z).array()
        )
      }
    }
  }

  def writeRotationList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    ret.+=((room.getCurrentPlayerCnt() - 1).toByte) // skip main player

    for (i <- room.charInfoMap if i._2.userId != charInfo.userId) {
      for (j <- i._2.rotationArr) {
        ret.+=(j.charId)
        ret.++=(
          ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(j.rotation.rotation).array()
        )
      }
    }
  }

  def writeStatusList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    ret.+=((room.getCurrentPlayerCnt() - 1).toByte) // skip main player

    for (i <- room.charInfoMap if i._2.userId != charInfo.userId) {
      for (j <- i._2.statusArr) {
        ret.+=(j.charId)
        ret.+=(j.status.status)
      }
    }
  }

  def writeStartedSkillList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) { // todo:
    // ret.+=((room.getCurrentPlayerCnt()).toByte)

    val newRet = new ArrayBuffer[Byte]()
    writeHeader(
      newRet,
      0,
      ServerConst.RESPONSE_STATUS.toByte,
      room.ctimestamp
    )

    val resField1 = ServerConst.MAIN_STARTED_SKILLID
    newRet.+=(resField1.toByte)

    val resField2 = 0
    newRet.+=(resField2.toByte)

    newRet.+=(charInfo.startedSkillArr.size.toByte)
    for (i <- charInfo.startedSkillArr) {
      newRet.+=(i.charId)
      newRet.+=(i.skillId)
    }
    charInfo.startedSkillArr.clear()

    // for (i <- room.charInfoMap) { // if i._2.userId != userId) { // send skill to other
    //   for (j <- i._2.startedSkillArr) {
    //     // ret.+=(j.charId)
    //     // ret.+=(j.skillId)
    //     newRet.+=(j.charId)
    //     newRet.+=(j.skillId)
    //   }
    // }

    // for (i <- room.charInfoMap) { // if i._2.userId == userId) { // then clear skill list
    // i._2.startedSkillArr.clear()
    // }

    broadcast(room, newRet.toArray, charInfo.id)
  }

  def writeMpList(ret: ArrayBuffer[Byte], room: Room, userId: Int) {
    ret.+=((room.getCurrentPlayerCnt()).toByte) // all players' mp

    for (i <- room.charInfoMap) {
      ret.+=(i._2.id)
      ret.++=(
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(i._2.mp).array()
      )
    }
  }

  def writeHpList(ret: ArrayBuffer[Byte], room: Room, userId: Int) {
    ret.+=((room.getCurrentPlayerCnt()).toByte) // all players' mp

    for (i <- room.charInfoMap) {
      ret.+=(i._2.id)
      ret.++=(
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(i._2.hp).array()
      )
    }
  }

  def writeBuffList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    // ret.+=(charInfo.getBufflistSize) // all buff list

    val newRet = new ArrayBuffer[Byte]()
    var buffCnt = 0
    for (i <- room.charInfoMap) {
      for (j <- i._2.buffList) {
        buffCnt += 1
        newRet.+=(i._2.id) // charId
        newRet.+=(j._2.id) // buffId
        newRet.++=(
          ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(j._2.ctimestamp).array()
        )
      }
    }

    if (buffCnt != 0 && newRet.nonEmpty) {
      ret.+=(buffCnt.toByte)
      ret.++=(newRet)
    }
  }

  /*
   current_buffs_list_field // 角色当前buff列表
   {
   byte buffs_num;
   struct character_buff_info[buffs_num];
   }

   struct character_buff_info
   {
   byte character_id;
   byte buff_id;
   float created_time; // buff创建的时间戳, 同时可以用于标识不同的buff
   }

   apply_buffs_list_field // 角色应用buff列表
   {
   byte buffs_num;
   struct character_apply_buff_info[buffs_num];
   }

   struct character_apply_buff_info
   {
   byte character_id;
   byte buff_id;
   float value;
   }
   */
  def writeAppliedBuffList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    // ret.+=(charInfo.getAppliedBuffSize) //

    val newRet = new ArrayBuffer[Byte]()
    var buffCnt = 0
    for (i <- room.charInfoMap) {
      for (j <- i._2.appliedBuffList) {
        buffCnt += 1
        newRet.+=(i._2.id) // charId
        newRet.+=(j._2.id) // buffId
        newRet.++=(
          ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(j._2.getBuffValue()).array()
        ) // buffValue
      }
    }

    if (buffCnt != 0 && newRet.nonEmpty) {
      ret.+=(buffCnt.toByte)
      ret.++=(newRet.toArray)
    }
  }

  def writeJoystickPosList(ret: ArrayBuffer[Byte], room: Room, charInfo: CharInfo) {
    ret.+=((room.getCurrentPlayerCnt() - 1).toByte)

    for (i <- room.charInfoMap if i._2.userId != charInfo.userId) {
      ret.+=(i._2.joystickPos.x)
      ret.+=(i._2.joystickPos.z)
    }
  }

  def broadcastStatus(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    val reqField = recvedData.get()

    val room = lobby.getRoomByUserId(userId)
    val playerInfo = room.getPlayerInfoByUserId(userId)

    val joystickPosField: Int = reqField & ServerConst.JOYSTICK // todo:
    if (joystickPosField.!=(Int.box(0))) {
      val x: Byte = recvedData.get()
      val z: Byte = recvedData.get()
      playerInfo.joystickPos = new CharJoystickPosition(x, z)
    }

    val positionField = reqField & ServerConst.POSITION_LIST
    if (positionField.!=(Int.box(0))) {
      val num = recvedData.get()
      val arr = getPositionList(recvedData, num, room)
      playerInfo.positionArr = arr

      room.updatePosition(arr, playerInfo)
    }

    val rotationField = reqField & ServerConst.ROTATION_LIST
    if (rotationField.!=(Int.box(0))) {
      val num = recvedData.get()
      val arr = getRotationList(recvedData, num)
      playerInfo.rotationArr = arr

      room.updateRotation(arr, playerInfo)
    }

    val statusField = reqField & ServerConst.STATUS_LIST
    if (statusField.!=(Int.box(0))) {
      val num = recvedData.get()
      val arr = getStatusList(recvedData, num)
      playerInfo.statusArr = arr

      room.updateStatus(arr, playerInfo)
    }

    val startedSkillField = reqField & ServerConst.STARTEDSKILLID_LIST
    if (startedSkillField.!=(Int.box(0))) {
      val num = recvedData.get()
      val arr = getStartedSkillList(recvedData, num)
      playerInfo.startedSkillArr = arr

      room.updateStartedSkill(arr, playerInfo)
    }

    val triggeredSkillField = reqField & ServerConst.TRIGGEREDSKILL_LIST
    if (triggeredSkillField.!=(Int.box(0))) {
      val num = recvedData.get()
      val arr = getTriggeredSkillList(recvedData, num)
      playerInfo.triggeredSkillArr = arr

      room.updateBufflist(arr.toArray)
    }

    var resField1 = 0
    var resField2 = 0

    writeHeader(
      ret,
      userId,
      ServerConst.RESPONSE_STATUS.toByte,
      room.ctimestamp
    )

    if (room.checkOthersPosition(playerInfo)) {
      resField1 |= ServerConst.MAIN_POSITION // write in response
    }

    resField1 |= ServerConst.MAIN_ROTATION
    resField1 |= ServerConst.MAIN_STATUS

    // resField1 |= ServerConst.MAIN_STARTED_SKILLID // use startedSkillField instead

    resField1 |= ServerConst.MAIN_HP

    resField1 |= ServerConst.MAIN_MP

    if (room.isBuffListNonEmpty()) { // all players' buff list
      resField1 |= ServerConst.MAIN_BUFF_LIST
    }

    if (room.calcAppliedBuff()) { // all players' applied buff list
      resField1 |= ServerConst.MAIN_APPLY_BUFF_LIST
    }

    // todo
    // resField2 |= ServerConst.RIVAL_JOYSTICKPOS

    ret.+=(resField1.toByte)
    ret.+=(resField2.toByte)

    val resPositionField = resField1 & ServerConst.MAIN_POSITION
    if (resPositionField.!=(0.toInt)) {
      writePositionList(ret, room, playerInfo)
    }

    val resRotationField = resField1 & ServerConst.MAIN_ROTATION
    if (resRotationField.!=(0.toInt)) {
      writeRotationList(ret, room, playerInfo)
    }

    val resStatusField = resField1 & ServerConst.MAIN_STATUS
    if (resStatusField.!=(0.toInt)) {
      writeStatusList(ret, room, playerInfo)
    }

    val resStartedSkillField = startedSkillField // resField1 & ServerConst.MAIN_STARTED_SKILLID
    if (resStartedSkillField.!=(0.toInt)) {
      writeStartedSkillList(ret, room, playerInfo) // need broadcast to all
    }

    room.updateAllPlayersByBuff() // calc all players based on appliedbuff

    val resHpField = resField1 & ServerConst.MAIN_HP // after appliedbuff
    if (resHpField.!=(0.toInt)) {
      writeHpList(ret, room, userId)
    }

    val resMpField = resField1 & ServerConst.MAIN_MP // after appliedbuff
    if (resMpField.!=(0.toInt)) {
      writeMpList(ret, room, userId)
    }

    val resBuffField = resField1 & ServerConst.MAIN_BUFF_LIST
    if (resBuffField.!=(0.toInt)) {
      writeBuffList(ret, room, playerInfo)
      room.clearBufflist()
    }

    val resAppliedBuffField = resField1 & ServerConst.MAIN_APPLY_BUFF_LIST
    if (resAppliedBuffField.!=(0.toInt)) {
      writeAppliedBuffList(ret, room, playerInfo)
      room.clearAppliedBuff()
    }

    val resJoystickPosField = resField2 & ServerConst.RIVAL_JOYSTICKPOS
    if (resJoystickPosField.!=(0.toInt)) {
      writeJoystickPosList(ret, room, playerInfo)
    }

    return ret.toArray
  }

  //create player when create room
  def createRoom(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()

    val maxPlayer = recvedData.get()
    val name = "test" + userId // getName(recvedData)
    val room = lobby.getNewRoom(name.getBytes(), maxPlayer, userId)

    writeHeader(
      ret,
      userId,
      ServerConst.RESPONSE_CREATEROOM.toByte,
      room.ctimestamp
    )

    val status: Byte = 0
    ret.+=(status)
    ret.++=(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(room.id).array())
    ret.+=(room.hostPlayerId)

    return ret.toArray
  }

  def broadcastJoinOrLeaveRoom(charInfo: CharInfo, room: Room, msgType: Byte) = {
    val ret = new ArrayBuffer[Byte]()
    writeHeader(
      ret,
      0,
      msgType,
      room.ctimestamp
    )

    ret.+=(room.getCurrentPlayerCnt())
    ret.+=(room.maxPlayer)
    ret.+=(charInfo.id)
    ret.++=(charInfo.name)
    ret.+=(0.toByte)
    broadcast(room, ret.toArray, charInfo.id)
  }

  def broadcast(room: Room, ret: Array[Byte], id: Byte = 0) = {
    for (i <- room.charInfoMap if i._2.id != id) {
      val userId = i._2.userId // send msg by userId
      val retWithUserId = new ArrayBuffer[Byte](ServerConst.userIdLength + ret.size)
      retWithUserId.++=(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(userId).array())
      retWithUserId.++=(ret)
      PubsubUtil.pubJedis.publish(PubsubUtil.pubchannel.getBytes, retWithUserId.toArray)
    }
  }

  def broadcastStartgame(userId: Int, room: Room) = {
    val ret = new ArrayBuffer[Byte]()
    writeHeader(
      ret,
      0,
      ServerConst.BROADCAST_STARTGAME.toByte,
      room.ctimestamp
    )

    val status: Byte = 0
    ret.+=(status)
    ret.+=(room.getCurrentPlayerCnt()) // only player cnt

    for (i <- room.charInfoMap) {
      ret.+=(i._2.id)
      // ret.+=(i._2.id % 2 )       // todo: camp id
      ret.+=(i._2.id) // work as spawn point
    }

    //no npc needed

    // val playerInfoArr = room.playerInfoArr.filter(p => p.userId == userId)
    // broadcast(playerInfoArr.apply(0).id, room, ret.toArray, )
    broadcast(room, ret.toArray)
  }

  def getReady(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()

    return ret.toArray
  }

  def cancelReady(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    return ret.toArray
  }

  def quitGame(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    return ret.toArray
  }

  def getMatcher(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    return ret.toArray
  }

  def startGame(userId: Int, recvedData: ByteBuffer) = {
    val ret = new ArrayBuffer[Byte]()
    val roomId = recvedData.getInt()
    val room = lobby.getRoom(roomId)

    writeHeader(
      ret,
      0,
      ServerConst.BROADCAST_STARTGAME.toByte,
      room.ctimestamp
    )

    val status: Byte = 0
    ret.+=(status)

    broadcastStartgame(userId, room)
  }

  def leaveLobby(userId: Int, recvedData: ByteBuffer): Unit = {
    val roomId = lobby.getRoomIdByUserId(userId)
    val newRet = // recvedData has no useful data now
      ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(roomId)
    newRet.flip() // reset position to 0
    leaveRoom(userId, newRet)
  }

  def leaveRoom(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()

    val roomId = recvedData.getInt()
    val room = lobby.getRoom(roomId)
    writeHeader(
      ret,
      userId,
      ServerConst.RESPONSE_LEAVEROOM.toByte,
      room.ctimestamp
    )

    val playerInfo = room.getPlayerInfoByUserId(userId)
    room.popFromPlayerInfoList(userId)

    val status = 0
    ret.+=(status.toByte)

    //todo: broadcast to other players
    broadcastJoinOrLeaveRoom(
      playerInfo,
      room,
      ServerConst.BROADCAST_PLAYER_LEAVE.toByte
    )

    if (room.isEmpty()) {
      lobby.popFromRoomlist(room.id)
    }

    return ret.toArray
  }

  //create player when join room
  def joinRoom(userId: Int, recvedData: ByteBuffer): Array[Byte] = {
    val ret = new ArrayBuffer[Byte]()
    val roomId = recvedData.getInt()
    lobby.addToUseridRoomIdMap(userId, roomId)

    val room = lobby.getRoom(roomId)
    val isFull = room.isFull()

    writeHeader(
      ret,
      userId,
      ServerConst.RESPONSE_JOINROOM.toByte,
      room.ctimestamp
    )

    val status: Byte = 0
    ret.+=(status)
    ret.++=(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(roomId).array())
    ret.+=(room.hostPlayerId)

    val name = new Array[Byte](0) // todo:
    val playerInfo = room.pushToPlayerInfoList(userId, name)
    ret.+=(playerInfo.id)
    ret.+=(room.maxPlayer)
    ret.+=(room.getCurrentPlayerCnt())

    for (i <- room.charInfoMap if i._2.id != playerInfo.id) {
      ret.+=(i._2.id)
      ret.++=(i._2.name)
      ret.+=(Byte.box(0))
      val readyStatus: Byte = 0
      ret.+=(readyStatus)
    }

    //todo: broadcast to other players
    broadcastJoinOrLeaveRoom(
      playerInfo,
      room,
      ServerConst.BROADCAST_PLAYER_JOIN.toByte
    )

    return ret.toArray
  }

}

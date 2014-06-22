package server.pvp

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap


/**
 * Created by bjcheny on 6/14/14.
 */
class Lobby {
  // append, update and random access take constant time (amortized time).
  // Prepends and removes are linear in the buffer size.
  val roomMap = new HashMap[Int, Room]()

  val roomIdBase = 1
  var roomIdGenerator = roomIdBase

  val useridRoomIdMap = HashMap[Int, Int]()

  def this(id: Int) = {
    this()
  }

  private def getDefaultRoom(): Room = {
    val name = ("default").getBytes()
    val maxPlayer = 0.toByte
    val userId = 0
    return new Room(name, maxPlayer, userId)
  }

  def getRoom(roomId: Int): Room = {
    if (roomMap.contains(roomId)) {
      return roomMap(roomId)
    } else {
      return getDefaultRoom() // todo:
    }
  }

  def getRoomIdByUserId(userId: Int): Int = {
    if (useridRoomIdMap.contains(userId)) {
      return useridRoomIdMap(userId)
    } else {
      return 0
    }
  }

  def getRoomByUserId(userId: Int): Room = {
    return this.getRoom(this.getRoomIdByUserId(userId))
  }

  def addToUseridRoomIdMap(userId: Int, roomId: Int) = {
    useridRoomIdMap(userId) = roomId
  }

  def getRoomlist(): HashMap[Int, Room] = {
    return roomMap.retain((k, v) => v.nonEmpty()) // (p => p._2.nonEmpty())
  }

  def getRoomCnt(): Int = {
    return roomMap.size
  }

  def getNewRoom(name: Array[Byte], maxPlayer: Byte, userId: Int): Room = {
    val room = new Room(name, maxPlayer, userId) // todo: need to get map info

    room.id = roomIdGenerator
    roomIdGenerator = roomIdGenerator + 1

    roomMap(room.id) = room

    useridRoomIdMap(userId) = room.id
    return room
  }

  def popFromRoomlist(roomId: Int): Boolean = {
    val result = true
    return result
  }
}

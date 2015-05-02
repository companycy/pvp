package server.pvp

/**
 * Created by bjcheny on 6/14/14.
 */
class CharPosition(val charId: Byte, val x: Short, val y: Short, val z: Short) {
  val position = new Position(x, y, z) 
}

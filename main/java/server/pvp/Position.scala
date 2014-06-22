package server.pvp

/**
 * Created by bjcheny on 6/19/14.
 */
class Position(val x: Short, val y: Short, val z: Short) {
  def isNonValid(): Boolean = {
    return (this.x == 0) && (this.y == 0) && (this.z == 0)
  }

  def isValid(): Boolean = {
    return !this.isNonValid()
  }

}

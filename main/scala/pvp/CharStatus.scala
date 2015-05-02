package server.pvp

/**
 * Created by bjcheny on 6/14/14.
 */
class CharStatus(val charId: Byte, val status_c: Byte) {
  val status = new Status(status_c)
}

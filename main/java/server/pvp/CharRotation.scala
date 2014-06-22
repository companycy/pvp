package server.pvp

/**
 * Created by bjcheny on 6/14/14.
 */
class CharRotation(val charId: Byte, rotation_c: Short) {
  val rotation = new Rotation(rotation_c)
}

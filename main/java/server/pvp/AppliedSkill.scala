package server.pvp

/**
 * Created by bjcheny on 6/18/14.
 */
class AppliedSkill(val charId: Byte, val buffId: Byte, val buffValue: Float) {
  val buff = new Buff(buffId, buffValue)
}

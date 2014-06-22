package server.pvp

/**
 * Created by bjcheny on 6/18/14.
 */
class Buff(val id: Byte, buffValue_c: Float, val delta: Int = 1000) {
  val value = getDefaultBuffValue()

  val ctimestamp = System.currentTimeMillis()
  var utimestamp = ctimestamp

  var duration: Int = getDuration()

  private def getDefaultBuffValue(): Float = {
    if (buffValue_c != 0.toFloat) {
      return buffValue_c
    } else {
      return 10
    }
  }

  private def getDuration(): Int = {
    if (this.id > 24) {
      return 5000
    } else {
      return 0
    }
  }

  // def isContinuous(): Boolean = {
  //   if (this.id == this.damageBuffId) { // todo:
  //     return false
  //   } else {
  //     return this.duration > 0
  //   }
  // }

  def getBuffValue(): Float = {
    val now = System.currentTimeMillis()
    if (this.id == 8 || this.id == 1) {
      return this.value
    } else if (this.duration > 0 && now - this.utimestamp > this.delta) {
      // this.utimestamp = now
      // if (this.duration - this.delta > 0) {
      //   this.duration = this.duration - this.delta
      // } else {
      //   this.duration = 0
      // }
      return this.value
    } else {
      return 0.toFloat
    }
  }

  def isMp(): Boolean = {
    return false
  }

  def isHp(): Boolean = {
    return true
  }

  def isToApply(): Boolean = {
    return this.getBuffValue() > 0
  }

  def isValid(): Boolean = { // used in CharInfo.clearBuffList
    val now = System.currentTimeMillis()
    if (this.id == 1) {
      return false
    } else if (this.duration < this.delta || this.value == 0) {
      return false
    } else {
      return true
    }
  }

  def updateDuration() = {
    val now = System.currentTimeMillis()
    this.utimestamp = now
    if (this.duration - this.delta > 0) {
      this.duration = this.duration - this.delta
    } else {
      this.duration = 0
    }
  }

}

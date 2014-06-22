package server.pvp

/**
 * Created by bjcheny on 6/14/14.
 */
class CharState(val position: CharPosition, val rotation: CharRotation,
  val joystickPos: CharJoystickPosition, val status: CharStatus, val startedSkill: StartedSkill) {

  // def this(position: CharPosition, rotation: CharRotation,
  //           joystickPos: CharJoystickPosition, status: CharStatus,
  //           startedSkill: StartedSkill) = {
  //   this()
  //   this.position = position
  //   this.rotation = rotation
  //   this.joystickPos = joystickPos
  //   this.status = status
  //   this.startedSkill = startedSkill
  // }

}

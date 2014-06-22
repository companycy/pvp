package server.equipment;

/**
 * Created by bjcheny on 6/12/14.
 */
public class WeaponInfo {
  //common
  private int minPhysicalAtk;
  private int maxPhysicalAtk;

  //normal
  private int elementAtk;
  private int luckValue;
  private float greed;
  private float hitRate;
  private float mpRegen;

  //bonus
  private float criticalRate;
  private float criticalDmgRatio;
  private float crushingBlowChance;

  //best
  private int hpDrain;
}

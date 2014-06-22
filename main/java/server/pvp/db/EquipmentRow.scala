package server.pvp.db

/**
 * Created by bjcheny on 6/22/14.
 */

object EquipmentTbl {
  val dbname = "new_project"
  val tblname = "base_equipments_lnu"

  val _id = "id"
  val _item_id = "item_id"
  val _display_name = "display_name"
  val _lvl = "lvl"
  val _min_physical_atk = "min_physical_atk"
  val _max_physical_atk = "max_physical_atk"
  val _armor = "armor"
  val _element_def = "element_def"
  val _element_atk = "element_atk"
  val _element_atk_type = "element_atk_type"
  val _luck_value = "luck_value"
  val _greed = "greed"
  val _hp_ratio = "hp_ratio"
  val _armor_ratio = "armor_ratio"
  val _evasion = "evasion"
  val _move_speed = "move_speed"
  val _attack_speed = "attack_speed"
  val _hit_rate = "hit_rate"
  val _mp_regen = "mp_regen"
  val _critical_rate = "critical_rate"
  val _critical_dmg_ratio = "critical_dmg_ratio"
  val _crushing_blow_chance = "crushing_blow_chance"
  val _delta_dmg_discount = "delta_dmg_discount" // todo:
  val _dmg_immortal_chance = "dmg_immortal_chance"
  val _hp_drain = "hp_drain"
  val _dmg_reflective = "dmg_reflective"

  val connection = JdbcUtil.connection

  // val statement = JdbcUtil.statement

  def query(row: EquipmentRow, id: Int): Unit = {
    val prep =
      connection.prepareStatement(
        " SELECT * FROM " + this.tblname
          + " where " + this._id
          + " = ? " + " LIMIT 1 "
      )
    prep.setString(1, id.toString)
    val resultSet = prep.executeQuery
    while (resultSet.next) {
      row.itemId = resultSet.getInt(this._item_id)
      row.displayName = resultSet.getString(this._display_name)
      row.lvl = resultSet.getInt(this._lvl)
      row.minPhysicalAtk = resultSet.getInt(this._min_physical_atk)
      row.maxPhysicalAtk = resultSet.getInt(this._max_physical_atk)
      row.armor = resultSet.getInt(this._armor)
      row.elementDef = resultSet.getInt(this._element_def)
      row.elementAtk = resultSet.getInt(this._element_atk)
      row.elementAtkType = resultSet.getInt(this._element_atk_type)
      row.luckValue = resultSet.getInt(this._luck_value)
      row.greed = resultSet.getDouble(this._greed)
      row.hpRatio = resultSet.getDouble(this._hp_ratio)
      row.armorRatio = resultSet.getDouble(this._armor_ratio)
      row.evasion = resultSet.getDouble(this._evasion)
      row.moveSpeed = resultSet.getDouble(this._move_speed)
      row.attackSpeed = resultSet.getDouble(this._attack_speed)
      row.hitRate = resultSet.getDouble(this._hit_rate)
      row.mpRegen = resultSet.getDouble(this._mp_regen)
      row.criticalRate = resultSet.getDouble(this._critical_rate)
      row.criticalDmgRatio = resultSet.getDouble(this._critical_dmg_ratio)
      row.crushingBlowChance = resultSet.getDouble(this._crushing_blow_chance)
      row.dealtDmgDiscount = resultSet.getDouble(this._delta_dmg_discount)
      row.dmgImmortalChance = resultSet.getDouble(this._dmg_immortal_chance)
      row.hpDrain = resultSet.getInt(this._hp_drain)
      row.dmgReflective = resultSet.getInt(this._dmg_reflective)
    }
    row.id = id
  }


  def update(id: Int): Unit = {
    // val prep =
    //   connection.prepareStatement("INSERT INTO quotes (quote, author) VALUES (?, ?)") // insert
    // prep.setString(1, "")
    // prep.setString(2, "")
    // val result =prep.executeUpdate
  }

  def test(): Boolean = {
    val row = new EquipmentRow()
    this.query(row, 1)
    return true
  }

}

class EquipmentRow extends TblRow {

  var itemId: Int = _
  var displayName: String = _
  var lvl: Int = _
  var minPhysicalAtk: Int = _
  var maxPhysicalAtk: Int = _
  var armor: Int = _
  var elementDef: Int = _
  var elementAtk: Int = _
  var elementAtkType: Int = _
  var luckValue: Int = _
  var greed: Double = _
  var hpRatio: Double = _
  var armorRatio: Double = _
  var evasion: Double = _
  var moveSpeed: Double = _
  var attackSpeed: Double = _
  var hitRate: Double = _
  var mpRegen: Double = _
  var criticalRate: Double = _
  var criticalDmgRatio: Double = _
  var crushingBlowChance: Double = _
  var dealtDmgDiscount: Double = _
  var dmgImmortalChance: Double = _
  var hpDrain: Int = _
  var dmgReflective: Int = _
}



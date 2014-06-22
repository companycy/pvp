package server.pvp.db

/**
 * Created by bjcheny on 6/22/14.
 */
object PlayerTbl {
  val dbname = "new_project"
  val tblname = "base_players_lnu"

  val _id = "id"
  val _user_id = "user_id"
  val _nickname = "nickname"
  val _sex = "sex"
  val _role_type = "role_type"
  val _lvl = "lvl"
  val _exp = "exp"
  val _atk = "atk"
  val _armor = "armor"
  val _defense = "def"
  val _element_def = "element_def"
  val _element_dmg_ratio = "element_dmg_ratio"
  val _hp = "hp"
  val _weapon = "weapon"
  val _gloves = "gloves"
  val _clothes = "clothes"
  val _helmet = "helmet"
  val _boots = "boots"
  val _ring = "ring"
  val _pendant = "pendant"
  val _skill = "skill"
  val _status = "status"
  val _title = "title"
  val _channel = "channel"
  val _zone = "zone"
  val _server_id = "server_id"
  val _vip_lv = "vip_lv"
  val _created_at = "created_at"
  val _updated_at = "updated_at"
  val _deleted_at = "deleted_at"

  val connection = JdbcUtil.connection

  def query(row: PlayerRow, id: Int): Unit = {
    val prep = 
      connection.prepareStatement(
        " SELECT * FROM " + this.tblname
          + " where " + this._id
          + " = ? " + " LIMIT 1 "
      )
    prep.setString(1, id.toString)
    val resultSet = prep.executeQuery
    while (resultSet.next) {
      row.user_id = resultSet.getInt(this._user_id)
      row.nickname = resultSet.getString(this._nickname)
      row.sex = resultSet.getInt(this._sex)
      row.role_type = resultSet.getInt(this._role_type)
      row.lvl = resultSet.getInt(this._lvl)
      row.exp = resultSet.getInt(this._exp)
      row.atk = resultSet.getInt(this._atk)
      row.armor = resultSet.getInt(this._armor)
      row.defense = resultSet.getInt(this._defense)
      row.element_def = resultSet.getDouble(this._element_def)
      row.element_dmg_ratio = resultSet.getDouble(this._element_dmg_ratio)
      row.hp = resultSet.getInt(this._hp)
      row.weapon = resultSet.getInt(this._weapon)
      row.gloves = resultSet.getInt(this._gloves)
      row.clothes = resultSet.getInt(this._clothes)
      row.helmet = resultSet.getInt(this._helmet)
      row.boots = resultSet.getInt(this._boots)
      row.ring = resultSet.getInt(this._ring)
      row.pendant = resultSet.getInt(this._pendant)
      row.skill = resultSet.getString(this._skill)
      row.status = resultSet.getString(this._status)
      row.title = resultSet.getString(this._title)
      row.channel = resultSet.getInt(this._channel)
      row.zone = resultSet.getInt(this._zone)
      row.server_id = resultSet.getInt(this._server_id)
      row.vip_lv = resultSet.getInt(this._vip_lv)
      row.created_at = resultSet.getInt(this._created_at)
      row.updated_at = resultSet.getInt(this._updated_at)
      row.deleted_at = resultSet.getInt(this._deleted_at)
    }
    row.id = id
  }

  def update(id: Int): Unit = {
  }

  def test(): Boolean = {
    val row = new PlayerRow()
    this.query(row, 1)
    return true
  }

}

class PlayerRow extends TblRow {

  var user_id: Int = _
  var nickname: String = _
  var sex: Int = _
  var role_type: Int = _
  var lvl: Int = _
  var exp: Int = _
  var atk: Int = _
  var armor: Int = _
  var defense: Int = _
  var element_def: Double = _
  var element_dmg_ratio: Double = _
  var hp: Int = _
  var weapon: Int = _
  var gloves: Int = _
  var clothes: Int = _
  var helmet: Int = _
  var boots: Int = _
  var ring: Int = _
  var pendant: Int = _
  var skill: String = _
  var status: String = _
  var title: String = _
  var channel: Int = _
  var zone: Int = _
  var server_id: Int = _
  var vip_lv: Int = _
  var created_at: Int = _
  var updated_at: Int = _
  var deleted_at: Int = _
}

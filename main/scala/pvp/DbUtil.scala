package server.pvp

import java.sql.{ResultSet, DriverManager, Connection}

/**
 * Created by bjcheny on 6/20/14.
 */
class DbUtil {
  val conn_str = "jdbc:mysql://localhost:3306/DBNAME?user=root&password="

  def run() {
    val driver = "com.mysql.jdbc.Driver"
    val dbname = "new_project"
    val url = "jdbc:mysql://localhost/" + dbname
    val username = "root"
    val password = ""

    // val log = Logging(context.system, this)

    Class.forName(driver) // make connection
    var connection:Connection = DriverManager.getConnection(url, username, password)
    try {
      val statement = connection.createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_UPDATABLE
      ) // create statement and run query


      val resultSet =
        statement.executeQuery("SELECT * FROM base_equipment_type_new") // query
      while (resultSet.next) {
        val equipmentType = resultSet.getString("type")
        val equipment = resultSet.getString("equipment")
        println("type, equipment = " + equipmentType + ", " + equipment)
      }

      val prep = 
        connection.prepareStatement("INSERT INTO quotes (quote, author) VALUES (?, ?)") // insert
      prep.setString(1, "")
      prep.setString(2, "")
      prep.executeUpdate


    } catch {
      case e => e.printStackTrace
    } finally {
      connection.close
    }

  }

}

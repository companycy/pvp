package server.pvp.db

import java.sql.{ResultSet, DriverManager, Connection}

/**
 * Created by bjcheny on 6/20/14.
 */

object JdbcUtil {
  val conn_str = "jdbc:mysql://localhost:3306/DBNAME?user=root&password="
  val driver = "com.mysql.jdbc.Driver"
  val dbname = "new_project"
  val url = "jdbc:mysql://localhost/" + dbname
  val username = "root"
  val password = ""

  // private var driverLoaded = false
  // private def loadDriver()  {
  //   try{
  //     Class.forName("com.mysql.jdbc.Driver").newInstance
  //     driverLoaded = true
  //   }catch{
  //     case e: Exception  => {
  //       println("ERROR: Driver not available: " + e.getMessage)
  //       throw e
  //     }
  //   }
  // }

  // def getConnection(dbc: DbConnection): Connection =  {
  //   // Only load driver first time
  //   this.synchronized {
  //     if(! driverLoaded) loadDriver()
  //   }

  //   // Get the connection
  //   try{
  //     DriverManager.getConnection(dbc.getConnectionString)
  //   }catch{
  //     case e: Exception  => {
  //       println("ERROR: No connection: " + e.getMessage)
  //       throw e
  //     }
  //   }
  // }

  Class.forName(driver) // make connection
  private val _connection: Connection = DriverManager.getConnection(url, username, password)

  // private val statement = connection.createStatement(
  //   ResultSet.TYPE_FORWARD_ONLY,
  //   ResultSet.CONCUR_UPDATABLE
  // ) // create statement and run query

  // try {
  // } catch {
  //   case e => e.printStackTrace
  // } finally {
  //   connection.close
  // }

  def connection = this._connection

}

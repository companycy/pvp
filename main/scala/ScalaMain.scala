
import server.pvp.db.{PlayerTbl, EquipmentRow, JdbcUtil}
import server.pvp.redisclient.PubsubUtil
import server.pvp.redisclient.LpushBrpopUtil
import server.thrift.ThriftServer
import test.TestIncr


/**
 * Created by bjcheny on 5/31/14.
 */





object ScalaMain extends App {
//  server.Damage.getHpDamage

PubsubUtil.run

//  ThriftServer.run
//  TestIncr.test

  // LpushBrpopUtil.run

  // val buffConfig = new BufferInfoConfig
  // val dbutil = new JdbcUtil
  // dbutil.run

  // PlayerTbl.test()
}




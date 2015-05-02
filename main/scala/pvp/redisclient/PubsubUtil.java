package server.pvp.redisclient;

/**
 * Created by bjcheny on 6/12/14.
 */


import redis.clients.jedis.*;
import server.pvp.Pvpserver;
import server.pvp.ServerConst;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// import server.pvp.ServerConst;

public class PubsubUtil extends BinaryJedisPubSub {
  private static JedisPoolConfig jedisPoolConfig;
  private static JedisPool jedisPool;

  private static Jedis subJedis;

  private static Pvpserver pvpserver = new Pvpserver();
  private static String subchannel = "room:request";

  public static String pubchannel = "room:response";
  public static Jedis pubJedis;


  @Override
  public void onMessage(byte[] channel, byte[] message) {
    // char[] chars = s2.toCharArray();
    // int dataLength = 2;
    // for (int i = 0; i < dataLength; ++i) {
    //   System.out.println((int) chars[i]);
    // }
    // char[] name = new char[4];
    // s2.getChars(dataLength, chars.length, name, 0);

    // char[] response = new char[4];
    // response[0] = 0x20;
    // pubJedis.publish("room:response", String.valueOf(response));
    // jedisPool.returnResource(pubJedis);

    byte[] ret = null;
    // if (true) {
    //   int i = 0x21ff;
    //   int j = 0xff34;
    //   // ret = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
    //   ret = ByteBuffer.allocate(4).putInt(i).array();
    //   byte[] newByte = new byte[ret.length+4];
    //   System.arraycopy(ret, 0, newByte, 0, ret.length);
    //   byte[] bb = ByteBuffer.allocate(4).putInt(j).array();
    //   System.arraycopy(bb,
    //           0, newByte, ret.length, 4);
    //   // pubJedis.publish(pubchannel.getBytes(), newByte);
    //   ByteOrder bo = ByteOrder.nativeOrder();
    //   pubJedis.publish(pubchannel.getBytes(),
    //                    ByteBuffer.allocate(newByte.length).order(ByteOrder.LITTLE_ENDIAN).put(newByte).array());
    //   return;
    // }

    // if (message.length > 12)            // 7 + 4 + 1
    //   System.out.print("msg length: " + message.length);
    ByteBuffer recvedData = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN);
    int userId = recvedData.getInt();

    byte field1 = recvedData.get();
    byte field2 = recvedData.get();

    byte msgType = recvedData.get();
    float clientTimestamp = recvedData.getFloat();
    // if (msgType != 5)
    //   System.out.println("  msgType: " + msgType);
    switch (msgType) {
      // case ServerConst.REQUEST_AUTH: {
      //   pvpserver.auth(recvedData);
      //   break;
      // }
      // case ServerConst.REQUEST_NTP: {
      //   pvpserver.syncClock(recvedData);
      //   break;
      // }
      case ServerConst.REQUEST_ROOMLIST: {
        ret = pvpserver.getRoomlist(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_CREATEROOM: {
        ret = pvpserver.createRoom(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_JOINROOM: {
        ret = pvpserver.joinRoom(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_LEAVEROOM: {
        ret = pvpserver.leaveRoom(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_GETREADY: {
        ret = pvpserver.getReady(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_CANCEL_READY: {
        ret = pvpserver.cancelReady(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_STARTGAME: {
        pvpserver.startGame(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_QUITGAME: {
        ret = pvpserver.quitGame(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_STATUS: {
        ret = pvpserver.broadcastStatus(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_GETMATCHER: {
        ret = pvpserver.getMatcher(userId, recvedData);
        break;
      }
      case ServerConst.REQUEST_LEAVELOBBY: {
        pvpserver.leaveLobby(userId, recvedData);
        break;
      }
      default: {
        break;
      }
    }

    if (ret != null) {
      pubJedis = jedisPool.getResource();
      pubJedis.publish(pubchannel.getBytes(), ret);
      jedisPool.returnResource(pubJedis);
    }
  }

  @Override
  public void onSubscribe(byte[] channel, int subscribedChannels) {

  }

  @Override
  public void onUnsubscribe(byte[] channel, int subscribedChannels) {

  }

  @Override
  public void onPSubscribe(byte[] pattern, int subscribedChannels) {
  }

  @Override
  public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
  }

  @Override
  public void onPMessage(byte[] pattern, byte[] channel,
                         byte[] message) {
  }

  public static void run() {
    jedisPoolConfig = new JedisPoolConfig();
    //        config.setMaxActive();
    //        config.setMaxIdle();
    //        config.setMaxWait();

    jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379, 0);

    final PubsubUtil subscriber = new PubsubUtil();
    subJedis = jedisPool.getResource();

    /*
      Key points here are,
      - Subscribe to channels on separate threads, they will be blocked while you are subscribed.
        You can subscribe to multiple channels with a single PubSub instance.
      - Do not forget that Jedis instances are not thread-safe.
        For my example code, it is not that important but you should consider it if you will use Jedis.
      - Use JedisPool if you will create many Jedis instances
          and give them back to the pool when you are done with them.
          By the way, the last arg (0) i passed to JedisPool is for avoiding SocketTimeoutException.
     */
    new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            System.out.println("Subscribing to room:request. This thread will be blocked.");
            subJedis.subscribe(subscriber, subchannel.getBytes());
            System.out.println("Subscription ended.");
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            // jedisPool.returnResource(subJedis);
          }
        }
      }).start();

  }
}

package server.pvp.redisclient;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Created by bjcheny on 6/17/14.
 */
public class LpushBrpopUtil {
  private static JedisPoolConfig jedisPoolConfig;
  private static JedisPool jedisPool;

  public static String lpushList = "room:lpushresponse";
  public static Jedis lpushJedis = null;

  public static String brpopList = "room:brpoprequest";
  public static Jedis brpopJedis = null;

  public static void run() {
    jedisPoolConfig = new JedisPoolConfig();
    jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379, 0);
    if (lpushJedis == null) {
      lpushJedis = jedisPool.getResource();
    }

    final byte[] key = { 1 };
    final byte[] ret = { 8 };
    final long bsize = lpushJedis.lpush(key, ret);

    if (brpopJedis == null) {
      brpopJedis = jedisPool.getResource();
    }

    int timeout = 1;
    List<byte[]> bresult = brpopJedis.brpop(timeout, key);
    System.out.println(bresult.toString());

    jedisPool.returnResource(lpushJedis);
    jedisPool.returnResource(brpopJedis);
  }
}

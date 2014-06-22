package test;

/**
 * Created by bjcheny on 6/12/14.
 */

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;


public class RedisClientUtil {
    protected static HostAndPort hnp = HostAndPortUtil.getRedisServers().get(0);
    protected static Jedis jedis;

    public static Jedis createJedis() {
        jedis = new Jedis(hnp.getHost(), hnp.getPort());
        jedis.connect();
//        jedis.auth("");
        jedis.flushAll();
        return jedis;
    }

    static void publish(final String channel, final String message) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    jedis.publish(channel, message);
                    jedis.disconnect();
                } catch (Exception ex) {
                }
            }
        });
        t.start();
    }

    static void subscribe() throws InterruptedException {
        jedis.subscribe(new JedisPubSub() {
            public void onMessage(String channel, String message) {
                System.out.print(channel + " + " + message);
                unsubscribe();
            }

            public void onSubscribe(String channel, int subscribedChannels) {
                System.out.print(channel + " + " + subscribedChannels);
                // now that I'm subscribed... publish
                publish("foo", "exit");
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
                System.out.print(channel + " + " + subscribedChannels);
            }

            public void onPSubscribe(String pattern, int subscribedChannels) {
            }

            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            public void onPMessage(String pattern, String channel,
                                   String message) {
            }
        }, "foo");
    }
}

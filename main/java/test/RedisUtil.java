package test;


import redis.clients.jedis.Connection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.SafeEncoder;

public class RedisUtil {

  /*
  private void publishOne(final String channel, final String message) {
    Thread t = new Thread(new Runnable() {
        public void run() {
          try {
            Jedis j = createJedis();
            j.publish(channel, message);
            j.disconnect();
          } catch (Exception ex) {
          }
        }
      });
    t.start();
  }


  public void subscribe() throws InterruptedException {
    jedis.subscribe(new JedisPubSub() {
        public void onMessage(String channel, String message) {
          assertEquals("foo", channel);
          assertEquals("exit", message);
          unsubscribe();
        }

        public void onSubscribe(String channel, int subscribedChannels) {
          assertEquals("foo", channel);
          assertEquals(1, subscribedChannels);

          // now that I'm subscribed... publish
          publishOne("foo", "exit");
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
          assertEquals("foo", channel);
          assertEquals(0, subscribedChannels);
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



  public void pubSubChannels(){
    final List<String> expectedActiveChannels = Arrays.asList("testchan1", "testchan2", "testchan3");
    jedis.subscribe(new JedisPubSub() {
        private int count = 0;

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
          count++;
          //All channels are subscribed
          if (count == 3) {
            Jedis otherJedis = createJedis();
            List<String> activeChannels = otherJedis.pubsubChannels("test*");
            assertTrue(expectedActiveChannels.containsAll(activeChannels));
            unsubscribe();
          }
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onMessage(String channel, String message) {
        }
      }, "testchan1", "testchan2", "testchan3");
  }


  public void pubSubNumPat(){
    jedis.psubscribe(new JedisPubSub() {
        private int count=0;
        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
          count++;
          if (count == 3) {
            Jedis otherJedis = createJedis();
            Long numPatterns = otherJedis.pubsubNumPat();
            assertEquals(new Long(2l), numPatterns);
            punsubscribe();
          }
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onMessage(String channel, String message) {
        }
      }, "test*", "test*", "chan*");
  }


  public void pubSubNumSub(){
    final MapInfo<String, String> expectedNumSub = new HashMap<String, String>();
    expectedNumSub.put("testchannel2", "1");
    expectedNumSub.put("testchannel1", "1");
    jedis.subscribe(new JedisPubSub() {
        private int count=0;
        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
          count++;
          if (count == 2) {
            Jedis otherJedis = createJedis();
            MapInfo<String, String> numSub = otherJedis.pubsubNumSub("testchannel1", "testchannel2");
            assertEquals(expectedNumSub, numSub);
            unsubscribe();
          }
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onMessage(String channel, String message) {
        }
      }, "testchannel1", "testchannel2");
  }


  public void subscribeMany() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.subscribe(new JedisPubSub() {
        public void onMessage(String channel, String message) {
          unsubscribe(channel);
        }

        public void onSubscribe(String channel, int subscribedChannels) {
          publishOne(channel, "exit");
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
        }
      }, "foo", "bar");
  }


  public void psubscribe() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.psubscribe(new JedisPubSub() {
        public void onMessage(String channel, String message) {
        }

        public void onSubscribe(String channel, int subscribedChannels) {
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
          assertEquals("foo.*", pattern);
          assertEquals(1, subscribedChannels);
          publishOne("foo.bar", "exit");

        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
          assertEquals("foo.*", pattern);
          assertEquals(0, subscribedChannels);
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
          assertEquals("foo.*", pattern);
          assertEquals("foo.bar", channel);
          assertEquals("exit", message);
          punsubscribe();
        }
      }, "foo.*");
  }

  public void psubscribeMany() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.psubscribe(new JedisPubSub() {
        public void onMessage(String channel, String message) {
        }

        public void onSubscribe(String channel, int subscribedChannels) {
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
          publishOne(pattern.replace("*", "123"), "exit");
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
          punsubscribe(pattern);
        }
      }, "foo.*", "bar.*");
  }

  public void subscribeLazily() throws UnknownHostException, IOException,
      InterruptedException {
    final JedisPubSub pubsub = new JedisPubSub() {
        public void onMessage(String channel, String message) {
          unsubscribe(channel);
        }

        public void onSubscribe(String channel, int subscribedChannels) {
          publishOne(channel, "exit");
          if (!channel.equals("bar")) {
            this.subscribe("bar");
            this.psubscribe("bar.*");
          }
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
          publishOne(pattern.replace("*", "123"), "exit");
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
          punsubscribe(pattern);
        }
      };

    jedis.subscribe(pubsub, "foo");
  }

  public void binarySubscribe() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.subscribe(new BinaryJedisPubSub() {
        public void onMessage(byte[] channel, byte[] message) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo"), channel));
          assertTrue(Arrays.equals(SafeEncoder.encode("exit"), message));
          unsubscribe();
        }

        public void onSubscribe(byte[] channel, int subscribedChannels) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo"), channel));
          assertEquals(1, subscribedChannels);
          publishOne(SafeEncoder.encode(channel), "exit");
        }

        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo"), channel));
          assertEquals(0, subscribedChannels);
        }

        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPMessage(byte[] pattern, byte[] channel,
                               byte[] message) {
        }
      }, SafeEncoder.encode("foo"));
  }

  public void binarySubscribeMany() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.subscribe(new BinaryJedisPubSub() {
        public void onMessage(byte[] channel, byte[] message) {
          unsubscribe(channel);
        }

        public void onSubscribe(byte[] channel, int subscribedChannels) {
          publishOne(SafeEncoder.encode(channel), "exit");
        }

        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPMessage(byte[] pattern, byte[] channel,
                               byte[] message) {
        }
      }, SafeEncoder.encode("foo"), SafeEncoder.encode("bar"));
  }

  public void binaryPsubscribe() throws UnknownHostException, IOException,
      InterruptedException {
    jedis.psubscribe(new BinaryJedisPubSub() {
        public void onMessage(byte[] channel, byte[] message) {
        }

        public void onSubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo.*"), pattern));
          assertEquals(1, subscribedChannels);
          publishOne(SafeEncoder.encode(pattern).replace("*", "bar"),
                     "exit");
        }

        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo.*"), pattern));
          assertEquals(0, subscribedChannels);
        }

        public void onPMessage(byte[] pattern, byte[] channel,
                               byte[] message) {
          assertTrue(Arrays.equals(SafeEncoder.encode("foo.*"), pattern));
          assertTrue(Arrays
                     .equals(SafeEncoder.encode("foo.bar"), channel));
          assertTrue(Arrays.equals(SafeEncoder.encode("exit"), message));
          punsubscribe();
        }
      }, SafeEncoder.encode("foo.*"));
  }

  public void binaryPsubscribeMany() throws UnknownHostException,
      IOException, InterruptedException {
    jedis.psubscribe(new BinaryJedisPubSub() {
        public void onMessage(byte[] channel, byte[] message) {
        }

        public void onSubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
          publishOne(SafeEncoder.encode(pattern).replace("*", "123"),
                     "exit");
        }

        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPMessage(byte[] pattern, byte[] channel,
                               byte[] message) {
          punsubscribe(pattern);
        }
      }, SafeEncoder.encode("foo.*"), SafeEncoder.encode("bar.*"));
  }

  public void binarySubscribeLazily() throws UnknownHostException,
      IOException, InterruptedException {
    final BinaryJedisPubSub pubsub = new BinaryJedisPubSub() {
        public void onMessage(byte[] channel, byte[] message) {
          unsubscribe(channel);
        }

        public void onSubscribe(byte[] channel, int subscribedChannels) {
          publishOne(SafeEncoder.encode(channel), "exit");

          if (!SafeEncoder.encode(channel).equals("bar")) {
            this.subscribe(SafeEncoder.encode("bar"));
            this.psubscribe(SafeEncoder.encode("bar.*"));
          }
        }

        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
          publishOne(SafeEncoder.encode(pattern).replace("*", "123"),
                     "exit");
        }

        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        public void onPMessage(byte[] pattern, byte[] channel,
                               byte[] message) {
          punsubscribe(pattern);
        }
      };

    jedis.subscribe(pubsub, SafeEncoder.encode("foo"));
  }

  public void unsubscribeWhenNotSusbscribed() throws InterruptedException {
    JedisPubSub pubsub = new JedisPubSub() {
        public void onMessage(String channel, String message) {
        }

        public void onPMessage(String pattern, String channel,
                               String message) {
        }

        public void onSubscribe(String channel, int subscribedChannels) {
        }

        public void onUnsubscribe(String channel, int subscribedChannels) {
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
        }
      };
    pubsub.unsubscribe();
  }

  public void handleClientOutputBufferLimitForSubscribeTooSlow()
      throws InterruptedException {
    final Jedis j = createJedis();
    final AtomicBoolean exit = new AtomicBoolean(false);

    final Thread t = new Thread(new Runnable() {
        public void run() {
          try {

            // we already set jedis1 config to
            // client-output-buffer-limit pubsub 256k 128k 5
            // it means if subscriber delayed to receive over 256k or
            // 128k continuously 5 sec,
            // redis disconnects subscriber

            // we publish over 100M data for making situation for exceed
            // client-output-buffer-limit
            String veryLargeString = makeLargeString(10485760);

            // 10M * 10 = 100M
            for (int i = 0; i < 10 && !exit.get(); i++) {
              j.publish("foo", veryLargeString);
            }

            j.disconnect();
          } catch (Exception ex) {
          }
        }
      });
    t.start();
    try {
      jedis.subscribe(new JedisPubSub() {
          public void onMessage(String channel, String message) {
            try {
              // wait 0.5 secs to slow down subscribe and
              // client-output-buffer exceed
              // System.out.println("channel - " + channel +
              // " / message - " + message);
              Thread.sleep(100);
            } catch (Exception e) {
              try {
                t.join();
              } catch (InterruptedException e1) {
              }

              fail(e.getMessage());
            }
          }

          public void onSubscribe(String channel, int subscribedChannels) {
          }

          public void onUnsubscribe(String channel, int subscribedChannels) {
          }

          public void onPSubscribe(String pattern, int subscribedChannels) {
          }

          public void onPUnsubscribe(String pattern,
                                     int subscribedChannels) {
          }

          public void onPMessage(String pattern, String channel,
                                 String message) {
          }
        }, "foo");
    } finally {
      // exit the publisher thread. if exception is thrown, thread might
      // still keep publishing things.
      exit.set(true);
      if (t.isAlive()) {
        t.join();
      }
    }
  }

  private String makeLargeString(int size) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < size; i++)
      sb.append((char) ('a' + i % 26));

    return sb.toString();
  }
  */
}

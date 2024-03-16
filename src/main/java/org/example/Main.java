package org.example;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class Main {

    public static void main(String[] args) {
        try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.set("users:300:email", "wogns8030@naver.com");
                jedis.set("users:300:name", "JaeHoon Yun");
                jedis.set("users:300:age", "29");

                var userEmail = jedis.get("users:300:email");
                System.out.println(userEmail);

                List<String> userInfo = jedis.mget("users:300:email", "users:300:name",
                    "users:300:age");
                userInfo.forEach(System.out::println);

                long counter = jedis.incr("counter");
                System.out.println(counter);

                counter = jedis.incrBy("counter", 10L);
                System.out.println(counter);

                long decr = jedis.decr("counter");
                System.out.println(decr);

                decr = jedis.decrBy("counter", 20);
                System.out.println(decr);

                Pipeline pipeline = jedis.pipelined();
                pipeline.set("user:400:email","wogns8030@naver.com");
                pipeline.set("user:400:name","JaeHoonYun");
                pipeline.set("user:400:age","29");
                List<Object> objects = pipeline.syncAndReturnAll();
                objects.forEach(i -> System.out.println(i.toString()));
            }
        }
    }

}
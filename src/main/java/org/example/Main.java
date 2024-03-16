package org.example;

import java.util.List;
import java.util.Set;
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

                //list
                //1. stack
                jedis.rpush("stack1","aaaa");
                jedis.rpush("stack1","bbbb");
                jedis.rpush("stack1","cccc");
                List<String> stack1 = jedis.lrange("stack1", 0, -1);
                stack1.forEach(System.out::println);
                System.out.println(jedis.rpop("stack1"));
                System.out.println(jedis.rpop("stack1"));
                System.out.println(jedis.rpop("stack1"));

                //2. queue
                jedis.rpush("queue1","zzzz");
                jedis.rpush("queue1","zzzz2");
                jedis.rpush("queue1","zzzz3");
                List<String> queue1 = jedis.lrange("queue1", 0, -1);
                queue1.forEach(System.out::println);
                System.out.println(jedis.lpop("queue1"));
                System.out.println(jedis.lpop("queue1"));
                System.out.println(jedis.lpop("queue1"));

                //3. block brpop, blpop
                List<String> blpop = jedis.blpop(3, "queue:blocking"); //queue:blocking key 3초 대기
                if(blpop != null){
                    blpop.forEach(System.out::println);
                }

                //Set(unique) 중복값 허용 X 정렬 X
                jedis.sadd("users:500:follow","100","200","300");
                jedis.sadd("users:100:follow","100","200","300");
                jedis.srem("users:500:follow","100"); //remove

                System.out.println(jedis.scard("users:500:follow"));

                Set<String> sinter = jedis.sinter("users:500:follow", "users:100:follow"); //500,100 공통 값
                sinter.forEach(System.out::println);

                Set<String> smembers = jedis.smembers("users:500:follow");
                smembers.forEach(System.out::println);

                System.out.println(jedis.sismember("users:500:follow", "200")); //return type boolean
                System.out.println(jedis.sismember("users:500:follow", "100")); //remove -> return result false


            }
        }
    }

}
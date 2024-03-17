package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.Tuple;

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
                pipeline.set("user:400:email", "wogns8030@naver.com");
                pipeline.set("user:400:name", "JaeHoonYun");
                pipeline.set("user:400:age", "29");
                List<Object> objects = pipeline.syncAndReturnAll();
                objects.forEach(i -> System.out.println(i.toString()));

                //list Test
                //1. stack
                jedis.rpush("stack1", "aaaa");
                jedis.rpush("stack1", "bbbb");
                jedis.rpush("stack1", "cccc");
                List<String> stack1 = jedis.lrange("stack1", 0, -1);
                stack1.forEach(System.out::println);
                System.out.println(jedis.rpop("stack1"));
                System.out.println(jedis.rpop("stack1"));
                System.out.println(jedis.rpop("stack1"));

                //2. queue
                jedis.rpush("queue1", "zzzz");
                jedis.rpush("queue1", "zzzz2");
                jedis.rpush("queue1", "zzzz3");
                List<String> queue1 = jedis.lrange("queue1", 0, -1);
                queue1.forEach(System.out::println);
                System.out.println(jedis.lpop("queue1"));
                System.out.println(jedis.lpop("queue1"));
                System.out.println(jedis.lpop("queue1"));

                //3. block brpop, blpop
                List<String> blpop = jedis.blpop(3, "queue:blocking"); //queue:blocking key 3초 대기
                if (blpop != null) {
                    blpop.forEach(System.out::println);
                }

                //Set(unique) 중복값 허용 X 정렬 X Test
                jedis.sadd("users:500:follow", "100", "200", "300");
                jedis.sadd("users:100:follow", "100", "200", "300");
                jedis.srem("users:500:follow", "100"); //remove

                System.out.println(jedis.scard("users:500:follow"));

                Set<String> sinter = jedis.sinter("users:500:follow",
                    "users:100:follow"); //500,100 공통
                sinter.forEach(System.out::println);

                Set<String> smembers = jedis.smembers("users:500:follow");
                smembers.forEach(System.out::println);

                System.out.println(
                    jedis.sismember("users:500:follow", "200")); //return type boolean
                System.out.println(
                    jedis.sismember("users:500:follow", "100")); //remove -> return result false

                //HASH Test
                jedis.hset("users:2:info", "name", "jh"); //name
                var info = new HashMap<String, String>();
                info.put("email", "wogns8030@naver.com");
                info.put("phone", "010-xxxx-xxxx");
                jedis.hset("users:2:info", info); //email, phone
                System.out.println(
                    "jedis.hgetAll(\"users:2:info\") = " + jedis.hgetAll("users:2:info"));

                //DELETE
                jedis.hdel("users:2:info", "name"); //name 삭제
                System.out.println(
                    "jedis.hgetAll(\"users:2:info\") = " + jedis.hgetAll("users:2:info"));

                //HGet
                System.out.println( //email get
                    "jedis.hget(\"users:2:info\",\"email\") = " + jedis.hget("users:2:info",
                        "email"));

                //hincr
                jedis.hincrBy("users:2:info", "visits", 1); //visits 1 증가
                System.out.println(
                    "jedis.hgetAll(\"users:2:info\") = " + jedis.hgetAll("users:2:info"));

                //Sorted Sets Test
                jedis.zadd("game1:scores", 100, "users1");
                jedis.zadd("game1:scores", 200, "users2");
                jedis.zadd("game1:scores", 300, "users3");
                jedis.zadd("game1:scores", 400, "users4");
                System.out.println(
                    "jedis.zrange(\"game1:scores\",0,5) = " + jedis.zrange("game1:scores", 0, 3));
                List<String> game1 = jedis.zrange("game1:scores", 0, Long.MAX_VALUE);
                game1.forEach(System.out::println); //forEach 사용
                System.out.println(
                    "jedis.zcard(\"game1:scores\") = " + jedis.zcard("game1:scores"));
                jedis.zrem("game1:scores", "users4"); //users4 remove
                System.out.println(
                    "jedis.zrange(\"game1:scores\",0,5) = " + jedis.zrange("game1:scores", 0, 3));
                System.out.println(
                    "jedis.zcard(\"game1:scores\") = " + jedis.zcard("game1:scores"));

                var scores = new HashMap<String, Double>();
                scores.put("user1", 200.0);
                scores.put("user2", 100.0);
                scores.put("user3", 300.0);
                scores.put("user4", 400.0);
                jedis.zadd("game2:scores", scores);

                List<String> game2 = jedis.zrange("game2:scores", 0, Long.MAX_VALUE);
                game2.forEach(System.out::println); // Sorted Sets -> 오름차순

                List<Tuple> tuples = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
                tuples.forEach(i -> System.out.println("%s, %f".formatted(i.getElement(), i.getScore())));
                System.out.println("-------------------------------");
                jedis.zincrby("game2:scores",100.0,"user4"); //user4 100 increment
                List<Tuple> tuples1 = jedis.zrangeWithScores("game2:scores", 0, Long.MAX_VALUE);
                tuples1.forEach(i -> System.out.println("%s, %f".formatted(i.getElement(), i.getScore())));
                System.out.println("-------------------------------");

                //Geospatial Test 위치(좌표)정보 저장
                jedis.geoadd("stores:geo",127.02985530619755,37.49911212874,"awesomePlace1");
                jedis.geoadd("stores:geo",127.0333352287619,37.491921163986234,"awesomePlace2");
                Double geodist = jedis.geodist("stores:geo", "awesomePlace1", "awesomePlace2");
                System.out.println(geodist);

                //500M 반경 내 위치 정보 반환
                List<GeoRadiusResponse> geosearch = jedis.geosearch("stores:geo",
                    new GeoSearchParam()
                        .fromLonLat(new GeoCoordinate(127.033, 37.495))
                        .byRadius(500, GeoUnit.M)
                        .withCoord());
                geosearch.forEach(i -> System.out.println("%s %f %f".formatted(i.getMemberByString(), i.getCoordinate().getLongitude(),i.getCoordinate().getLatitude())));

                //삭제
                jedis.unlink("stores:geo");
            }
        }
    }
}
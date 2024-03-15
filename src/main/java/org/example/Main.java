package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Main {
    public static void main(String[] args) {

        try (var jedisPool = new JedisPool("127.0.0.1",6379)){
            try(Jedis jedis = jedisPool.getResource()){
                jedis.set("users:300:email","wogns8030@naver.com");
            }
        }
    }

}
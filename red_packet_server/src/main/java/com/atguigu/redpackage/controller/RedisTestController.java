package com.atguigu.redpackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author lfy
 * @Description
 * @create 2023-07-09 16:26
 */
@RestController
public class RedisTestController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/redis/set")
    public String redisTest(){
        String string = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("haha-lfy",string);
        return  "刚才给redis中保存了："+string;
    }

    @GetMapping("/redis/get")
    public String redisTestGet(){
        String s = redisTemplate.opsForValue().get("haha-lfy");
        return  "redis查到："+s;
    }
}

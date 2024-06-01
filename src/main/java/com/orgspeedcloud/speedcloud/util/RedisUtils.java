package com.orgspeedcloud.speedcloud.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 提供基础redis命令
 *
 * @author Chen
 */
@Component
public class RedisUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object update(String key, Object value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    public boolean delete(String key) {
        Boolean delete = redisTemplate.delete(key);
        return delete != null && delete;
    }

    public void expire(String key, long tempTime, TimeUnit timeUnit) {
        redisTemplate.expire(key, tempTime, timeUnit);
    }

    public void setnx(String key, Object value, long tempTime) {
        set(key, value);
        expire(key, tempTime, TimeUnit.SECONDS);
    }

    public void hset(String hashKey,String key,String value){
        redisTemplate.opsForHash().put(hashKey,key,value);
    }
    public String hget(String hashKey,String key){
        return (String)redisTemplate.opsForHash().get(hashKey, key);
    }
    public Boolean exists(String key){
        Set<String> keys = redisTemplate.keys(key);
        return keys != null && keys.size() > 0;
    }
    public void increment(String key){
        redisTemplate.opsForValue().increment(key);
    }
}

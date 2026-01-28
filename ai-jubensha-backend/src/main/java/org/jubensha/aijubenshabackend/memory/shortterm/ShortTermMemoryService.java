package org.jubensha.aijubenshabackend.memory.shortterm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ShortTermMemoryService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long DEFAULT_TTL = 24L;
    private static final TimeUnit TTL_UNIT = TimeUnit.HOURS;
    
    @Autowired
    public ShortTermMemoryService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 存储短期记忆
     */
    public void storeMemory(Long gameId, String key, Object value) {
        String redisKey = getRedisKey(gameId, key);
        redisTemplate.opsForValue().set(redisKey, value, DEFAULT_TTL, TTL_UNIT);
    }
    
    /**
     * 存储短期记忆，指定过期时间
     */
    public void storeMemory(Long gameId, String key, Object value, long ttl, TimeUnit unit) {
        String redisKey = getRedisKey(gameId, key);
        redisTemplate.opsForValue().set(redisKey, value, ttl, unit);
    }
    
    /**
     * 获取短期记忆
     */
    public Object getMemory(Long gameId, String key) {
        String redisKey = getRedisKey(gameId, key);
        return redisTemplate.opsForValue().get(redisKey);
    }
    
    /**
     * 删除短期记忆
     */
    public void deleteMemory(Long gameId, String key) {
        String redisKey = getRedisKey(gameId, key);
        redisTemplate.delete(redisKey);
    }
    
    /**
     * 向列表添加元素
     */
    public void addToList(Long gameId, String listKey, Object value) {
        String redisKey = getRedisKey(gameId, listKey);
        redisTemplate.opsForList().rightPush(redisKey, value);
        redisTemplate.expire(redisKey, DEFAULT_TTL, TTL_UNIT);
    }
    
    /**
     * 获取列表元素
     */
    public List<Object> getList(Long gameId, String listKey, long start, long end) {
        String redisKey = getRedisKey(gameId, listKey);
        return redisTemplate.opsForList().range(redisKey, start, end);
    }
    
    /**
     * 清除游戏的所有短期记忆
     */
    public void clearGameMemory(Long gameId) {
        String pattern = "game:" + gameId + ":*";
        redisTemplate.keys(pattern).forEach(redisTemplate::delete);
    }
    
    /**
     * 生成Redis键
     */
    private String getRedisKey(Long gameId, String key) {
        return "game:" + gameId + ":" + key;
    }
}
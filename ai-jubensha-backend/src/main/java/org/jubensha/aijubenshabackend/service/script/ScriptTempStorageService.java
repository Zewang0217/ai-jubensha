package org.jubensha.aijubenshabackend.service.script;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 剧本临时存储服务
 * 用于存储生成中的剧本内容
 */
@Service
public class ScriptTempStorageService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration EXPIRATION_TIME = Duration.ofHours(2); // 2小时过期

    public ScriptTempStorageService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存储临时剧本内容
     * @param scriptId 剧本ID
     * @param content 剧本内容
     */
    public void storeTempScript(Long scriptId, String content) {
        String key = getTempScriptKey(scriptId);
        redisTemplate.opsForValue().set(key, content, EXPIRATION_TIME);
    }

    /**
     * 获取临时剧本内容
     * @param scriptId 剧本ID
     * @return 剧本内容
     */
    public String getTempScript(Long scriptId) {
        String key = getTempScriptKey(scriptId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除临时剧本内容
     * @param scriptId 剧本ID
     */
    public void deleteTempScript(Long scriptId) {
        String key = getTempScriptKey(scriptId);
        redisTemplate.delete(key);
    }

    /**
     * 检查临时剧本是否存在
     * @param scriptId 剧本ID
     * @return 是否存在
     */
    public boolean existsTempScript(Long scriptId) {
        String key = getTempScriptKey(scriptId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 生成临时剧本的Redis键
     * @param scriptId 剧本ID
     * @return Redis键
     */
    private String getTempScriptKey(Long scriptId) {
        return "temp:script:" + scriptId;
    }
}

package org.jubensha.aijubenshabackend.core.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JsonValidationUtilTest {

    @Test
    void testGenerateWithRetry_Success() throws Exception {
        // 测试正常的JSON生成和验证
        String json = JsonValidationUtil.generateWithRetry(() -> "{\"name\": \"test\", \"value\": 123}", 3);
        assertNotNull(json);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("value"));
        log.info("测试成功: 正常JSON生成和验证通过");
    }

    @Test
    void testGenerateWithRetry_InvalidJson_RetrySuccess() throws Exception {
        // 测试JSON格式错误的重试场景
        int[] attempts = {0};
        String json = JsonValidationUtil.generateWithRetry(() -> {
            attempts[0]++;
            if (attempts[0] == 1) {
                // 第一次返回无效的JSON
                return "{\"name\": \"test\", \"value\": 123"; // 缺少结束的}
            } else {
                // 第二次返回有效的JSON
                return "{\"name\": \"test\", \"value\": 123}";
            }
        }, 3);
        assertNotNull(json);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("value"));
        log.info("测试成功: JSON格式错误重试成功");
    }

    @Test
    void testGenerateWithRetry_MaxRetriesExceeded() {
        // 测试达到最大重试次数的场景
        int[] attempts = {0};
        Exception exception = assertThrows(Exception.class, () -> {
            JsonValidationUtil.generateWithRetry(() -> {
                attempts[0]++;
                // 每次都返回无效的JSON
                return "{\"name\": \"test\", \"value\": 123"; // 缺少结束的}
            }, 3);
        });
        assertNotNull(exception);
        log.info("测试成功: 达到最大重试次数时正确抛出异常");
    }


}

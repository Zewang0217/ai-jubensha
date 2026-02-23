package org.jubensha.aijubenshabackend.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class JsonValidationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 验证JSON格式
     * @param json 要验证的JSON字符串
     * @return 是否为有效的JSON格式
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            log.debug("JSON格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 带重试的JSON生成与验证
     * @param supplier 生成JSON的供应商函数
     * @param maxRetries 最大重试次数
     * @return 验证通过的JSON字符串
     * @throws Exception 如果达到最大重试次数仍失败
     */
    public static String generateWithRetry(Supplier<String> supplier, int maxRetries) throws Exception {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            attempts++;
            try {
                log.info("尝试生成JSON，第{}次", attempts);
                String json = supplier.get();
                
                // 预处理JSON
                json = preprocessJson(json);
                
                // 验证JSON格式
                if (isValidJson(json)) {
                    log.info("JSON生成成功，验证通过");
                    return json;
                } else {
                    throw new IllegalArgumentException("生成的JSON格式无效");
                }
            } catch (Exception e) {
                lastException = e;
                String errorMessage = e.getMessage();
                
                // 检查是否是护轨拦截错误
                if (isGuardrailError(errorMessage)) {
                    String guardrailReason = extractGuardrailReason(errorMessage);
                    log.warn("第{}次尝试失败: 输入护轨拦截 - 原因: {}", attempts, guardrailReason);
                    log.debug("完整的护轨拦截错误信息: {}", errorMessage);
                } else {
                    log.warn("第{}次尝试失败: {}", attempts, errorMessage);
                }
                
                // 如果不是最后一次尝试，等待一段时间后重试
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000); // 1秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        log.error("达到最大重试次数({})，JSON生成失败", maxRetries);
        throw lastException != null ? lastException : new IllegalStateException("JSON生成失败，未知错误");
    }

    /**
     * 检查是否是输入护轨拦截错误
     * @param errorMessage 错误信息
     * @return 是否是护轨拦截错误
     */
    private static boolean isGuardrailError(String errorMessage) {
        return errorMessage != null && (
            errorMessage.contains("The guardrail") && 
            errorMessage.contains("PromptSafetyInputGuardrail failed with this message")
        );
    }

    /**
     * 从错误信息中提取护轨拦截的具体原因
     * @param errorMessage 错误信息
     * @return 护轨拦截的具体原因
     */
    private static String extractGuardrailReason(String errorMessage) {
        if (errorMessage != null && errorMessage.contains("failed with this message: ")) {
            int startIndex = errorMessage.indexOf("failed with this message: ") + "failed with this message: ".length();
            return errorMessage.substring(startIndex).trim();
        }
        return errorMessage;
    }

    /**
     * 使用默认重试次数的JSON生成与验证
     * @param supplier 生成JSON的供应商函数
     * @return 验证通过的JSON字符串
     * @throws Exception 如果达到最大重试次数仍失败
     */
    public static String generateWithRetry(Supplier<String> supplier) throws Exception {
        return generateWithRetry(supplier, DEFAULT_MAX_RETRIES);
    }

    /**
     * 预处理JSON，移除代码块标记并修复不完整的JSON
     * @param json 要预处理的JSON字符串
     * @return 预处理后的JSON字符串
     */
    private static String preprocessJson(String json) {
        if (json == null || json.isEmpty()) {
            log.warn("输入JSON为空");
            return "{}";
        }
        
        // 移除开头的代码块标记
        if (json.startsWith("```json")) {
            json = json.substring(7);
            log.debug("移除了开头的JSON代码块标记");
        } else if (json.startsWith("```")) {
            json = json.substring(3);
            log.debug("移除了开头的代码块标记");
        }
        
        // 移除结尾的代码块标记
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
            log.debug("移除了结尾的代码块标记");
        }
        
        // 去除首尾空白
        json = json.trim();
        log.debug("去除首尾空白后的JSON长度: {}", json.length());
        
        // 移除开头的前言文字，只保留JSON内容
        int jsonStartIndex = json.indexOf('{');
        if (jsonStartIndex != -1) {
            json = json.substring(jsonStartIndex);
            log.debug("移除了开头的前言文字，JSON长度: {}", json.length());
        }
        
        return json;
    }
}

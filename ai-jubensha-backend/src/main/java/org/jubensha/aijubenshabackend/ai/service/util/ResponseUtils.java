package org.jubensha.aijubenshabackend.ai.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于处理模型返回的响应数据
 *
 * @author luobo
 * @date 2026/2/9
 */
public class ResponseUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 从 AI 输出的混杂字符串中提取 JSON 对象
     * 支持以下格式：
     * 1. ```json ... ``` 代码块
     * 2. ``` ... ``` 普通代码块
     * 3. 裸 JSON（无代码块包裹）
     * 超级健壮!
     *
     * @param aiOutput AI 输出的原始字符串
     * @return 解析后的 JsonNode 对象
     * @throws IllegalArgumentException 当无法找到有效 JSON 时
     */
    public static JsonNode extractJson(String aiOutput) {
        if (aiOutput == null || aiOutput.trim().isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }

        // 策略1: 尝试提取 ```json 或 ``` 代码块
        String jsonContent = extractFromCodeBlock(aiOutput);

        // 策略2: 如果没有代码块，尝试直接解析整个字符串
        if (jsonContent == null) {
            jsonContent = aiOutput.trim();
        }

        // 清理可能的残留标记
        jsonContent = cleanJsonString(jsonContent);

        try {
            return mapper.readTree(jsonContent);
        } catch (Exception e) {
            // 策略3: 如果失败，尝试从文本中模糊匹配 JSON 对象/数组
            String fuzzyJson = fuzzyExtractJson(aiOutput);
            if (fuzzyJson != null) {
                try {
                    return mapper.readTree(fuzzyJson);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "找到疑似 JSON 但解析失败: " + ex.getMessage() + "\n内容: " + fuzzyJson.substring(0, Math.min(200, fuzzyJson.length()))
                    );
                }
            }
            throw new IllegalArgumentException("无法解析 JSON: " + e.getMessage());
        }
    }

    /**
     * 从 markdown 代码块中提取内容
     */
    private static String extractFromCodeBlock(String text) {
        // 匹配 ```json ... ``` 或 ``` ... ```，支持可选的 json 标记
        Pattern pattern = Pattern.compile(
                "```(?:json)?\\s*([\\s\\S]*?)```",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);

        // 返回第一个匹配的代码块内容
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 清理 JSON 字符串中的常见污染
     */
    private static String cleanJsonString(String json) {
        // 移除开头的 "json" 标记（如果代码块提取没处理干净）
        json = json.replaceFirst("^(?i)json\\s*", "");

        // 移除 BOM 标记
        json = json.replace("\uFEFF", "");

        // 移除零宽字符
        json = json.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");

        return json.trim();
    }

    /**
     * 模糊匹配：在文本中寻找最像 JSON 的大括号/中括号包裹内容
     */
    private static String fuzzyExtractJson(String text) {
        // 寻找最外层的大括号或中括号包裹内容
        Pattern pattern = Pattern.compile(
                "(\\{[\\s\\S]*\\}|\\[[\\s\\S]*\\])"
        );
        Matcher matcher = pattern.matcher(text);

        String bestMatch = null;
        int maxDepth = 0;

        while (matcher.find()) {
            String candidate = matcher.group(1);
            // 简单验证：检查大括号/中括号是否平衡
            int depth = calculateNestingDepth(candidate);
            if (depth > maxDepth) {
                maxDepth = depth;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * 计算 JSON 嵌套深度作为可信度指标
     */
    private static int calculateNestingDepth(String str) {
        int depth = 0, maxDepth = 0;
        boolean inString = false;
        boolean escape = false;

        for (char c : str.toCharArray()) {
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"' && !escape) {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{' || c == '[') {
                    depth++;
                    maxDepth = Math.max(maxDepth, depth);
                } else if (c == '}' || c == ']') {
                    depth--;
                }
            }
        }
        return maxDepth;
    }
}

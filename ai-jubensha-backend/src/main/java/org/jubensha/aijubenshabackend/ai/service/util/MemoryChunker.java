package org.jubensha.aijubenshabackend.ai.service.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆分块工具类，用于处理长文本的智能分块
 * <p>
 * 主要用于处理剧本杀游戏中的长文本记忆，如线索、时间线等
 * 确保长文本能被完整索引，避免因Embedding模型token限制导致的内容截断
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-03-02
 * @since 2026
 */
@Slf4j
@Component
public class MemoryChunker {

    @Autowired
    private TokenUtils tokenUtils;

    // 分块大小配置
    private static final int CLUE_CHUNK_SIZE = 400;         // 线索分块大小
    private static final int TIMELINE_CHUNK_SIZE = 300;     // 时间线分块大小
    private static final int SAFE_TOKEN_THRESHOLD = 500;    // 安全token阈值

    /**
     * 记忆类型枚举
     */
    public enum MemoryType {
        CLUE,        // 线索记忆
        TIMELINE,    // 时间线记忆
        CONVERSATION // 对话记忆
    }

    /**
     * 智能分块记忆内容
     * 根据记忆类型选择合适的分块策略
     *
     * @param content    原始内容
     * @param memoryType 记忆类型
     * @return 分块后的内容列表
     */
    public List<String> chunkMemory(String content, MemoryType memoryType) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        // 检查是否需要分块
        if (!needsChunking(content, memoryType)) {
            // 不需要分块，直接返回原始内容
            List<String> result = new ArrayList<>();
            result.add(content);
            return result;
        }

        // 根据记忆类型选择分块策略
        int chunkSize = getChunkSizeForMemoryType(memoryType);
        String[] chunks = tokenUtils.chunkText(content, chunkSize);

        // 转换为列表
        List<String> result = new ArrayList<>();
        for (String chunk : chunks) {
            if (!chunk.trim().isEmpty()) {
                result.add(chunk);
            }
        }

        log.debug("记忆分块完成，原始内容长度: {}, 分块数量: {}, 记忆类型: {}",
                content.length(), result.size(), memoryType);

        return result;
    }

    /**
     * 检查内容是否需要分块
     *
     * @param content    原始内容
     * @param memoryType 记忆类型
     * @return 是否需要分块
     */
    public boolean needsChunking(String content, MemoryType memoryType) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        // 估算token数量
        int tokenCount = tokenUtils.estimateTokens(content);
        int safeThreshold = getSafeThresholdForMemoryType(memoryType);

        return tokenCount > safeThreshold;
    }

    /**
     * 根据记忆类型获取分块大小
     *
     * @param memoryType 记忆类型
     * @return 分块大小（token数）
     */
    private int getChunkSizeForMemoryType(MemoryType memoryType) {
        switch (memoryType) {
            case CLUE:
                return CLUE_CHUNK_SIZE;
            case TIMELINE:
                return TIMELINE_CHUNK_SIZE;
            case CONVERSATION:
                // 对话记忆使用短块策略
                return 50;
            default:
                return CLUE_CHUNK_SIZE;
        }
    }

    /**
     * 根据记忆类型获取安全阈值
     *
     * @param memoryType 记忆类型
     * @return 安全阈值（token数）
     */
    private int getSafeThresholdForMemoryType(MemoryType memoryType) {
        switch (memoryType) {
            case CLUE:
            case TIMELINE:
                return SAFE_TOKEN_THRESHOLD;
            case CONVERSATION:
                // 对话记忆使用更严格的阈值
                return 100;
            default:
                return SAFE_TOKEN_THRESHOLD;
        }
    }

    /**
     * 批量分块记忆内容
     *
     * @param contents   内容列表
     * @param memoryType 记忆类型
     * @return 分块后的内容列表
     */
    public List<String> chunkMemories(List<String> contents, MemoryType memoryType) {
        List<String> result = new ArrayList<>();

        for (String content : contents) {
            List<String> chunks = chunkMemory(content, memoryType);
            result.addAll(chunks);
        }

        return result;
    }

    /**
     * 获取分块大小配置
     *
     * @param memoryType 记忆类型
     * @return 分块大小配置 [chunkSize, safeThreshold]
     */
    public int[] getChunkConfig(MemoryType memoryType) {
        return new int[]{getChunkSizeForMemoryType(memoryType), getSafeThresholdForMemoryType(memoryType)};
    }
}

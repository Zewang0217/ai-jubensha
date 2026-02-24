package org.jubensha.aijubenshabackend.ai.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息分块工具类，用于处理不同类型消息的智能分块
 * <p>
 * 主要用于处理剧本杀游戏中的各种消息，特别是超长消息的分块存储
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-09
 * @since 2026
 */
@Slf4j
@Component
public class MessageChunker {

    @Autowired
    private TokenUtils tokenUtils;

    // 消息类型枚举
    public enum MessageType {
        DM_MESSAGE,        // DM消息
        PLAYER_MESSAGE,    // 玩家消息
        AI_PLAYER_MESSAGE, // AI玩家消息
        SYSTEM_MESSAGE     // 系统消息
    }

    // 不同类型消息的处理策略
    private static final int DM_MESSAGE_CHUNK_SIZE = 400;         // DM消息分块大小
    private static final int PLAYER_MESSAGE_CHUNK_SIZE = 450;     // 玩家消息分块大小
    private static final int AI_PLAYER_MESSAGE_CHUNK_SIZE = 450;  // AI玩家消息分块大小
    private static final int SYSTEM_MESSAGE_CHUNK_SIZE = 300;     // 系统消息分块大小
    
    // 短块切分策略（用于父子文档检索）
    private static final int SHORT_CHUNK_MIN_SIZE = 30;           // 短块最小大小
    private static final int SHORT_CHUNK_MAX_SIZE = 50;           // 短块最大大小

    /**
     * 智能分块消息
     * 根据消息类型选择合适的分块策略
     *
     * @param message 原始消息
     * @param messageType 消息类型
     * @return 分块后的消息列表
     */
    public List<String> chunkMessage(String message, MessageType messageType) {
        if (message == null || message.isEmpty()) {
            return new ArrayList<>();
        }

        int chunkSize = getChunkSizeForMessageType(messageType);
        String[] chunks = tokenUtils.chunkText(message, chunkSize);
        
        // 添加分块元数据
        String[] chunksWithMetadata = tokenUtils.addChunkMetadata(chunks, message);
        
        // 转换为列表
        List<String> result = new ArrayList<>();
        for (String chunk : chunksWithMetadata) {
            result.add(chunk);
        }

        log.debug("消息分块完成，原始消息长度: {}, 分块数量: {}, 消息类型: {}", 
                message.length(), result.size(), messageType);

        return result;
    }

    /**
     * 根据消息类型获取分块大小
     *
     * @param messageType 消息类型
     * @return 分块大小（token数）
     */
    private int getChunkSizeForMessageType(MessageType messageType) {
        switch (messageType) {
            case DM_MESSAGE:
                return DM_MESSAGE_CHUNK_SIZE;
            case PLAYER_MESSAGE:
                return PLAYER_MESSAGE_CHUNK_SIZE;
            case AI_PLAYER_MESSAGE:
                return AI_PLAYER_MESSAGE_CHUNK_SIZE;
            case SYSTEM_MESSAGE:
                return SYSTEM_MESSAGE_CHUNK_SIZE;
            default:
                return PLAYER_MESSAGE_CHUNK_SIZE;
        }
    }

    /**
     * 检查消息是否需要分块
     *
     * @param message 原始消息
     * @param messageType 消息类型
     * @return 是否需要分块
     */
    public boolean needsChunking(String message, MessageType messageType) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        int chunkSize = getChunkSizeForMessageType(messageType);
        return tokenUtils.estimateTokens(message) > chunkSize;
    }

    /**
     * 智能处理消息
     * 根据消息类型和长度选择合适的处理策略
     *
     * @param message 原始消息
     * @param messageType 消息类型
     * @return 处理后的消息列表
     */
    public List<String> processMessage(String message, MessageType messageType) {
        List<String> result = new ArrayList<>();

        // 检查是否需要分块
        if (needsChunking(message, messageType)) {
            // 需要分块，进行智能分块
            result = chunkMessage(message, messageType);
        } else {
            // 不需要分块，直接返回原始消息
            result.add(message);
        }

        return result;
    }

    /**
     * 批量处理消息
     *
     * @param messages 消息列表
     * @param messageType 消息类型
     * @return 处理后的消息块列表
     */
    public List<String> processMessages(List<String> messages, MessageType messageType) {
        List<String> result = new ArrayList<>();

        for (String message : messages) {
            List<String> chunks = processMessage(message, messageType);
            result.addAll(chunks);
        }

        return result;
    }

    /**
     * 识别消息类型
     * 根据消息内容和发送者信息智能识别消息类型
     *
     * @param message 消息内容
     * @param senderName 发送者名称
     * @return 消息类型
     */
    public MessageType identifyMessageType(String message, String senderName) {
        if (senderName == null) {
            return MessageType.SYSTEM_MESSAGE;
        }

        // 识别DM消息
        if (senderName.equals("DM") || senderName.equals("主持人")) {
            return MessageType.DM_MESSAGE;
        }

        return MessageType.PLAYER_MESSAGE;
    }

    /**
     * 获取消息存储策略
     * 根据消息类型决定是否存储到向量数据库
     *
     * @param messageType 消息类型
     * @return 是否需要存储到向量数据库
     */
    public boolean shouldStoreToVectorDB(MessageType messageType) {
        switch (messageType) {
            case AI_PLAYER_MESSAGE:
                return true;  // AI玩家消息必须存储
            case PLAYER_MESSAGE:
                return true;  // 玩家消息必须存储
            case DM_MESSAGE:
                return false;  // DM消息不存储，主要是规则说明和流程引导
            case SYSTEM_MESSAGE:
                // 系统消息选择性存储，只存储包含游戏状态更新的消息
                return false;
            default:
                return false;
        }
    }

    /**
     * 优化消息存储
     * 为存储到向量数据库的消息进行优化处理
     *
     * @param message 原始消息
     * @param messageType 消息类型
     * @return 优化后的消息（可能为空，表示不需要存储）
     */
    public List<String> optimizeMessageForStorage(String message, MessageType messageType) {
        // 检查是否需要存储
        if (!shouldStoreToVectorDB(messageType)) {
            return new ArrayList<>();
        }

        // 需要存储，进行处理
        return processMessage(message, messageType);
    }

    /**
     * 批量优化消息存储
     *
     * @param messages 消息列表
     * @param messageType 消息类型
     * @return 优化后的消息块列表
     */
    public List<String> optimizeMessagesForStorage(List<String> messages, MessageType messageType) {
        List<String> result = new ArrayList<>();

        for (String message : messages) {
            List<String> optimizedChunks = optimizeMessageForStorage(message, messageType);
            result.addAll(optimizedChunks);
        }

        return result;
    }
    
    /**
     * 短块切分（用于父子文档检索）
     * 将消息切分为30-50 tokens的短块，适合向量相似度搜索
     *
     * @param message 原始消息
     * @return 短块列表
     */
    public List<String> chunkForParentChildRetrieval(String message) {
        if (message == null || message.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> shortChunks = new ArrayList<>();
        
        // 按句号、逗号等标点符号切分
        String[] sentences = message.split("[。，！？；]");
        
        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }
            
            int sentenceTokens = tokenUtils.estimateTokens(sentence);
            
            // 如果当前块加上这个句子超过最大大小，先保存当前块
            if (currentTokens + sentenceTokens > SHORT_CHUNK_MAX_SIZE) {
                if (currentChunk.length() > 0) {
                    shortChunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentTokens = 0;
                }
                
                // 如果句子本身就很长，单独作为一个块
                if (sentenceTokens > SHORT_CHUNK_MAX_SIZE) {
                    String[] subSentences = sentence.split(" ");
                    StringBuilder subChunk = new StringBuilder();
                    int subTokens = 0;
                    
                    for (String subSentence : subSentences) {
                        int subSentenceTokens = tokenUtils.estimateTokens(subSentence);
                        if (subTokens + subSentenceTokens > SHORT_CHUNK_MAX_SIZE) {
                            if (subChunk.length() > 0) {
                                shortChunks.add(subChunk.toString().trim());
                                subChunk = new StringBuilder();
                                subTokens = 0;
                            }
                        }
                        subChunk.append(subSentence).append(" ");
                        subTokens += subSentenceTokens;
                    }
                    
                    if (subChunk.length() > 0) {
                        shortChunks.add(subChunk.toString().trim());
                    }
                } else {
                    currentChunk.append(sentence).append("。");
                    currentTokens = sentenceTokens;
                }
            } else {
                currentChunk.append(sentence).append("。");
                currentTokens += sentenceTokens;
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            shortChunks.add(currentChunk.toString().trim());
        }
        
        // 过滤掉过短的块
        List<String> filteredChunks = new ArrayList<>();
        for (String chunk : shortChunks) {
            int chunkTokens = tokenUtils.estimateTokens(chunk);
            if (chunkTokens >= SHORT_CHUNK_MIN_SIZE || shortChunks.size() == 1) {
                filteredChunks.add(chunk);
            }
        }
        
        return filteredChunks;
    }
    
    /**
     * 获取短块大小范围
     *
     * @return 短块大小范围数组 [min, max]
     */
    public int[] getShortChunkSizeRange() {
        return new int[]{SHORT_CHUNK_MIN_SIZE, SHORT_CHUNK_MAX_SIZE};
    }
}

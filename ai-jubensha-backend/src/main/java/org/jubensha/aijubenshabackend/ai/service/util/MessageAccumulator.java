package org.jubensha.aijubenshabackend.ai.service.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息积累器
 * 用于收集和存储讨论消息，支持消息的分类存储和检索
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class MessageAccumulator {

    // 存储游戏讨论消息，key: gameId, value: 消息列表
    private final Map<Long, List<Map<String, Object>>> gameMessages = new ConcurrentHashMap<>();

    // 存储单聊消息，key: "senderId:receiverId", value: 消息列表
    private final Map<String, List<Map<String, Object>>> privateChatMessages = new ConcurrentHashMap<>();

    /**
     * 添加讨论消息
     *
     * @param gameId    游戏ID
     * @param playerId  玩家ID
     * @param playerName 玩家名称
     * @param content   消息内容
     * @param timestamp 时间戳
     */
    public void addDiscussionMessage(Long gameId, Long playerId, String playerName, String content, long timestamp) {
        List<Map<String, Object>> messages = gameMessages.computeIfAbsent(gameId, k -> new CopyOnWriteArrayList<>());

        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("gameId", gameId);
        message.put("playerId", playerId);
        message.put("playerName", playerName);
        message.put("content", content);
        message.put("timestamp", timestamp);
        message.put("type", "discussion");

        messages.add(message);
        log.debug("添加讨论消息，游戏ID: {}, 玩家: {}, 内容: {}", gameId, playerName, content);
    }

    /**
     * 添加单聊消息
     *
     * @param senderId   发送者ID
     * @param senderName 发送者名称
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param timestamp  时间戳
     */
    public void addPrivateChatMessage(Long senderId, String senderName, Long receiverId, String content, long timestamp) {
        String key = senderId + ":" + receiverId;
        List<Map<String, Object>> messages = privateChatMessages.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());

        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("senderId", senderId);
        message.put("senderName", senderName);
        message.put("receiverId", receiverId);
        message.put("content", content);
        message.put("timestamp", timestamp);
        message.put("type", "private_chat");

        messages.add(message);
        log.debug("添加单聊消息，发送者: {}, 接收者: {}, 内容: {}", senderName, receiverId, content);
    }

    /**
     * 获取游戏讨论消息
     *
     * @param gameId 游戏ID
     * @param limit  限制数量
     * @return 消息列表
     */
    public List<Map<String, Object>> getDiscussionMessages(Long gameId, int limit) {
        List<Map<String, Object>> messages = gameMessages.getOrDefault(gameId, new ArrayList<>());
        if (messages.size() <= limit) {
            return messages;
        }
        return messages.subList(messages.size() - limit, messages.size());
    }

    /**
     * 获取单聊消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param limit      限制数量
     * @return 消息列表
     */
    public List<Map<String, Object>> getPrivateChatMessages(Long senderId, Long receiverId, int limit) {
        String key = senderId + ":" + receiverId;
        List<Map<String, Object>> messages = privateChatMessages.getOrDefault(key, new ArrayList<>());
        if (messages.size() <= limit) {
            return messages;
        }
        return messages.subList(messages.size() - limit, messages.size());
    }

    /**
     * 获取游戏消息数量
     *
     * @param gameId 游戏ID
     * @return 消息数量
     */
    public int getDiscussionMessageCount(Long gameId) {
        List<Map<String, Object>> messages = gameMessages.get(gameId);
        return messages != null ? messages.size() : 0;
    }

    /**
     * 清空游戏消息
     *
     * @param gameId 游戏ID
     */
    public void clearDiscussionMessages(Long gameId) {
        gameMessages.remove(gameId);
        log.debug("清空游戏讨论消息，游戏ID: {}", gameId);
    }

    /**
     * 清空单聊消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     */
    public void clearPrivateChatMessages(Long senderId, Long receiverId) {
        String key = senderId + ":" + receiverId;
        privateChatMessages.remove(key);
        log.debug("清空单聊消息，发送者: {}, 接收者: {}", senderId, receiverId);
    }
}

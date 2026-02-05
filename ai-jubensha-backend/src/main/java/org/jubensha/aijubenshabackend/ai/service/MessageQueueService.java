package org.jubensha.aijubenshabackend.ai.service;


import java.util.List;

/**
 * 消息队列服务接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 16:00
 * @since 2026
 */
public interface MessageQueueService {

    /**
     * 发送讨论消息
     *
     * @param message      消息内容
     * @param recipientIds 接收者ID列表
     */
    void sendDiscussionMessage(String message, List<Long> recipientIds);

    /**
     * 发送单聊消息
     *
     * @param message      消息内容
     * @param senderId     发送者ID
     * @param receiverId   接收者ID
     */
    void sendPrivateChatMessage(String message, Long senderId, Long receiverId);

    /**
     * 发送答题消息
     *
     * @param answer    答案内容
     * @param playerId  玩家ID
     * @param dmId      DM ID
     */
    void sendAnswerMessage(String answer, Long playerId, Long dmId);

    /**
     * 发送评分消息
     *
     * @param score     评分内容
     * @param dmId      DM ID
     * @param playerId  玩家ID
     */
    void sendScoreMessage(java.util.Map<String, Object> score, Long dmId, Long playerId);

    /**
     * 发送系统消息
     *
     * @param message      消息内容
     * @param recipientIds 接收者ID列表
     */
    void sendSystemMessage(String message, List<Long> recipientIds);
}

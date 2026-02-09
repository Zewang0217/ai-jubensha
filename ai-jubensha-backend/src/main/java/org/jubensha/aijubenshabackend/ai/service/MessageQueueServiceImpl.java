package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 消息队列服务实现
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 16:10
 * @since 2026
 */
@Slf4j
@Service
public class MessageQueueServiceImpl implements MessageQueueService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 交换机名称
    private static final String DISCUSSION_EXCHANGE = "discussion.exchange";
    private static final String PRIVATE_EXCHANGE = "private.exchange";
    private static final String SYSTEM_EXCHANGE = "system.exchange";

    // 路由键前缀
    private static final String DISCUSSION_ROUTING_KEY_PREFIX = "discussion.";
    private static final String PRIVATE_ROUTING_KEY_PREFIX = "private.";
    private static final String SYSTEM_ROUTING_KEY_PREFIX = "system.";

    @Override
    public void sendDiscussionMessage(String message, List<Long> recipientIds) {
        log.info("发送讨论消息: {}, 接收者数量: {}", message, recipientIds.size());

        // 构建消息内容
        Map<String, Object> messageContent = Map.of(
                "type", "DISCUSSION",
                "content", message,
                "timestamp", System.currentTimeMillis(),
                "recipientIds", recipientIds
        );

        // 发送到讨论交换机
        rabbitTemplate.convertAndSend(DISCUSSION_EXCHANGE, DISCUSSION_ROUTING_KEY_PREFIX + "all", messageContent);
        log.info("讨论消息已发送到所有接收者");

        // 也可以发送到每个接收者的队列
//        for (Long recipientId : recipientIds) {
//            rabbitTemplate.convertAndSend(DISCUSSION_EXCHANGE, DISCUSSION_ROUTING_KEY_PREFIX + recipientId, messageContent);
//            log.debug("讨论消息已发送到接收者: {}", recipientId);
//        }
    }

    @Override
    public void sendPrivateChatMessage(String message, Long senderId, Long receiverId) {
        String senderName = "AI玩家" + senderId;
        String receiverName = "AI玩家" + receiverId;
        log.info("发送单聊消息: 发送者: {}, 接收者: {}, 内容: {}", senderName, receiverName, message);

        // 构建消息内容
        Map<String, Object> messageContent = Map.of(
                "type", "PRIVATE_CHAT",
                "content", message,
                "senderId", senderId,
                "receiverId", receiverId,
                "timestamp", System.currentTimeMillis()
        );

        // 发送到单聊交换机
        rabbitTemplate.convertAndSend(PRIVATE_EXCHANGE, PRIVATE_ROUTING_KEY_PREFIX + receiverId, messageContent);
        log.info("单聊消息已发送成功");
    }

    @Override
    public void sendAnswerMessage(String answer, Long playerId, Long dmId) {
        log.info("发送答题消息: {}, 玩家: {}, DM: {}", answer, playerId, dmId);

        // 构建消息内容
        Map<String, Object> messageContent = Map.of(
                "type", "ANSWER",
                "content", answer,
                "playerId", playerId,
                "dmId", dmId,
                "timestamp", System.currentTimeMillis()
        );

        // 发送到系统交换机
        rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE, SYSTEM_ROUTING_KEY_PREFIX + "dm", messageContent);
    }

    @Override
    public void sendScoreMessage(Map<String, Object> score, Long dmId, Long playerId) {
        log.info("发送评分消息: {}, DM: {}, 玩家: {}", score, dmId, playerId);

        // 构建消息内容
        Map<String, Object> messageContent = Map.of(
                "type", "SCORE",
                "content", score,
                "dmId", dmId,
                "playerId", playerId,
                "timestamp", System.currentTimeMillis()
        );

        // 发送到系统交换机
        rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE, SYSTEM_ROUTING_KEY_PREFIX + "player." + playerId, messageContent);
    }

    @Override
    public void sendSystemMessage(String message, List<Long> recipientIds) {
        log.info("发送系统消息: {}, 接收者数量: {}", message, recipientIds.size());

        // 构建消息内容
        Map<String, Object> messageContent = Map.of(
                "type", "SYSTEM",
                "content", message,
                "timestamp", System.currentTimeMillis(),
                "recipientIds", recipientIds
        );

        // 发送到系统交换机
        rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE, SYSTEM_ROUTING_KEY_PREFIX + "all", messageContent);

        // 也可以发送到每个接收者的队列
        for (Long recipientId : recipientIds) {
            rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE, SYSTEM_ROUTING_KEY_PREFIX + "player." + recipientId, messageContent);
        }
    }
}

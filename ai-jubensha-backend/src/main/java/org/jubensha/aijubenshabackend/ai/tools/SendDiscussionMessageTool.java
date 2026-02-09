package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.MessageQueueService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.service.util.MessageChunker;
import org.jubensha.aijubenshabackend.ai.service.util.TokenUtils;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 发送讨论消息工具
 * 用于AI发送讨论消息，支持向所有玩家广播消息
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class SendDiscussionMessageTool extends BaseTool {

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private MessageChunker messageChunker;

    @Override
    public String getToolName() {
        return "sendDiscussionMessage";
    }

    @Override
    public String getDisplayName() {
        return "发送讨论消息";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            String message = arguments.getStr("message");
            Long gameId = arguments.getLong("gameId");
            Long playerId = arguments.getLong("playerId");
            List<Long> recipientIds = arguments.getBeanList("recipientIds", Long.class);

            // 获取发送者信息
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
            String playerName = player.getNickname();

            // 智能处理消息
            String processedMessage = processMessage(message, playerName);

            log.debug("发送讨论消息，玩家ID: {}, 游戏ID: {}, 接收者数量: {}, 消息长度: {}", 
                    playerId, gameId, recipientIds.size(), processedMessage.length());

            // 发送消息到消息队列
            messageQueueService.sendDiscussionMessage(processedMessage, recipientIds);

            // 存储消息到向量数据库
            ragService.insertConversationMemory(gameId, playerId, playerName, processedMessage);

            return "讨论消息发送成功";
        } catch (Exception e) {
            log.error("发送讨论消息失败: {}, 消息:{}", e.getMessage(), arguments.getStr("message"), e);
            return "发送讨论消息失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public boolean execute(String message, Long gameId, Long playerId, List<Long> recipientIds) {
        try {
            // 获取发送者信息
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
            String playerName = player.getNickname();

            // 智能处理消息
            String processedMessage = processMessage(message, playerName);

            log.debug("执行讨论消息发送，玩家ID: {}, 游戏ID: {}, 接收者数量: {}, 消息长度: {}", 
                    playerId, gameId, recipientIds.size(), processedMessage.length());

            // 发送消息到消息队列
            messageQueueService.sendDiscussionMessage(processedMessage, recipientIds);

            // 存储消息到向量数据库
            ragService.insertConversationMemory(gameId, playerId, playerName, processedMessage);

            return true;
        } catch (Exception e) {
            log.error("发送讨论消息失败: {}, 消息内容:{}", e.getMessage(), message, e);
            return false;
        }
    }

    /**
     * 智能处理消息
     * 检查消息长度，识别消息类型，确保消息符合要求
     *
     * @param message    原始消息
     * @param playerName 发送者名称
     * @return 处理后的消息
     */
    private String processMessage(String message, String playerName) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // 检查消息长度
        int tokenCount = tokenUtils.estimateTokens(message);
        log.debug("原始消息token数: {}", tokenCount);

        // 识别消息类型
        MessageChunker.MessageType messageType = messageChunker.identifyMessageType(message, playerName);
        log.debug("识别消息类型: {}", messageType);

        // 检查是否需要处理
        if (tokenUtils.exceedsSafeThreshold(message)) {
            log.warn("消息长度超过安全阈值，进行智能处理，原始长度: {}, token数: {}", message.length(), tokenCount);
            
            // 根据消息类型选择处理策略
            if (messageType == MessageChunker.MessageType.DM_MESSAGE) {
                // DM消息：保留核心信息，截断详细说明
                log.warn("DM消息已处理，可能包含规则说明等详细内容");
            } else if (messageType == MessageChunker.MessageType.SYSTEM_MESSAGE) {
                // 系统消息：保留关键信息，简化格式
                log.warn("系统消息已处理，可能包含状态更新等信息");
            }

            // 智能截断
            return tokenUtils.truncateText(message);
        }

        return message;
    }

    /**
     * 截断消息长度，确保不超过模型的token限制
     * 简单估算：1个token约等于4个字符
     */
    private String truncateMessage(String message) {
        // 设置最大字符数（512 tokens * 4 chars/token = 2048 chars）
        final int MAX_CHARS = 2048;
        
        if (message.length() > MAX_CHARS) {
            // 截断消息并添加省略号
            message = message.substring(0, MAX_CHARS - 3) + "...";
            log.warn("消息长度超过限制，已截断: {}", message.length());
        }
        
        return message;
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case PLAYER:
                // DM和Player可以发送讨论消息
                return ToolPermissionLevel.PLAYER;
            case JUDGE:
            case SUMMARY:
                // Judge和Summary不应该发送讨论消息
                return ToolPermissionLevel.NONE;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

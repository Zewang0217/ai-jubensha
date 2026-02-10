package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
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
 * 用于AI发送讨论消息，支持向指定玩家广播消息
 * <p>
 * 工具说明：
 * - 功能：发送讨论消息到指定玩家，自动处理消息长度，存储消息到向量数据库
 * - 参数：
 *   - message：消息内容（必填）
 *   - gameId：游戏ID（必填）
 *   - playerId：发送者ID（必填）
 *   - recipientIds：接收者ID列表（必填）
 * - 返回格式：消息发送结果
 * - 权限：DM和Player可以发送讨论消息，Judge和Summary不应该发送讨论消息
 * <p>
 * 调用时机：
 * 1. 陈述阶段，玩家进行角色背景陈述时
 * 2. 自由讨论阶段，玩家发表对案件的分析时
 * 3. 线索讨论阶段，玩家针对线索发表见解时
 * 4. 单聊结束后，玩家分享重要信息时
 * 5. 答题阶段，玩家提交最终答案时
 * <p>
 * 示例调用：
 * {
 *   "toolcall": {
 *     "thought": "需要向其他玩家分享我的分析",
 *     "name": "sendDiscussionMessage",
 *     "params": {
 *       "message": "根据我的分析，凶手应该是...",
 *       "gameId": "1",
 *       "playerId": "2",
 *       "recipientIds": [1, 2, 3, 4, 5]
 *     }
 *   }
 * }
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-02-10
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
    @Tool("发送讨论消息")
    public String executeSendDiscussionMessage(@P("消息内容") String message, @P("游戏ID") Long gameId, @P("玩家ID") Long playerId, @P("接收者ID列表") List<Long> recipientIds) {
        // 创建参数对象
        JSONObject arguments = new JSONObject();
        arguments.put("message", message);
        arguments.put("gameId", gameId);
        arguments.put("playerId", playerId);
        arguments.put("recipientIds", recipientIds);
        // 调用核心逻辑方法
        return generateToolExecutedResult(arguments);
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

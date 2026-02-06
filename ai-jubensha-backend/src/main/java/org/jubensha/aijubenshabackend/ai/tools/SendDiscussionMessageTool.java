package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.MessageQueueService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
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

            log.debug("发送讨论消息，玩家ID: {}, 游戏ID: {}, 接收者数量: {}", playerId, gameId, recipientIds.size());

            // 发送消息到消息队列
            messageQueueService.sendDiscussionMessage(message, recipientIds);

            // 存储消息到向量数据库
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
            String playerName = player.getNickname();
            ragService.insertConversationMemory(gameId, playerId, playerName, message);

            return "讨论消息发送成功";
        } catch (Exception e) {
            log.error("发送讨论消息失败: {}", e.getMessage(), e);
            return "发送讨论消息失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public boolean execute(String message, Long gameId, Long playerId, List<Long> recipientIds) {
        try {
            // 发送消息到消息队列
            messageQueueService.sendDiscussionMessage(message, recipientIds);

            // 存储消息到向量数据库
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
            String playerName = player.getNickname();
            ragService.insertConversationMemory(gameId, playerId, playerName, message);

            return true;
        } catch (Exception e) {
            log.error("发送讨论消息失败: {}", e.getMessage(), e);
            return false;
        }
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

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
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private org.jubensha.aijubenshabackend.ai.service.util.ScrollingSummaryManager scrollingSummaryManager;
    
    @Autowired
    private WebSocketService webSocketService;

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
            String message = arguments.getStr("message");
            Long gameId = arguments.getLong("gameId");
            Long playerId = arguments.getLong("playerId");
            List<Long> recipientIds = arguments.getBeanList("recipientIds", Long.class);

            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
            String playerName = player.getNickname();

            String processedMessage = processMessage(message, playerName);

            log.info("[陈述阶段] AI玩家 {} ({}) 陈述内容: {}", playerId, playerName, processedMessage.substring(0, Math.min(50, processedMessage.length())) + "...");

            messageQueueService.sendDiscussionMessage(processedMessage, recipientIds);

            Map<String, Object> chatPayload = new HashMap<>();
            chatPayload.put("playerId", playerId);
            chatPayload.put("playerName", playerName);
            chatPayload.put("message", processedMessage);
            chatPayload.put("timestamp", System.currentTimeMillis());
            
            WebSocketMessage wsMessage = new WebSocketMessage();
            wsMessage.setType(WebSocketMessage.MessageType.CHAT_MESSAGE);
            wsMessage.setSender(playerId);
            wsMessage.setPayload(chatPayload);
            
            webSocketService.broadcastChatMessage(gameId, wsMessage);

            ragService.insertConversationMemory(gameId, playerId, playerName, processedMessage);

            scrollingSummaryManager.incrementMessageCount(gameId);

            return "讨论消息发送成功";
        } catch (Exception e) {
            log.error("发送讨论消息失败: {}, 消息:{}", e.getMessage(), arguments.getStr("message"), e);
            return "发送讨论消息失败: " + e.getMessage();
        }
    }

    @Tool("发送讨论消息")
    public String executeSendDiscussionMessage(@P("消息内容") String message, @P("游戏ID") Long gameId, @P("玩家ID") Long playerId, @P("接收者ID列表") List<Long> recipientIds) {
        JSONObject arguments = new JSONObject();
        arguments.put("message", message);
        arguments.put("gameId", gameId);
        arguments.put("playerId", playerId);
        arguments.put("recipientIds", recipientIds);
        return generateToolExecutedResult(arguments);
    }

    private String processMessage(String message, String playerName) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String processedMessage = extractMessageFromToolCall(message);
        if (processedMessage != null) {
            message = processedMessage;
        }

        int tokenCount = tokenUtils.estimateTokens(message);
        log.debug("原始消息token数: {}", tokenCount);

        MessageChunker.MessageType messageType = messageChunker.identifyMessageType(message, playerName);

        if (playerName != null && (playerName.startsWith("AI_") || playerName.contains("AI") || playerName.contains("智能"))) {
            messageType = MessageChunker.MessageType.AI_PLAYER_MESSAGE;
        }

        log.debug("识别消息类型: {}", messageType);

        return message;
    }

    private String extractMessageFromToolCall(String message) {
        try {
            if (message.trim().startsWith("{")) {
                cn.hutool.json.JSONObject jsonObject = new cn.hutool.json.JSONObject(message.trim());
                
                if (jsonObject.containsKey("toolcall")) {
                    cn.hutool.json.JSONObject toolcall = jsonObject.getJSONObject("toolcall");
                    if (toolcall.containsKey("params") && toolcall.getJSONObject("params").containsKey("message")) {
                        String extractedMessage = toolcall.getJSONObject("params").getStr("message");
                        log.debug("从工具调用JSON中提取消息内容，长度: {}", extractedMessage.length());
                        return extractedMessage;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("消息不是有效的工具调用JSON: {}", e.getMessage());
        }
        return null;
    }

    private String truncateMessage(String message) {
        final int MAX_CHARS = 2048;

        if (message.length() > MAX_CHARS) {
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
                return ToolPermissionLevel.PLAYER;
            case JUDGE:
            case SUMMARY:
                return ToolPermissionLevel.NONE;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

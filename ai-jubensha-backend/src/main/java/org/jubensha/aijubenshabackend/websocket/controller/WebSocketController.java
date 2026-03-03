package org.jubensha.aijubenshabackend.websocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 控制器
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * 1. 前端发送到聊天室的消息
     * 客户端发送到 /app/game/{gameId}/chat
     */
    @MessageMapping("/game/{gameId}/chat")
    public void handleChatMessage(
            @DestinationVariable Long gameId,
            @Payload WebSocketMessage message) {
        log.debug("游戏 {} 收到聊天消息", gameId);
        webSocketService.broadcastChatMessage(gameId, message);
    }

    /**
     * 2. 前端发送的投票信息
     * 客户端发送到 /app/game/{gameId}/vote
     */
    @MessageMapping("/game/{gameId}/vote")
    public void handleVote(
            @DestinationVariable Long gameId,
            @Payload WebSocketMessage message) {
        log.info("游戏 {} 收到投票: {}", gameId, message.getPayload());
        // TODO: 处理投票逻辑
    }
}

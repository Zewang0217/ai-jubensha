package org.jubensha.aijubenshabackend.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * @author zewang
 * @author luobo
 */
@Slf4j
@Controller
public class WebSocketController {

    private final WebSocketService webSocketService;

    @Autowired
    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    /**
     * 简单的广播消息
     * 客户端发送到 /app/broadcast，服务器广播到 /topic/public
     */
    @MessageMapping("/broadcast")  // 接收路径: /app/broadcast
    @SendTo("/topic/public")       // 广播路径: /topic/public
    public WebSocketMessage broadcastMessage(
            @Payload WebSocketMessage message
    ) {
        log.info("收到广播消息: {}", message);
        return message;
    }

    /**
     * 处理指定 GameRoom 的消息
     */
    @MessageMapping("/game/{gameId}/message")
    public void sendToRoom(
            @DestinationVariable Long gameId,
            @Payload WebSocketMessage message
    ) {
        log.info("房间 {} 收到消息: {}", gameId, message);
        // 发送消息到特定游戏的频道
        // 手动指定发送路径
        webSocketService.sendToGameRoom(gameId, message);
    }
}

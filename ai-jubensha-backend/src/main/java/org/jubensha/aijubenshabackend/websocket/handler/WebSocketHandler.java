package org.jubensha.aijubenshabackend.websocket.handler;

import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.jubensha.aijubenshabackend.websocket.message.GameMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public WebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * 处理客户端发送的聊天消息
     */
    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public WebSocketMessage handleChatMessage(WebSocketMessage message) {
        logger.info("Received chat message: {}", message);
        return message;
    }
    
    /**
     * 处理游戏消息
     */
    @MessageMapping("/game/message")
    public void handleGameMessage(GameMessage gameMessage) {
        logger.info("Received game message for game {}: {}", gameMessage.getGameId(), gameMessage.getMessage());
        // 发送消息到特定游戏的频道
        messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getGameId(), gameMessage.getMessage());
    }
    
    /**
     * 处理玩家操作
     */
    @MessageMapping("/game/action")
    public void handleGameAction(GameMessage gameMessage) {
        logger.info("Received game action for game {}: {}", gameMessage.getGameId(), gameMessage.getMessage());
        // 发送操作到特定游戏的频道
        messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getGameId() + "/actions", gameMessage.getMessage());
    }
}

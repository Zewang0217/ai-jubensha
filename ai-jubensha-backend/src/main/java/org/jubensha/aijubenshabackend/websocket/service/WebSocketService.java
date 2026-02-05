package org.jubensha.aijubenshabackend.websocket.service;

import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送消息到所有客户端
     */
    public void broadcastMessage(String topic, WebSocketMessage message) {
        logger.info("Broadcasting message to {}: {}", topic, message);
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * 发送消息到特定游戏
     */
    public void sendToGame(Long gameId, WebSocketMessage message) {
        logger.info("Sending message to game {}: {}", gameId, message);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }

    /**
     * 发送消息到特定用户
     */
    public void sendToUser(String username, WebSocketMessage message) {
        logger.info("Sending message to user {}: {}", username, message);
        messagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
    }

    /**
     * 发送游戏状态更新
     */
    public void sendGameStateUpdate(Long gameId, Object gameState) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("GAME_STATE_UPDATE");
        message.setPayload(gameState);
        sendToGame(gameId, message);
    }

    /**
     * 发送聊天消息
     */
    public void sendChatMessage(Long gameId, String sender, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("CHAT_MESSAGE");
        message.setPayload("{\"sender\": \"" + sender + "\", \"content\": \"" + content + "\"}");
        sendToGame(gameId, message);
    }

    /**
     * 发送线索发现消息
     */
    public void sendClueFoundMessage(Long gameId, String clueName, String clueDescription) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("CLUE_FOUND");
        message.setPayload("{\"clueName\": \"" + clueName + "\", \"clueDescription\": \"" + clueDescription + "\"}");
        sendToGame(gameId, message);
    }

    /**
     * 发送阶段转换消息
     */
    public void sendPhaseChangeMessage(Long gameId, String phase) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("PHASE_CHANGE");
        message.setPayload("{\"phase\": \"" + phase + "\"}");
        sendToGame(gameId, message);
    }
}

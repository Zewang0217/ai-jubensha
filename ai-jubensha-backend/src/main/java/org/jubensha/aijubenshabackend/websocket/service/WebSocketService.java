package org.jubensha.aijubenshabackend.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class WebSocketService {

    //    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    PlayerService playerService = SpringContextUtil.getBean(PlayerService.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送消息到所有客户端
     */
    public void broadcastMessage(String topic, WebSocketMessage message) {
        log.info("Broadcasting message to {}: {}", topic, message);
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * 发送消息到特定游戏
     */
    public void sendToGame(Long gameId, WebSocketMessage message) {
        log.info("Sending message to game {}: {}", gameId, message);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }

    /**
     * 发送消息到特定用户
     */
    public void sendToUser(String username, WebSocketMessage message) {
        log.info("Sending message to user {}: {}", username, message);
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

    /**
     * // TODO: 待完善
     * 通知玩家开始搜证
     */
    public void notifyPlayerStartInvestigation(Long playerId, List<Map<String, Object>> investigationScenes) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("START_INVESTIGATION");
        message.setPayload(investigationScenes);
        String username = getUsernameByPlayerId(playerId);
        sendToUser(username, message);
    }

    /**
     * // TODO: 待完善
     * 根据玩家ID获取用户名
     *
     * @param playerId 玩家ID
     * @return 玩家用户名，如果未找到则返回默认用户名"User"+playerId
     */
    private String getUsernameByPlayerId(Long playerId) {
        try {
            // 使用已注入的服务实例而非手动获取Bean
            Optional<Player> player = playerService.getPlayerById(playerId);
            if (player.isPresent()) {
                return player.get().getUsername();
            }
            log.warn("Player not found for id: {}, using default username", playerId);
        } catch (Exception e) {
            log.error("Error getting username for player id: {}", playerId, e);
        }
        // 默认用户名
        return "User" + playerId;
    }
}

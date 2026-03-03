package org.jubensha.aijubenshabackend.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerSessionManager sessionManager;
    private final GamePlayerService gamePlayerService;

    @Override
    public void broadcastChatMessage(Long gameId, WebSocketMessage message) {
        String destination = "/topic/game/" + gameId + "/chat";
        messagingTemplate.convertAndSend(destination, message);
        log.debug("广播聊天消息到游戏 {}: {}", gameId, message);
    }

    @Override
    public void notifyGamePlayerScriptReady(Long gamePlayerId, Long scriptId) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.MessageType.SCRIPT_READY);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gamePlayerId", gamePlayerId);
        payload.put("scriptId", scriptId);
        message.setPayload(payload);

        sendToGamePlayer(gamePlayerId, message);
        log.info("通知玩家 {} 剧本已就绪: scriptId={}", gamePlayerId, scriptId);
    }

    @Override
    public void notifyPlayerStartInvestigation(Long gamePlayerId, Map<String, Object> investigationScenes) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.MessageType.START_INVESTIGATION);
        message.setPayload(investigationScenes);

        sendToGamePlayer(gamePlayerId, message);
        log.info("通知玩家 {} 开始搜证", gamePlayerId);
    }

    @Override
    public void broadcastPublicClues(Long gameId, List<?> clues) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.MessageType.PUBLIC_CLUE);
        message.setPayload(clues);

        sendToGameRealPlayers(gameId, message);
        log.info("广播公开线索到游戏 {}: {} 条线索", gameId, clues.size());
    }

    @Override
    public void broadcastVoteResult(Long gameId, Long murdererId, Map<Long, Long> voteDetails, Integer dmScore) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessage.MessageType.VOTE_RESULT);

        Map<String, Object> payload = new HashMap<>();
        payload.put("murdererId", murdererId);
        payload.put("voteDetails", voteDetails);
        payload.put("dmScore", dmScore);
        message.setPayload(payload);

        sendToGameRealPlayers(gameId, message);
        log.info("广播投票结果到游戏 {}: murdererId={}", gameId, murdererId);
    }

    /**
     * 发送消息给指定GamePlayer
     */
    private void sendToGamePlayer(Long gamePlayerId, WebSocketMessage message) {
        String sessionId = sessionManager.getSessionId(gamePlayerId);
        if (sessionId == null) {
            log.warn("GamePlayer {} 不在线，无法发送消息", gamePlayerId);
            return;
        }

        // 通过用户目的地发送（使用gamePlayerId作为用户标识）
        messagingTemplate.convertAndSendToUser(
                gamePlayerId.toString(),
                "/queue/messages",
                message
        );
    }

    /**
     * 发送消息给游戏的所有真人玩家
     */
    private void sendToGameRealPlayers(Long gameId, WebSocketMessage message) {
        List<GamePlayer> realPlayers = gamePlayerService.getRealPlayersByGameId(gameId);

        for (GamePlayer gamePlayer : realPlayers) {
            Long gamePlayerId = gamePlayer.getId();
            if (sessionManager.isConnected(gamePlayerId)) {
                sendToGamePlayer(gamePlayerId, message);
            }
        }
    }
}

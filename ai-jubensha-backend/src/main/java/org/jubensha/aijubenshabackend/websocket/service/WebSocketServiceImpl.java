package org.jubensha.aijubenshabackend.websocket.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket 服务实现
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;
    
    @Resource
    private PlayerSessionManager sessionManager;
    
    @Resource
    private GamePlayerService gamePlayerService;
    
    @Resource
    private GameService gameService;
    
    @Resource
    private PlayerService playerService;
    
    @Resource
    private CharacterService characterService;
    
    @Resource
    private DialogueRepository dialogueRepository;
    
    @Resource
    private MessageAccumulator messageAccumulator;
    
    @Resource
    @Lazy
    private DiscussionService discussionService;

    @Override
    public void broadcastChatMessage(Long gameId, WebSocketMessage message) {
        String destination = "/topic/game/" + gameId + "/chat";
        messagingTemplate.convertAndSend(destination, message);
        log.debug("广播聊天消息到游戏 {}: {}", gameId, message);
        
        // 处理真人玩家发言
        handleRealPlayerMessage(gameId, message);
    }
    
    /**
     * 处理真人玩家的发言
     * @param gameId 游戏ID
     * @param message WebSocket消息
     */
    private void handleRealPlayerMessage(Long gameId, WebSocketMessage message) {
        try {
            Long senderId = message.getSender();
            Object payload = message.getPayload();
            String content = payload != null ? payload.toString() : "";
            
            if (senderId == null || content.isEmpty()) {
                log.warn("无效的聊天消息: senderId={}, content={}", senderId, content);
                return;
            }
            
            log.info("处理真人玩家发言，游戏ID: {}, 发送者ID: {}, 内容: {}", gameId, senderId, content);
            
            // 获取角色ID
            Long characterId = null;
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, senderId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                if (gamePlayer.getCharacter() != null) {
                    characterId = gamePlayer.getCharacter().getId();
                }
            }
            
            // 存储讨论消息到数据库
            storeDiscussionMessageToDatabase(gameId, senderId, characterId, content);
            
            // 将消息添加到讨论历史
            if (messageAccumulator != null) {
                // 获取玩家名称
                String playerName = "真人玩家" + senderId;
                Optional<GamePlayer> gamePlayerOpt2 = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, senderId);
                if (gamePlayerOpt2.isPresent()) {
                    GamePlayer gamePlayer = gamePlayerOpt2.get();
                    if (gamePlayer.getCharacter() != null) {
                        playerName = gamePlayer.getCharacter().getName();
                    }
                }
                
                // 调用正确的方法签名
                messageAccumulator.addDiscussionMessage(
                        gameId,
                        senderId,
                        playerName,
                        content,
                        System.currentTimeMillis()
                );
                log.info("真人玩家发言已添加到讨论历史，游戏ID: {}, 发送者ID: {}", gameId, senderId);
            } else {
                log.warn("MessageAccumulator为null，无法添加消息到讨论历史");
            }
            
        } catch (Exception e) {
            log.error("处理真人玩家发言失败", e);
        }
    }
    
    /**
     * 存储讨论消息到数据库
     * @param gameId 游戏ID
     * @param playerId 玩家ID
     * @param characterId 角色ID
     * @param content 消息内容
     */
    private void storeDiscussionMessageToDatabase(Long gameId, Long playerId, Long characterId, String content) {
        try {
            log.debug("存储讨论消息，游戏ID: {}, 玩家ID: {}, 角色ID: {}", gameId, playerId, characterId);
            
            // 创建对话记录
            org.jubensha.aijubenshabackend.models.entity.Dialogue dialogue = new org.jubensha.aijubenshabackend.models.entity.Dialogue();
            
            // 设置游戏信息
            org.jubensha.aijubenshabackend.models.entity.Game game = gameService.getGameById(gameId).orElse(null);
            if (game == null) {
                log.warn("游戏不存在，游戏ID: {}", gameId);
                return;
            }
            dialogue.setGame(game);
            
            // 设置玩家信息
            org.jubensha.aijubenshabackend.models.entity.Player player = playerService.getPlayerById(playerId).orElse(null);
            if (player == null) {
                log.warn("玩家不存在，玩家ID: {}", playerId);
                return;
            }
            dialogue.setPlayer(player);
            
            // 设置角色信息
            if (characterId != null) {
                org.jubensha.aijubenshabackend.models.entity.Character character = characterService.getCharacterById(characterId).orElse(null);
                if (character != null) {
                    dialogue.setCharacter(character);
                }
            }
            
            // 设置消息内容和类型
            dialogue.setContent(content);
            dialogue.setType(org.jubensha.aijubenshabackend.models.enums.DialogueType.CHAT);
            
            // 保存到数据库
            dialogueRepository.save(dialogue);
            log.info("讨论消息已成功存储到数据库，对话ID: {}", dialogue.getId());
            
        } catch (Exception e) {
            log.error("存储讨论消息失败: {}", e.getMessage(), e);
        }
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

    @Override
    public void broadcastPhaseChange(Long gameId, GamePhase newPhase) {
        WebSocketMessage message = new WebSocketMessage("PHASE_CHANGE", 0L, newPhase.name());
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/phase", message);
    }

    @Override
    public void broadcastPhaseReady(Long gameId, String nodeName, Boolean isReady, String message) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType(WebSocketMessage.MessageType.PHASE_READY);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("nodeName", nodeName);
        payload.put("isReady", isReady);
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        wsMessage.setPayload(payload);
        
        // 广播到游戏的阶段主题
        String destination = "/topic/game/" + gameId + "/phase";
        messagingTemplate.convertAndSend(destination, wsMessage);
        log.info("广播阶段就绪状态到游戏 {}: nodeName={}, isReady={}, message={}", gameId, nodeName, isReady, message);
    }

    @Override
    public void broadcastInvestigationComplete(Long gameId, String message) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType(WebSocketMessage.MessageType.INVESTIGATION_COMPLETE);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        wsMessage.setPayload(payload);
        
        String destination = "/topic/game/" + gameId + "/investigation";
        messagingTemplate.convertAndSend(destination, wsMessage);
        log.info("广播搜证完成通知到游戏 {}: {}", gameId, message);
    }

    @Override
    public void handleVote(Long gameId, WebSocketMessage message) {
        try {
            log.info("处理投票消息: gameId={}, message={}", gameId, message);
            
            // 解析消息payload
            Map<String, Object> payload = (Map<String, Object>) message.getPayload();
            
            // 提取投票数据
            Long targetId = Long.valueOf(payload.get("targetId").toString());
            Long playerId = Long.valueOf(payload.get("playerId").toString());
            String voteMessage = (String) payload.get("voteMessage");
            
            log.info("解析投票数据: targetId={}, playerId={}, voteMessage={}", targetId, playerId, voteMessage);
            
            // 调用DiscussionService的submitAnswer方法存储答案
            discussionService.submitAnswer(playerId, voteMessage);
            log.info("真人玩家答题已提交，玩家ID: {}, 答案: {}", playerId, voteMessage);
            
            // 发送投票成功的反馈消息
            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("status", "success");
            responsePayload.put("message", "投票成功");
            responsePayload.put("playerId", playerId);
            responsePayload.put("targetId", targetId);
            
            WebSocketMessage response = new WebSocketMessage("VOTE_SUCCESS", playerId, responsePayload);
            
            // 发送反馈消息给投票的玩家
            sendToGamePlayer(playerId, response);
            log.info("已发送投票成功反馈给玩家: {}", playerId);
            
        } catch (Exception e) {
            log.error("处理投票消息失败", e);
            
            // 发送投票失败的反馈消息
            try {
                Long playerId = message.getSender();
                if (playerId != null) {
                    Map<String, Object> errorPayload = new HashMap<>();
                    errorPayload.put("status", "error");
                    errorPayload.put("message", "投票失败，请重试");
                    
                    WebSocketMessage errorResponse = new WebSocketMessage("VOTE_ERROR", playerId, errorPayload);
                    
                    sendToGamePlayer(playerId, errorResponse);
                    log.info("已发送投票失败反馈给玩家: {}", playerId);
                }
            } catch (Exception ex) {
                log.error("发送错误反馈失败", ex);
            }
        }
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

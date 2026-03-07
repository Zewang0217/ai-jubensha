package org.jubensha.aijubenshabackend.websocket.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.service.util.DMModerator;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.ScrollingSummaryManager;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
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
 * 处理WebSocket消息的发送和接收，包括真人玩家的发言和投票
 *
 * @author Zewang
 * @version 1.1
 * @since 2026
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
    @org.springframework.context.annotation.Lazy
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
    
    @Resource
    @Lazy
    private DMModerator dmModerator;
    
    @Resource
    @Lazy
    private TurnManager turnManager;
    
    @Resource
    @Lazy
    private RAGService ragService;
    
    @Resource
    @Lazy
    private ScrollingSummaryManager scrollingSummaryManager;

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
     * 根据当前讨论阶段执行不同的处理逻辑：
     * - 陈述阶段：通知DMModerator真人玩家已发言
     * - 自由讨论阶段：广播消息并添加到讨论历史
     *
     * @param gameId 游戏ID
     * @param message WebSocket消息
     */
    private void handleRealPlayerMessage(Long gameId, WebSocketMessage message) {
        try {
            Long senderId = message.getSender();
            Object payload = message.getPayload();
            String content = payload != null ? payload.toString() : "";
            
            if (senderId == null || content.isEmpty()) {
                log.warn("[WebSocket] 无效的聊天消息: senderId={}, content={}", senderId, content);
                return;
            }
            
            log.info("[WebSocket] 处理真人玩家发言，游戏ID: {}, 发送者ID: {}, 内容: {}", gameId, senderId, content);
            
            // 获取当前讨论阶段
            String currentPhase = turnManager.getCurrentPhase(gameId);
            log.info("[WebSocket] 当前讨论阶段: {}", currentPhase);
            
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
            
            // 获取玩家名称
            String playerName = "真人玩家" + senderId;
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                if (gamePlayer.getCharacter() != null) {
                    playerName = gamePlayer.getCharacter().getName();
                }
            }
            
            // 将消息添加到讨论历史
            if (messageAccumulator != null) {
                messageAccumulator.addDiscussionMessage(
                        gameId,
                        senderId,
                        playerName,
                        content,
                        System.currentTimeMillis()
                );
                log.info("[WebSocket] 真人玩家发言已添加到讨论历史，游戏ID: {}, 发送者ID: {}", gameId, senderId);
            } else {
                log.warn("[WebSocket] MessageAccumulator为null，无法添加消息到讨论历史");
            }
            
            // 存储到RAG向量数据库，使AI玩家能够检索真人发言
            if (ragService != null) {
                try {
                    ragService.insertConversationMemory(gameId, senderId, playerName, content);
                    log.info("[WebSocket] 真人玩家发言已存储到RAG向量数据库，游戏ID: {}, 发送者ID: {}, 玩家名称: {}", gameId, senderId, playerName);
                    
                    // 增加消息计数，用于滚动摘要触发
                    if (scrollingSummaryManager != null) {
                        scrollingSummaryManager.incrementMessageCount(gameId);
                    }
                } catch (Exception e) {
                    log.error("[WebSocket] 存储真人玩家发言到RAG失败，游戏ID: {}, 发送者ID: {}, 错误: {}", gameId, senderId, e.getMessage(), e);
                }
            } else {
                log.warn("[WebSocket] RAGService为null，无法存储真人玩家发言到向量数据库");
            }
            
            // 根据讨论阶段执行不同的处理
            if (TurnManager.PHASE_STATEMENT.equals(currentPhase)) {
                // 陈述阶段：通知DMModerator真人玩家已发言
                log.info("[WebSocket] 陈述阶段，通知DMModerator真人玩家 {} 已发言", senderId);
                dmModerator.onRealPlayerStatementReceived(gameId, senderId);
            } else if (TurnManager.PHASE_FREE_DISCUSSION.equals(currentPhase)) {
                // 自由讨论阶段：消息已广播，无需额外处理
                log.info("[WebSocket] 自由讨论阶段，消息已广播");
            }
            
        } catch (Exception e) {
            log.error("[WebSocket] 处理真人玩家发言失败", e);
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
            
            if (characterId == null) {
                log.warn("角色ID为空，跳过存储讨论消息到数据库，游戏ID: {}, 玩家ID: {}", gameId, playerId);
                return;
            }
            
            org.jubensha.aijubenshabackend.models.entity.Dialogue dialogue = new org.jubensha.aijubenshabackend.models.entity.Dialogue();
            
            org.jubensha.aijubenshabackend.models.entity.Game game = gameService.getGameById(gameId).orElse(null);
            if (game == null) {
                log.warn("游戏不存在，游戏ID: {}", gameId);
                return;
            }
            dialogue.setGame(game);
            
            org.jubensha.aijubenshabackend.models.entity.Player player = playerService.getPlayerById(playerId).orElse(null);
            if (player == null) {
                log.warn("玩家不存在，玩家ID: {}", playerId);
                return;
            }
            dialogue.setPlayer(player);
            
            org.jubensha.aijubenshabackend.models.entity.Character character = characterService.getCharacterById(characterId).orElse(null);
            if (character == null) {
                log.warn("角色不存在，角色ID: {}", characterId);
                return;
            }
            dialogue.setCharacter(character);
            
            dialogue.setContent(content);
            dialogue.setType(org.jubensha.aijubenshabackend.models.enums.DialogueType.CHAT);
            
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
        broadcastPhaseChange(gameId, null, newPhase, "阶段已切换");
    }

    /**
     * 广播阶段变化通知（包含详细信息）
     *
     * @param gameId 游戏ID
     * @param previousPhase 上一阶段
     * @param newPhase 新阶段
     * @param message 提示消息
     */
    public void broadcastPhaseChange(Long gameId, GamePhase previousPhase, GamePhase newPhase, String message) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType(WebSocketMessage.MessageType.PHASE_CHANGE);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("newPhase", newPhase != null ? newPhase.toFrontendFormat() : null);
        payload.put("previousPhase", previousPhase != null ? previousPhase.toFrontendFormat() : null);
        payload.put("newPhaseBackend", newPhase != null ? newPhase.name() : null);
        payload.put("previousPhaseBackend", previousPhase != null ? previousPhase.name() : null);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("message", message != null ? message : "进入" + (newPhase != null ? newPhase.getTitle() : "新阶段"));
        payload.put("phaseTitle", newPhase != null ? newPhase.getTitle() : null);
        payload.put("phaseDescription", newPhase != null ? newPhase.getDescription() : null);
        wsMessage.setPayload(payload);
        
        // 广播到游戏的阶段主题
        String destination = "/topic/game/" + gameId + "/phase";
        messagingTemplate.convertAndSend(destination, wsMessage);
        log.info("广播阶段变化通知到游戏 {}: {} -> {}, message={}", 
            gameId, 
            previousPhase != null ? previousPhase.name() : "null", 
            newPhase != null ? newPhase.name() : "null", 
            message);
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
            log.info("[投票处理] 处理投票消息: gameId={}, message={}", gameId, message);
            
            // 解析消息payload
            Map<String, Object> payload = (Map<String, Object>) message.getPayload();
            
            // 提取投票数据
            Long targetId = Long.valueOf(payload.get("targetId").toString());
            Long playerId = Long.valueOf(payload.get("playerId").toString());
            String voteMessage = (String) payload.get("voteMessage");
            
            log.info("[投票处理] 解析投票数据: targetId={}, playerId={}, voteMessage={}", targetId, playerId, voteMessage);
            
            // 获取DiscussionServiceImpl实例并调用onRealPlayerVoteReceived方法
            try {
                // 通过反射调用onRealPlayerVoteReceived方法
                java.lang.reflect.Method method = discussionService.getClass().getMethod("onRealPlayerVoteReceived", Long.class, String.class);
                method.invoke(discussionService, playerId, voteMessage);
                log.info("[投票处理] 已通知DiscussionService真人玩家 {} 投票完成", playerId);
            } catch (Exception e) {
                log.error("[投票处理] 调用DiscussionService.onRealPlayerVoteReceived失败: {}", e.getMessage(), e);
                // 备用方案：直接调用submitAnswer
                discussionService.submitAnswer(playerId, voteMessage);
                log.info("[投票处理] 真人玩家答题已提交（备用方案），玩家ID: {}, 答案: {}", playerId, voteMessage);
            }
            
            // 发送投票成功的反馈消息
            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("status", "success");
            responsePayload.put("message", "投票成功");
            responsePayload.put("playerId", playerId);
            responsePayload.put("targetId", targetId);
            
            WebSocketMessage response = new WebSocketMessage("VOTE_SUCCESS", playerId, responsePayload);
            
            // 发送反馈消息给投票的玩家
            sendToGamePlayer(playerId, response);
            log.info("[投票处理] 已发送投票成功反馈给玩家: {}", playerId);
            
        } catch (Exception e) {
            log.error("[投票处理] 处理投票消息失败", e);
            
            // 发送投票失败的反馈消息
            try {
                Long playerId = message.getSender();
                if (playerId != null) {
                    Map<String, Object> errorPayload = new HashMap<>();
                    errorPayload.put("status", "error");
                    errorPayload.put("message", "投票失败，请重试");
                    
                    WebSocketMessage errorResponse = new WebSocketMessage("VOTE_ERROR", playerId, errorPayload);
                    
                    sendToGamePlayer(playerId, errorResponse);
                    log.info("[投票处理] 已发送投票失败反馈给玩家: {}", playerId);
                }
            } catch (Exception ex) {
                log.error("[投票处理] 发送错误反馈失败", ex);
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

    @Override
    public void broadcastGameEnded(Long gameId, String message) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType(WebSocketMessage.MessageType.GAME_ENDED);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("gameId", gameId);
        payload.put("timestamp", System.currentTimeMillis());
        wsMessage.setPayload(payload);
        
        // 广播到游戏主频道
        String destination = "/topic/game/" + gameId + "/status";
        messagingTemplate.convertAndSend(destination, wsMessage);
        log.info("广播游戏结束通知到游戏 {}: {}", gameId, message);
        
        // 同时发送给所有真人玩家
        sendToGameRealPlayers(gameId, wsMessage);
    }

    @Override
    public void broadcastAgentAction(Long gameId, String actionType, String agentName, String targetName, String message, Boolean isPublic) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setType(WebSocketMessage.MessageType.AGENT_ACTION);

        Map<String, Object> payload = new HashMap<>();
        payload.put("actionType", actionType);
        payload.put("agentName", agentName);
        payload.put("targetName", targetName);
        payload.put("message", message);
        payload.put("isPublic", isPublic);
        payload.put("timestamp", LocalDateTime.now().toString());
        wsMessage.setPayload(payload);

        // 广播到游戏公屏主题
        String destination = "/topic/game/" + gameId + "/agent-actions";
        messagingTemplate.convertAndSend(destination, wsMessage);
        log.info("广播AI Agent操作到游戏 {}: type={}, agent={}, target={}, message={}",
                gameId, actionType, agentName, targetName, message);

        // 同时发送给所有真人玩家（作为个人消息）
        sendToGameRealPlayers(gameId, wsMessage);
    }
}

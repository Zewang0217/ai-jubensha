package org.jubensha.aijubenshabackend.websocket.service;

import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.websocket.message.WebSocketMessage;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 服务接口
 */
public interface WebSocketService {

    /**
     * 1. 向指定gameId的所有真人GamePlayer广播聊天室信息
     */
    void broadcastChatMessage(Long gameId, WebSocketMessage message);

    /**
     * 2. 通知玩家剧本生成成功
     */
    void notifyGamePlayerScriptReady(Long gamePlayerId, Long scriptId);

    /**
     * 3. 通知玩家开始搜证
     */
    void notifyPlayerStartInvestigation(Long gamePlayerId, Map<String, Object> investigationScenes);

    /**
     * 4. 向指定gameId的所有真人GamePlayer广播公开线索
     */
    void broadcastPublicClues(Long gameId, List<?> clues);

    /**
     * 5. 向指定gameId的所有真人GamePlayer广播最终投票结果
     */
    void broadcastVoteResult(Long gameId, Long murdererId, Map<Long, Long> voteDetails, Integer dmScore);

    /**
     * 6. 向指定gameId的游戏前端,在工作流节点结束后,广播同住阶段的变化
     */
    void broadcastPhaseChange(Long gameId, GamePhase newPhase);

    /**
     * 7. 处理投票逻辑
     */
    void handleVote(Long gameId, WebSocketMessage message);
}

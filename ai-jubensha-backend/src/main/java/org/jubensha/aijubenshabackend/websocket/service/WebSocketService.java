package org.jubensha.aijubenshabackend.websocket.service;

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
}

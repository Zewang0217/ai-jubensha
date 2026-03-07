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
     * 广播阶段变化通知（包含详细信息）
     *
     * @param gameId 游戏ID
     * @param previousPhase 上一阶段
     * @param newPhase 新阶段
     * @param message 提示消息
     */
    void broadcastPhaseChange(Long gameId, GamePhase previousPhase, GamePhase newPhase, String message);

    /**
     * 7. 处理投票逻辑
     */
    void handleVote(Long gameId, WebSocketMessage message);

    /**
     * 广播阶段就绪状态
     *
     * @param gameId 游戏ID
     * @param nodeName 节点名称
     * @param isReady 是否就绪
     * @param message 提示信息
     */
    void broadcastPhaseReady(Long gameId, String nodeName, Boolean isReady, String message);

    /**
     * 广播搜证完成通知
     *
     * @param gameId 游戏ID
     * @param message 提示信息
     */
    void broadcastInvestigationComplete(Long gameId, String message);

    /**
     * 广播游戏结束通知
     *
     * @param gameId 游戏ID
     * @param message 提示信息
     */
    void broadcastGameEnded(Long gameId, String message);

    /**
     * 广播 AI Agent 操作消息（公屏显示）
     *
     * @param gameId     游戏ID
     * @param actionType 操作类型（INVESTIGATE, REVEAL_CLUE, HIDE_CLUE, SPEAK, VOTE, SYSTEM）
     * @param agentName  AI Agent 角色名
     * @param targetName 目标名称（线索名、场景名等）
     * @param message    显示消息内容
     * @param isPublic   是否公开操作
     */
    void broadcastAgentAction(Long gameId, String actionType, String agentName, String targetName, String message, Boolean isPublic);
}

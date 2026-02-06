package org.jubensha.aijubenshabackend.ai.service.util;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.tools.*;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AI讨论辅助工具
 * 为AI玩家提供讨论相关的辅助功能，不再自动生成消息，而是由AI自主决策
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class DiscussionHelper {

    @Autowired
    private RAGService ragService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GetDiscussionHistoryTool getDiscussionHistoryTool;

    @Autowired
    private GetTimelineTool getTimelineTool;

    @Autowired
    private GetClueTool getClueTool;

    @Autowired
    private GetSecretTool getSecretTool;

    @Autowired
    private SendDiscussionMessageTool sendDiscussionMessageTool;

    @Autowired
    private SendPrivateChatRequestTool sendPrivateChatRequestTool;

    @Autowired
    private GetPlayerStatusTool getPlayerStatusTool;

    @Autowired
    private TurnManager turnManager;

    /**
     * 检查是否可以发起单聊
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 是否可以发起单聊
     */
    public boolean canRequestPrivateChat(Long gameId, Long playerId) {
        // 检查单聊次数
        int chatCount = turnManager.getPrivateChatCount(gameId, playerId);
        if (chatCount >= 2) {
            log.debug("玩家单聊次数已达上限，玩家ID: {}", playerId);
            return false;
        }

        // 检查是否在自由讨论阶段
        String currentPhase = turnManager.getCurrentPhase(gameId);
        if (!currentPhase.equals(TurnManager.PHASE_FREE_DISCUSSION)) {
            log.debug("只能在自由讨论阶段发起单聊，当前阶段: {}", currentPhase);
            return false;
        }

        return true;
    }

    /**
     * 获取当前游戏阶段
     *
     * @param gameId 游戏ID
     * @return 当前阶段
     */
    public String getCurrentPhase(Long gameId) {
        return turnManager.getCurrentPhase(gameId);
    }

    /**
     * 获取玩家单聊次数
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 单聊次数
     */
    public int getPrivateChatCount(Long gameId, Long playerId) {
        return turnManager.getPrivateChatCount(gameId, playerId);
    }

    /**
     * 获取玩家名称
     *
     * @param playerId 玩家ID
     * @return 玩家名称
     */
    public String getPlayerName(Long playerId) {
        Optional<Player> playerOpt = playerService.getPlayerById(playerId);
        Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
        return player.getNickname();
    }

    /**
     * 辅助方法：构建单聊邀请消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @return 邀请消息
     */
    public String buildPrivateChatMessage(Long senderId, Long receiverId) {
        String senderName = getPlayerName(senderId);
        String receiverName = getPlayerName(receiverId);
        return String.format("你好%s，我是%s，关于刚才讨论的内容，我有些事情想单独和你聊聊，可以吗？", receiverName, senderName);
    }
}

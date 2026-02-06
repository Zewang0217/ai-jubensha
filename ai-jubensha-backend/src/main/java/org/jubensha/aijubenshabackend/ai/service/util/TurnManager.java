package org.jubensha.aijubenshabackend.ai.service.util;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轮流发言管理器
 * 用于管理讨论环节的发言顺序和时间控制
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class TurnManager {

    @Autowired
    private TimerService timerService;

    // 游戏发言状态，key: gameId, value: 发言状态
    private final Map<Long, GameTurnState> gameTurnStates = new ConcurrentHashMap<>();

    // 发言阶段常量
    public static final String PHASE_STATEMENT = "STATEMENT";
    public static final String PHASE_FREE_DISCUSSION = "FREE_DISCUSSION";
    public static final String PHASE_PRIVATE_CHAT = "PRIVATE_CHAT";
    public static final String PHASE_ANSWER = "ANSWER";

    /**
     * 初始化游戏发言状态
     *
     * @param gameId    游戏ID
     * @param playerIds 玩家ID列表
     * @param dmId      DM ID
     */
    public void initializeGameTurn(Long gameId, List<Long> playerIds, Long dmId) {
        GameTurnState state = new GameTurnState();
        state.setGameId(gameId);
        state.setPlayerIds(new ArrayList<>(playerIds));
        state.setDmId(dmId);
        state.setCurrentPhase(PHASE_STATEMENT);
        state.setCurrentTurnIndex(0);
        state.setPrivateChatCounts(new ConcurrentHashMap<>());

        // 初始化单聊次数
        for (Long playerId : playerIds) {
            state.getPrivateChatCounts().put(playerId, 0);
        }

        gameTurnStates.put(gameId, state);
        log.info("初始化游戏发言状态，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());
    }

    /**
     * 获取当前发言玩家
     *
     * @param gameId 游戏ID
     * @return 当前发言玩家ID
     */
    public Long getCurrentSpeaker(Long gameId) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return null;
        }

        if (state.getCurrentPhase().equals(PHASE_STATEMENT)) {
            List<Long> playerIds = state.getPlayerIds();
            int index = state.getCurrentTurnIndex();
            if (index < playerIds.size()) {
                return playerIds.get(index);
            }
        }

        return null;
    }

    /**
     * 开始当前玩家发言
     *
     * @param gameId    游戏ID
     * @param duration  发言时间（秒）
     * @param callback  超时回调
     */
    public void startCurrentTurn(Long gameId, long duration, Runnable callback) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return;
        }

        Long currentSpeaker = getCurrentSpeaker(gameId);
        if (currentSpeaker != null) {
            log.info("开始玩家发言，游戏ID: {}, 玩家ID: {}, 时间: {}秒", gameId, currentSpeaker, duration);
            timerService.startTimer("TURN_" + gameId + "_" + currentSpeaker, duration, callback);
        }
    }

    /**
     * 结束当前玩家发言
     *
     * @param gameId 游戏ID
     * @return 下一个发言玩家ID，如果没有则返回null
     */
    public Long endCurrentTurn(Long gameId) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return null;
        }

        if (state.getCurrentPhase().equals(PHASE_STATEMENT)) {
            int currentIndex = state.getCurrentTurnIndex();
            List<Long> playerIds = state.getPlayerIds();
            
            // 取消当前计时器
            Long currentSpeaker = getCurrentSpeaker(gameId);
            if (currentSpeaker != null) {
                timerService.cancelTimer("TURN_" + gameId + "_" + currentSpeaker);
            }

            // 移动到下一个玩家
            int nextIndex = currentIndex + 1;
            state.setCurrentTurnIndex(nextIndex);

            if (nextIndex < playerIds.size()) {
                return playerIds.get(nextIndex);
            } else {
                // 陈述阶段结束，进入自由讨论
                state.setCurrentPhase(PHASE_FREE_DISCUSSION);
                log.info("陈述阶段结束，游戏ID: {}", gameId);
                return null;
            }
        }

        return null;
    }

    /**
     * 切换到下一个阶段
     *
     * @param gameId 游戏ID
     * @param phase  下一阶段
     */
    public void switchPhase(Long gameId, String phase) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return;
        }

        state.setCurrentPhase(phase);
        log.info("切换讨论阶段，游戏ID: {}, 阶段: {}", gameId, phase);

        // 重置发言顺序（如果需要）
        if (phase.equals(PHASE_STATEMENT)) {
            state.setCurrentTurnIndex(0);
        }
    }

    /**
     * 发起单聊请求
     *
     * @param gameId     游戏ID
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @return 是否允许发起单聊
     */
    public boolean requestPrivateChat(Long gameId, Long senderId, Long receiverId) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return false;
        }

        // 检查是否在自由讨论阶段
        if (!state.getCurrentPhase().equals(PHASE_FREE_DISCUSSION)) {
            log.warn("只能在自由讨论阶段发起单聊，当前阶段: {}", state.getCurrentPhase());
            return false;
        }

        // 检查单聊次数
        Map<Long, Integer> privateChatCounts = state.getPrivateChatCounts();
        int count = privateChatCounts.getOrDefault(senderId, 0);
        if (count >= 2) {
            log.warn("玩家单聊次数已达上限，玩家ID: {}", senderId);
            return false;
        }

        // 增加单聊次数
        privateChatCounts.put(senderId, count + 1);
        log.info("发起单聊请求，发送者: {}, 接收者: {}, 剩余次数: {}", senderId, receiverId, 2 - (count + 1));

        return true;
    }

    /**
     * 获取当前阶段
     *
     * @param gameId 游戏ID
     * @return 当前阶段
     */
    public String getCurrentPhase(Long gameId) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return null;
        }

        return state.getCurrentPhase();
    }

    /**
     * 获取单聊次数
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 单聊次数
     */
    public int getPrivateChatCount(Long gameId, Long playerId) {
        GameTurnState state = gameTurnStates.get(gameId);
        if (state == null) {
            log.warn("游戏发言状态不存在，游戏ID: {}", gameId);
            return 0;
        }

        return state.getPrivateChatCounts().getOrDefault(playerId, 0);
    }

    /**
     * 清理游戏发言状态
     *
     * @param gameId 游戏ID
     */
    public void cleanup(Long gameId) {
        gameTurnStates.remove(gameId);
        log.info("清理游戏发言状态，游戏ID: {}", gameId);
    }

    /**
     * 游戏发言状态
     */
    private static class GameTurnState {
        private Long gameId;
        private List<Long> playerIds;
        private Long dmId;
        private String currentPhase;
        private int currentTurnIndex;
        private Map<Long, Integer> privateChatCounts;

        // getter and setter methods
        public Long getGameId() {
            return gameId;
        }

        public void setGameId(Long gameId) {
            this.gameId = gameId;
        }

        public List<Long> getPlayerIds() {
            return playerIds;
        }

        public void setPlayerIds(List<Long> playerIds) {
            this.playerIds = playerIds;
        }

        public Long getDmId() {
            return dmId;
        }

        public void setDmId(Long dmId) {
            this.dmId = dmId;
        }

        public String getCurrentPhase() {
            return currentPhase;
        }

        public void setCurrentPhase(String currentPhase) {
            this.currentPhase = currentPhase;
        }

        public int getCurrentTurnIndex() {
            return currentTurnIndex;
        }

        public void setCurrentTurnIndex(int currentTurnIndex) {
            this.currentTurnIndex = currentTurnIndex;
        }

        public Map<Long, Integer> getPrivateChatCounts() {
            return privateChatCounts;
        }

        public void setPrivateChatCounts(Map<Long, Integer> privateChatCounts) {
            this.privateChatCounts = privateChatCounts;
        }
    }
}

package org.jubensha.aijubenshabackend.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理客户端连接session与Real GamePlayerId的对应关系
 */
@Slf4j
@Component
public class PlayerSessionManager {

    /**
     * sessionId -> gamePlayerId 映射
     */
    private final Map<String, Long> sessionGamePlayerMap = new ConcurrentHashMap<>();

    /**
     * gamePlayerId -> sessionId 映射（反向查找）
     */
    private final Map<Long, String> gamePlayerSessionMap = new ConcurrentHashMap<>();

    /**
     * 注册session与GamePlayerId的关联
     *
     * @param sessionId    WebSocket Session ID
     * @param gamePlayerId 游戏玩家ID（观察者模式下为null）
     */
    public void register(String sessionId, Long gamePlayerId) {
        // 观察者模式下 gamePlayerId 为 null，不进行注册
        if (gamePlayerId == null) {
            log.info("观察者模式连接，不注册 session: sessionId={}", sessionId);
            return;
        }
        
        // 如果该gamePlayerId已有session，先移除
        String oldSession = gamePlayerSessionMap.get(gamePlayerId);
        if (oldSession != null && !oldSession.equals(sessionId)) {
            sessionGamePlayerMap.remove(oldSession);
            log.info("GamePlayerId {} 的旧session {} 已被替换", gamePlayerId, oldSession);
        }

        sessionGamePlayerMap.put(sessionId, gamePlayerId);
        gamePlayerSessionMap.put(gamePlayerId, sessionId);
        log.info("注册session: sessionId={}, gamePlayerId={}", sessionId, gamePlayerId);
    }

    /**
     * 根据sessionId获取GamePlayerId
     */
    public Long getGamePlayerId(String sessionId) {
        return sessionGamePlayerMap.get(sessionId);
    }

    /**
     * 根据GamePlayerId获取sessionId
     */
    public String getSessionId(Long gamePlayerId) {
        return gamePlayerSessionMap.get(gamePlayerId);
    }

    /**
     * 移除session关联
     */
    public void removeBySessionId(String sessionId) {
        Long gamePlayerId = sessionGamePlayerMap.remove(sessionId);
        if (gamePlayerId != null) {
            gamePlayerSessionMap.remove(gamePlayerId);
            log.info("移除session: sessionId={}, gamePlayerId={}", sessionId, gamePlayerId);
        }
    }

    /**
     * 检查GamePlayerId是否已连接
     */
    public boolean isConnected(Long gamePlayerId) {
        return gamePlayerSessionMap.containsKey(gamePlayerId);
    }
}
package org.jubensha.aijubenshabackend.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.websocket.service.PlayerSessionManager;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 握手拦截器
 * 在连接建立时分配 gamePlayerId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameHandshakeInterceptor implements HandshakeInterceptor {

    private final GamePlayerService gamePlayerService;
    private final PlayerSessionManager sessionManager;

    /**
     * 记录每个游戏的下一个可分配索引
     */
    private final Map<Long, AtomicInteger> gameNextIndexMap = new ConcurrentHashMap<>();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 从 URL 参数获取 gameId
            String gameIdParam = servletRequest.getServletRequest().getParameter("gameId");
            if (gameIdParam == null) {
                log.error("连接被拒绝：缺少 gameId 参数");
                return false;
            }

            Long gameId;
            try {
                gameId = Long.parseLong(gameIdParam);
            } catch (NumberFormatException e) {
                log.error("连接被拒绝：无效的 gameId: {}", gameIdParam);
                return false;
            }

            // 获取游戏的真人玩家列表
            List<GamePlayer> realPlayers = gamePlayerService.getRealPlayersByGameId(gameId);
            if (realPlayers.isEmpty()) {
                log.error("连接被拒绝：游戏 {} 没有真人玩家", gameId);
                return false;
            }

            // 获取下一个可分配的索引
            AtomicInteger nextIndex = gameNextIndexMap.computeIfAbsent(gameId, k -> new AtomicInteger(0));
            int index = nextIndex.getAndIncrement();

            // 检查是否还有剩余的 Real GamePlayer
            if (index >= realPlayers.size()) {
                log.error("连接被拒绝：游戏 {} 的真人玩家已满 ({}个)", gameId, realPlayers.size());
                return false;
            }

            // 分配 gamePlayerId
            GamePlayer assignedPlayer = realPlayers.get(index);
            Long gamePlayerId = assignedPlayer.getId();

            // 存储到 attributes，供后续使用
            attributes.put("gameId", gameId);
            attributes.put("gamePlayerId", gamePlayerId);

            log.info("连接分配成功: gameId={}, gamePlayerId={}, index={}/{}",
                    gameId, gamePlayerId, index + 1, realPlayers.size());
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理，不需要额外操作
    }
}

package org.jubensha.aijubenshabackend.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.websocket.service.PlayerSessionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * STOMP 通道拦截器
 * 在 CONNECT 时将 gamePlayerId 与 session 关联
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameChannelInterceptor implements ChannelInterceptor {

    private final PlayerSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从 session attributes 获取握手时存储的信息
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Long gamePlayerId = (Long) sessionAttributes.get("gamePlayerId");
                Boolean isObserver = (Boolean) sessionAttributes.get("isObserver");
                String sessionId = accessor.getSessionId();

                if (gamePlayerId != null && sessionId != null) {
                    // 注册 session 与 gamePlayerId 的关联
                    sessionManager.register(sessionId, gamePlayerId);
                    log.info("STOMP CONNECT: sessionId={} 关联 gamePlayerId={}", sessionId, gamePlayerId);
                } else if (Boolean.TRUE.equals(isObserver)) {
                    // 观察者模式连接
                    log.info("STOMP CONNECT: sessionId={} 以观察者模式连接", sessionId);
                }
            }
        }

        return message;
    }
}

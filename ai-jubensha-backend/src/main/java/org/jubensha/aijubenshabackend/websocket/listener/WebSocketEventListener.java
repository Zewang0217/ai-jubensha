package org.jubensha.aijubenshabackend.websocket.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * Listener 预留
 *
 * @author luobo
 * @date 2026/2/28
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public WebSocketEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.info("收到了 sessionID 为 {} 的游戏连接事件", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // 从 session 属性中获取用户信息
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            String username = (String) sessionAttrs.get("username");
            String roomId = (String) sessionAttrs.get("roomId");

            if (username != null && roomId != null) {
                log.info("用户 {} 断开连接, 从房间 {} 移除", username, roomId);

                // 广播用户离开消息
                Map<String, Object> leaveMessage = Map.of(
                        "type", "LEAVE",
                        "sender", username,
                        "content", username + " 离开了房间",
                        "roomId", roomId
                );

                simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
            }
        }
    }
}

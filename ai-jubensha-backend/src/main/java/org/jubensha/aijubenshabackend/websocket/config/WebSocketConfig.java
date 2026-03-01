package org.jubensha.aijubenshabackend.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author zewang
 * @author luobo
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置消息代理，用于广播消息
        // "/topic" 用于广播消息，"/queue" 用于点对点消息
        config.enableSimpleBroker("/topic", "/queue");

        // 配置应用前缀，用于处理客户端发送的消息
        // 例如：@MessageMapping("/hello") 对应客户端发送路径 "/app/hello"
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许客户端连接
        registry.addEndpoint("/ws")
                .withSockJS()
                .setHeartbeatTime(25000);
    }
}
package org.jubensha.aijubenshabackend.websocket.config;

import lombok.RequiredArgsConstructor;
import org.jubensha.aijubenshabackend.websocket.interceptor.GameChannelInterceptor;
import org.jubensha.aijubenshabackend.websocket.interceptor.GameHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
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
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final GameHandshakeInterceptor handshakeInterceptor;
    private final GameChannelInterceptor channelInterceptor;

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
                .addInterceptors(handshakeInterceptor)
                .withSockJS()
                .setHeartbeatTime(25000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 添加通道拦截器，处理 CONNECT 事件
        registration.interceptors(channelInterceptor);
    }
}
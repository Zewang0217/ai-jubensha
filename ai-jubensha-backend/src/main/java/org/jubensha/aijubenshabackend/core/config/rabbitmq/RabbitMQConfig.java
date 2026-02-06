package org.jubensha.aijubenshabackend.core.config.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ核心配置类
 * 管理交换机、队列、绑定关系等核心配置
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    // 交换机名称常量
    public static final String DISCUSSION_EXCHANGE = "discussion.exchange";
    public static final String PRIVATE_EXCHANGE = "private.exchange";
    public static final String SYSTEM_EXCHANGE = "system.exchange";

    // 队列名称常量
    public static final String DISCUSSION_QUEUE_ALL = "discussion.queue.all";
    public static final String DISCUSSION_QUEUE_PREFIX = "discussion.queue.";
    public static final String PRIVATE_QUEUE_PREFIX = "private.queue.";
    public static final String SYSTEM_QUEUE_DM = "system.queue.dm";
    public static final String SYSTEM_QUEUE_ALL = "system.queue.all";
    public static final String SYSTEM_QUEUE_PLAYER_PREFIX = "system.queue.player.";

    // 路由键常量
    public static final String DISCUSSION_ROUTING_KEY_ALL = "discussion.all";
    public static final String DISCUSSION_ROUTING_KEY_PREFIX = "discussion.";
    public static final String PRIVATE_ROUTING_KEY_PREFIX = "private.";
    public static final String SYSTEM_ROUTING_KEY_DM = "system.dm";
    public static final String SYSTEM_ROUTING_KEY_ALL = "system.all";
    public static final String SYSTEM_ROUTING_KEY_PLAYER_PREFIX = "system.player.";

    // ==================== 交换机声明 ====================

    /**
     * 声明讨论交换机
     */
    @Bean
    public TopicExchange discussionExchange() {
        return ExchangeBuilder
                .topicExchange(DISCUSSION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 声明私信交换机
     */
    @Bean
    public TopicExchange privateExchange() {
        return ExchangeBuilder
                .topicExchange(PRIVATE_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 声明系统交换机
     */
    @Bean
    public TopicExchange systemExchange() {
        return ExchangeBuilder
                .topicExchange(SYSTEM_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 队列声明 ====================

    /**
     * 声明讨论通用队列（所有玩家都能收到）
     */
    @Bean
    public Queue discussionQueueAll() {
        return QueueBuilder
                .durable(DISCUSSION_QUEUE_ALL)
                .build();
    }

    /**
     * 声明系统DM队列（仅DM能收到）
     */
    @Bean
    public Queue systemQueueDm() {
        return QueueBuilder
                .durable(SYSTEM_QUEUE_DM)
                .build();
    }

    /**
     * 声明系统通用队列（所有玩家都能收到）
     */
    @Bean
    public Queue systemQueueAll() {
        return QueueBuilder
                .durable(SYSTEM_QUEUE_ALL)
                .build();
    }

    // ==================== 绑定关系 ====================

    /**
     * 绑定讨论交换机与通用队列
     */
    @Bean
    public Binding bindingDiscussionAll(TopicExchange discussionExchange, Queue discussionQueueAll) {
        return BindingBuilder
                .bind(discussionQueueAll)
                .to(discussionExchange)
                .with(DISCUSSION_ROUTING_KEY_ALL);
    }

    /**
     * 绑定系统交换机与DM队列
     */
    @Bean
    public Binding bindingSystemDm(TopicExchange systemExchange, Queue systemQueueDm) {
        return BindingBuilder
                .bind(systemQueueDm)
                .to(systemExchange)
                .with(SYSTEM_ROUTING_KEY_DM);
    }

    /**
     * 绑定系统交换机与通用队列
     */
    @Bean
    public Binding bindingSystemAll(TopicExchange systemExchange, Queue systemQueueAll) {
        return BindingBuilder
                .bind(systemQueueAll)
                .to(systemExchange)
                .with(SYSTEM_ROUTING_KEY_ALL);
    }

    // ==================== 消息转换器 ====================

    /**
     * 配置JSON消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== RabbitTemplate配置 ====================

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        // 设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息发送成功
                log.info("消息发送成功:{}", correlationData);
            } else {
                // 消息发送失败，可进行重试或日志记录
                log.error("消息发送失败:{}", cause);
            }
        });
        // 设置返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            // 消息无法路由时的处理
        });
        return rabbitTemplate;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取讨论队列名称
     */
    public static String getDiscussionQueueName(Long playerId) {
        return DISCUSSION_QUEUE_PREFIX + playerId;
    }

    /**
     * 获取私信队列名称
     */
    public static String getPrivateQueueName(Long playerId) {
        return PRIVATE_QUEUE_PREFIX + playerId;
    }

    /**
     * 获取系统玩家队列名称
     */
    public static String getSystemPlayerQueueName(Long playerId) {
        return SYSTEM_QUEUE_PLAYER_PREFIX + playerId;
    }

    /**
     * 获取讨论路由键
     */
    public static String getDiscussionRoutingKey(Long playerId) {
        return DISCUSSION_ROUTING_KEY_PREFIX + playerId;
    }

    /**
     * 获取私信路由键
     */
    public static String getPrivateRoutingKey(Long playerId) {
        return PRIVATE_ROUTING_KEY_PREFIX + playerId;
    }

    /**
     * 获取系统玩家路由键
     */
    public static String getSystemPlayerRoutingKey(Long playerId) {
        return SYSTEM_ROUTING_KEY_PLAYER_PREFIX + playerId;
    }
}
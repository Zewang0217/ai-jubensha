package org.jubensha.aijubenshabackend.core.config.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * RabbitMQ 关闭监听器
 * 在应用关闭时清理队列中的待处理消息
 *
 * @author zewang
 * @date 2026-03-01
 */
@Slf4j
@Component
public class RabbitMQShutdownListener implements ApplicationListener<ContextClosedEvent> {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 队列名称
    private static final String INVESTIGATION_QUEUE = "investigation.queue";

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            log.info("应用关闭，开始清理RabbitMQ队列中的待处理消息");
            
            // 清理搜证队列中的待处理消息
            clearInvestigationQueue();
            
            log.info("RabbitMQ队列清理完成");
        } catch (Exception e) {
            log.error("清理RabbitMQ队列失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理搜证队列中的待处理消息
     */
    private void clearInvestigationQueue() {
        try {
            // 使用RabbitTemplate的execute方法来清空队列
            rabbitTemplate.execute(channel -> {
                // 声明队列（如果不存在）
                channel.queueDeclare(INVESTIGATION_QUEUE, true, false, false, null);
                
                // 清空队列
                com.rabbitmq.client.AMQP.Queue.PurgeOk purgeOk = channel.queuePurge(INVESTIGATION_QUEUE);
                int messageCount = purgeOk.getMessageCount();
                log.info("已清空搜证队列，共清理 {} 条消息", messageCount);
                
                return null;
            });
        } catch (Exception e) {
            log.error("清理搜证队列失败: {}", e.getMessage(), e);
        }
    }
}

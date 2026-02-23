package org.jubensha.aijubenshabackend.ai.service.agent;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜证 Agent 处理器
 * 用于监听消息队列中的搜证通知，并处理 AI 玩家的搜证逻辑
 *
 * @author luobo
 * @date 2026-02-22
 */
@Slf4j
@Component
public class InvestigationAgentHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private PlayerAgent playerAgent;

    private GameService gameService;
    private GamePlayerService gamePlayerService;
    private InvestigationService investigationService;
    private SceneService sceneService;

    @Autowired
    public InvestigationAgentHandler(ChatModel chatModel) {
        this.playerAgent = AiServices.builder(PlayerAgent.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 监听 AI 玩家搜证通知
     *
     * @param message 消息内容
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "investigation.queue", durable = "true"),
                    exchange = @Exchange(name = "system.exchange", type = "topic"),
                    key = "system.investigation.*"
            )
    )
    public void handleInvestigationNotification(Map<String, Object> message) {
        try {
            log.info("接收到AI玩家搜证通知: {}", message);

            // 解析消息内容
            Long gameId = ((Number) message.get("gameId")).longValue();
            Long playerId = ((Number) message.get("playerId")).longValue();
            List<Long> sceneIds = objectMapper.convertValue(message.get("sceneIds"), new TypeReference<List<Long>>() {});
            Integer maxChances = ((Number) message.get("maxChances")).intValue();

            log.info("处理AI玩家搜证: 游戏ID={}, 玩家ID={}, 场景数量={}, 最大搜证次数={}",
                    gameId, playerId, sceneIds.size(), maxChances);

            // 延迟初始化服务
            lazyInitServices();

            // 获取玩家角色信息
            String characterName = getPlayerCharacterName(gameId, playerId);
            if (characterName == null) {
                log.warn("无法获取玩家 {} 的角色信息", playerId);
                return;
            }

            // 调用 PlayerAgent 处理搜证逻辑
            List<String> sceneIdStrings = sceneIds.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            
            String result = playerAgent.investigate(
                    gameId.toString(),
                    playerId.toString(),
                    characterName,
                    sceneIdStrings,
                    maxChances
            );

            log.info("AI玩家搜证完成: 结果={}", result);

        } catch (Exception e) {
            log.error("处理AI玩家搜证通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 延迟初始化服务
     */
    private void lazyInitServices() {
        if (gameService == null) {
            gameService = SpringContextUtil.getBean(GameService.class);
            gamePlayerService = SpringContextUtil.getBean(GamePlayerService.class);
            investigationService = SpringContextUtil.getBean(InvestigationService.class);
            sceneService = SpringContextUtil.getBean(SceneService.class);
        }
    }

    /**
     * 获取玩家的角色名称
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 角色名称
     */
    private String getPlayerCharacterName(Long gameId, Long playerId) {
        try {
            // 这里需要根据实际的服务实现来获取玩家角色名称
            // 暂时返回一个默认值
            return "AI玩家" + playerId;
        } catch (Exception e) {
            log.warn("获取玩家角色名称失败: {}", e.getMessage());
            return null;
        }
    }
}

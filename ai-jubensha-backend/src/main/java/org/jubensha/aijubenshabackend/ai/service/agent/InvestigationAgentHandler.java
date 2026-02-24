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
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.service.util.ResponseUtils;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.jubensha.aijubenshabackend.memory.MemoryService;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜证 Agent 处理器
 * 用于监听消息队列中的搜证通知，并处理 AI 玩家的搜证逻辑
 *
 * @author zewang
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
    private ClueService clueService;
    private MemoryService memoryService;
    private RAGService ragService;

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
            List<Map<String, Object>> clueOptions = objectMapper.convertValue(message.get("clueOptions"), new TypeReference<List<Map<String, Object>>>() {});
            Integer maxChances = ((Number) message.get("maxChances")).intValue();

            log.info("处理AI玩家搜证: 游戏ID={}, 玩家ID={}, 线索选项数量={}, 最大搜证次数={}",
                    gameId, playerId, clueOptions.size(), maxChances);

            // 延迟初始化服务
            lazyInitServices();

            // 获取玩家角色信息
            String characterName = getPlayerCharacterName(gameId, playerId);
            if (characterName == null) {
                log.warn("无法获取玩家 {} 的角色信息", playerId);
                return;
            }

            // 准备线索选项字符串列表，格式为 "clueId: clueName"
            List<String> clueOptionStrings = clueOptions.stream()
                    .map(clue -> clue.get("clueId") + ": " + clue.get("clueName"))
                    .collect(Collectors.toList());
            
            String result = playerAgent.investigate(
                    gameId.toString(),
                    playerId.toString(),
                    characterName,
                    clueOptionStrings,
                    maxChances
            );

            log.info("AI玩家搜证完成: 结果={}", result);

            // 解析JSON格式的搜证请求
            try {
                // 使用ResponseUtils工具类解析AI返回的JSON，支持Markdown代码块等各种格式
                com.fasterxml.jackson.databind.JsonNode resultJson = ResponseUtils.extractJson(result);
                Map<String, Object> investigationResult = objectMapper.convertValue(resultJson, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> requests = objectMapper.convertValue(
                        investigationResult.get("investigationRequests"), 
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                // 执行搜证操作
                    for (Map<String, Object> request : requests) {
                        String clueIdStr = (String) request.get("clueId"); // AI返回的clueId

                        if (clueIdStr == null) {
                            log.warn("搜证请求格式错误，缺少必要字段: {}", request);
                            continue;
                        }

                        try {
                            Long clueId = parseClueId(clueIdStr);
                            log.info("解析AI搜证结果: 线索ID={}", clueId);

                            // 直接查询线索信息
                            java.util.Optional<org.jubensha.aijubenshabackend.models.entity.Clue> clueOptional = clueService.getClueById(clueId);
                            if (clueOptional.isPresent()) {
                                org.jubensha.aijubenshabackend.models.entity.Clue clue = clueOptional.get();
                                String clueContent = clue.getName() + ": " + clue.getDescription();
                                
                                log.info("AI玩家搜证成功: 线索ID={}, 线索名称={}", clueId, clue.getName());
                                
                                // 将线索内容返回给 AI 玩家，让其决定是否公开
                                String revealDecision = playerAgent.decideToReveal(
                                        gameId.toString(),
                                        playerId.toString(),
                                        characterName,
                                        clueId.toString(),
                                        clueContent
                                );
                                
                                log.info("AI玩家对线索 {} 的公开决定: {}", clueId, revealDecision);
                                
                                // 解析决定结果并返回给系统
                                try {
                                    // 使用ResponseUtils工具类解析AI返回的JSON，支持Markdown代码块等各种格式
                                    com.fasterxml.jackson.databind.JsonNode decisionJson = ResponseUtils.extractJson(revealDecision);
                                    Map<String, Object> decisionResult = objectMapper.convertValue(decisionJson, new TypeReference<Map<String, Object>>() {});
                                    String decision = (String) decisionResult.get("decision");
                                    String reason = (String) decisionResult.get("reason");
                                    String analysis = (String) decisionResult.get("analysis");
                                    
                                    log.info("解析决定结果: 决策={}, 理由={}, 分析={}", decision, reason, analysis);
                                    
                                    // 根据决策结果执行相应操作
                                    if ("公开".equals(decision)) {
                                        // 公开线索的逻辑
                                        log.info("AI玩家选择公开线索: {}", clueId);
                                        // 修改线索可见性为PUBLIC
                                        clue.setVisibility(ClueVisibility.PUBLIC);
                                        clueService.updateClue(clueId, clue);
                                        log.info("线索 {} 可见性已修改为 PUBLIC", clueId);
                                        // 将线索存储到向量数据库，playerid设置为0
                                        memoryService.storeClueMemory(gameId, 0L, clueId, clueContent, "AI玩家" + playerId);
                                        log.info("线索 {} 已存储到向量数据库，playerid=0", clueId);
                                    } else {
                                        // 不公开线索的逻辑
                                        log.info("AI玩家选择不公开线索: {}", clueId);
                                        // 修改线索可见性为PRIVATE
                                        clue.setVisibility(ClueVisibility.PRIVATE);
                                        clueService.updateClue(clueId, clue);
                                        log.info("线索 {} 可见性已修改为 PRIVATE", clueId);
                                        // 将线索存储到向量数据库，playerid设置为发现玩家的id
                                        memoryService.storeClueMemory(gameId, playerId, clueId, clueContent, "AI玩家" + playerId);
                                        log.info("线索 {} 已存储到向量数据库，playerid={}", clueId, playerId);
                                    }
                                } catch (Exception e) {
                                    log.error("解析决定结果失败: {}", e.getMessage(), e);
                                }
                            } else {
                                log.warn("线索ID {} 不存在", clueId);
                            }

                    } catch (NumberFormatException e) {
                        log.warn("搜证请求中ID格式错误: 线索ID={}", clueIdStr);
                    } catch (Exception e) {
                        log.error("执行搜证操作失败: {}", e.getMessage(), e);
                    }
                }

            } catch (Exception e) {
                log.error("解析AI玩家搜证请求失败: {}", e.getMessage(), e);
            }

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
            clueService = SpringContextUtil.getBean(ClueService.class);
            memoryService = SpringContextUtil.getBean(MemoryService.class);
            ragService = SpringContextUtil.getBean(RAGService.class);
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

    /**
     * 解析线索ID
     * 支持处理 "clue_xxx_yy" 格式的线索ID，提取纯数字部分
     *
     * @param clueIdStr 线索ID字符串
     * @return 解析后的线索ID
     * @throws NumberFormatException 如果ID格式错误
     */
    private Long parseClueId(String clueIdStr) {
        if (clueIdStr == null) {
            throw new NumberFormatException("线索ID不能为空");
        }
        
        // 检查是否为 "clue_xxx_yy" 格式
        if (clueIdStr.startsWith("clue_")) {
            // 提取数字部分
            String numericPart = clueIdStr.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                throw new NumberFormatException("线索ID格式错误，无法提取数字部分");
            }
            return Long.parseLong(numericPart);
        }
        
        // 否则尝试直接解析为长整型
        return Long.parseLong(clueIdStr);
    }
}

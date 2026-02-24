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
            InvestigationMessageData messageData = parseInvestigationMessage(message);
            if (messageData == null) {
                return;
            }

            // 延迟初始化服务
            lazyInitServices();

            // 获取玩家角色信息
            String characterName = getPlayerCharacterName(messageData.getGameId(), messageData.getPlayerId());
            if (characterName == null) {
                log.warn("无法获取玩家 {} 的角色信息", messageData.getPlayerId());
                return;
            }

            // 准备线索选项字符串列表，格式为 "clueId: clueName"
            List<String> clueOptionStrings = prepareClueOptionStrings(messageData.getClueOptions());
            
            // 执行AI玩家搜证
            String result = executeInvestigation(messageData, characterName, clueOptionStrings);
            if (result == null) {
                return;
            }

            // 处理搜证结果
            processInvestigationResult(messageData, characterName, result);

        } catch (Exception e) {
            log.error("处理AI玩家搜证通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析搜证通知消息
     *
     * @param message 消息内容
     * @return 解析后的消息数据
     */
    private InvestigationMessageData parseInvestigationMessage(Map<String, Object> message) {
        try {
            Long gameId = ((Number) message.get("gameId")).longValue();
            Long playerId = ((Number) message.get("playerId")).longValue();
            List<Map<String, Object>> clueOptions = objectMapper.convertValue(message.get("clueOptions"), new TypeReference<List<Map<String, Object>>>() {});
            Integer maxChances = ((Number) message.get("maxChances")).intValue();

            log.info("处理AI玩家搜证: 游戏ID={}, 玩家ID={}, 线索选项数量={}, 最大搜证次数={}",
                    gameId, playerId, clueOptions.size(), maxChances);

            return new InvestigationMessageData(gameId, playerId, clueOptions, maxChances);
        } catch (Exception e) {
            log.error("解析消息内容失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 准备线索选项字符串列表
     *
     * @param clueOptions 线索选项列表
     * @return 格式化后的线索选项字符串列表
     */
    private List<String> prepareClueOptionStrings(List<Map<String, Object>> clueOptions) {
        return clueOptions.stream()
                .map(clue -> clue.get("clueId") + ": " + clue.get("clueName"))
                .collect(Collectors.toList());
    }

    /**
     * 执行AI玩家搜证
     *
     * @param messageData 消息数据
     * @param characterName 角色名称
     * @param clueOptionStrings 线索选项字符串列表
     * @return 搜证结果
     */
    private String executeInvestigation(InvestigationMessageData messageData, String characterName, List<String> clueOptionStrings) {
        try {
            String result = playerAgent.investigate(
                    messageData.getGameId().toString(),
                    messageData.getPlayerId().toString(),
                    characterName,
                    clueOptionStrings,
                    messageData.getMaxChances()
            );

            log.info("AI玩家搜证完成: 结果={}", result);
            return result;
        } catch (Exception e) {
            log.error("执行AI玩家搜证失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 处理搜证结果
     *
     * @param messageData 消息数据
     * @param characterName 角色名称
     * @param result 搜证结果
     */
    private void processInvestigationResult(InvestigationMessageData messageData, String characterName, String result) {
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
                processSingleInvestigationRequest(messageData, characterName, request);
            }
        } catch (Exception e) {
            log.error("解析AI玩家搜证请求失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理单个搜证请求
     *
     * @param messageData 消息数据
     * @param characterName 角色名称
     * @param request 搜证请求
     */
    private void processSingleInvestigationRequest(InvestigationMessageData messageData, String characterName, Map<String, Object> request) {
        String clueIdStr = (String) request.get("clueId"); // AI返回的clueId

        if (clueIdStr == null) {
            log.warn("搜证请求格式错误，缺少必要字段: {}", request);
            return;
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
                
                // 处理线索公开决定
                processClueRevealDecision(messageData, characterName, clue, clueContent);
            } else {
                log.warn("线索ID {} 不存在", clueId);
            }

        } catch (NumberFormatException e) {
            log.warn("搜证请求中ID格式错误: 线索ID={}", clueIdStr);
        } catch (Exception e) {
            log.error("执行搜证操作失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理线索公开决定
     *
     * @param messageData 消息数据
     * @param characterName 角色名称
     * @param clue 线索信息
     * @param clueContent 线索内容
     */
    private void processClueRevealDecision(InvestigationMessageData messageData, String characterName, 
                                          org.jubensha.aijubenshabackend.models.entity.Clue clue, String clueContent) {
        try {
            // 将线索内容返回给 AI 玩家，让其决定是否公开
            String revealDecision = playerAgent.decideToReveal(
                    messageData.getGameId().toString(),
                    messageData.getPlayerId().toString(),
                    characterName,
                    clue.getId().toString(),
                    clueContent
            );
            
            log.info("AI玩家对线索 {} 的公开决定: {}", clue.getId(), revealDecision);
            
            // 解析决定结果并返回给系统
            parseAndExecuteRevealDecision(messageData, clue, clueContent, revealDecision);
        } catch (Exception e) {
            log.error("处理线索公开决定失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析并执行公开决定
     *
     * @param messageData 消息数据
     * @param clue 线索信息
     * @param clueContent 线索内容
     * @param revealDecision 公开决定
     */
    private void parseAndExecuteRevealDecision(InvestigationMessageData messageData, 
                                             org.jubensha.aijubenshabackend.models.entity.Clue clue, 
                                             String clueContent, String revealDecision) {
        try {
            // 使用ResponseUtils工具类解析AI返回的JSON，支持Markdown代码块等各种格式
            com.fasterxml.jackson.databind.JsonNode decisionJson = ResponseUtils.extractJson(revealDecision);
            Map<String, Object> decisionResult = objectMapper.convertValue(decisionJson, new TypeReference<Map<String, Object>>() {});
            String decision = (String) decisionResult.get("decision");
            String reason = (String) decisionResult.get("reason");
            String analysis = (String) decisionResult.get("analysis");
            
            log.info("解析决定结果: 决策={}, 理由={}, 分析={}", decision, reason, analysis);
            
            // 根据决策结果执行相应操作
            executeRevealDecision(messageData, clue, clueContent, decision);
            
            // 扣减搜证次数
            consumeInvestigationChance(messageData.getGameId(), messageData.getPlayerId());
        } catch (Exception e) {
            log.error("解析决定结果失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行公开决定
     *
     * @param messageData 消息数据
     * @param clue 线索信息
     * @param clueContent 线索内容
     * @param decision 决策结果
     */
    private void executeRevealDecision(InvestigationMessageData messageData, 
                                     org.jubensha.aijubenshabackend.models.entity.Clue clue, 
                                     String clueContent, String decision) {
        if ("公开".equals(decision)) {
            // 公开线索的逻辑
            handlePublicClue(messageData, clue, clueContent);
        } else {
            // 不公开线索的逻辑
            handlePrivateClue(messageData, clue, clueContent);
        }
    }

    /**
     * 处理公开线索
     *
     * @param messageData 消息数据
     * @param clue 线索信息
     * @param clueContent 线索内容
     */
    private void handlePublicClue(InvestigationMessageData messageData, 
                                org.jubensha.aijubenshabackend.models.entity.Clue clue, 
                                String clueContent) {
        log.info("AI玩家选择公开线索: {}", clue.getId());
        // 修改线索可见性为PUBLIC，并设置playerId为发现玩家
        clue.setVisibility(ClueVisibility.PUBLIC);
        clue.setPlayerId(messageData.getPlayerId());
        clueService.updateClue(clue.getId(), clue);
        log.info("线索 {} 可见性已修改为 PUBLIC，playerId={}", clue.getId(), messageData.getPlayerId());
        // 将线索存储到向量数据库，playerid设置为0
        memoryService.storeClueMemory(messageData.getGameId(), 0L, clue.getId(), clueContent, "AI玩家" + messageData.getPlayerId());
        log.info("线索 {} 已存储到向量数据库，playerid=0", clue.getId());
    }

    /**
     * 处理私有线索
     *
     * @param messageData 消息数据
     * @param clue 线索信息
     * @param clueContent 线索内容
     */
    private void handlePrivateClue(InvestigationMessageData messageData, 
                                 org.jubensha.aijubenshabackend.models.entity.Clue clue, 
                                 String clueContent) {
        log.info("AI玩家选择不公开线索: {}", clue.getId());
        // 修改线索可见性为PRIVATE，并设置playerId为发现玩家
        clue.setVisibility(ClueVisibility.PRIVATE);
        clue.setPlayerId(messageData.getPlayerId());
        clueService.updateClue(clue.getId(), clue);
        log.info("线索 {} 可见性已修改为 PRIVATE，playerId={}", clue.getId(), messageData.getPlayerId());
        // 将线索存储到向量数据库，playerid设置为发现玩家的id
        memoryService.storeClueMemory(messageData.getGameId(), messageData.getPlayerId(), clue.getId(), clueContent, "AI玩家" + messageData.getPlayerId());
        log.info("线索 {} 已存储到向量数据库，playerid={}", clue.getId(), messageData.getPlayerId());
    }

    /**
     * 扣减搜证次数
     *
     * @param gameId 游戏ID
     * @param playerId 玩家ID
     */
    private void consumeInvestigationChance(Long gameId, Long playerId) {
        try {
            // 获取工作流上下文
            org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext context = investigationService.getWorkflowContext(gameId);
            if (context != null) {
                // 记录扣减前的剩余次数
                int beforeCount = context.getRemainingInvestigationCount(playerId);
                
                // 扣减搜证次数
                boolean consumed = context.consumeInvestigationChance(playerId);
                if (consumed) {
                    // 记录扣减后的剩余次数
                    int afterCount = context.getRemainingInvestigationCount(playerId);
                    log.info("扣减后剩余搜证次数: 玩家ID={}, 剩余次数={}", playerId, afterCount);
                    
                    // 检查是否已用完所有搜证次数
                    if (afterCount <= 0) {
                        context.markInvestigationCompleted(playerId);
                        log.info("玩家 {} 已用完所有搜证次数，标记为已完成搜证", playerId);
                    }
                    
                    // 保存更新后的上下文
                    investigationService.saveWorkflowContext(gameId, context);
                    log.info("已保存更新后的工作流上下文，游戏ID={}", gameId);
                } else {
                    log.warn("扣减玩家 {} 搜证次数失败，可能已无剩余次数", playerId);
                }
            } else {
                log.warn("游戏 {} 的工作流上下文不存在，无法扣减搜证次数", gameId);
            }
        } catch (Exception e) {
            log.error("扣减搜证次数失败: {}", e.getMessage(), e);
            // 扣减失败不影响整个搜证流程
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

    /**
     * 搜证消息数据类
     * 用于封装解析后的消息数据
     */
    private static class InvestigationMessageData {
        private final Long gameId;
        private final Long playerId;
        private final List<Map<String, Object>> clueOptions;
        private final Integer maxChances;

        public InvestigationMessageData(Long gameId, Long playerId, List<Map<String, Object>> clueOptions, Integer maxChances) {
            this.gameId = gameId;
            this.playerId = playerId;
            this.clueOptions = clueOptions;
            this.maxChances = maxChances;
        }

        public Long getGameId() {
            return gameId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public List<Map<String, Object>> getClueOptions() {
            return clueOptions;
        }

        public Integer getMaxChances() {
            return maxChances;
        }
    }
}

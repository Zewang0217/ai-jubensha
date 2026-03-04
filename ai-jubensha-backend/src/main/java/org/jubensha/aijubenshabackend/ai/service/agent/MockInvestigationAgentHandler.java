package org.jubensha.aijubenshabackend.ai.service.agent;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.jubensha.aijubenshabackend.ai.service.util.ResponseUtils;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.jubensha.aijubenshabackend.memory.MemoryService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Mock 搜证 Agent 处理器
 * 在 mock-ai profile 下替代真实的 InvestigationAgentHandler
 * 用于测试 WebSocket 通信的前后端交互，避免大模型 token 消耗
 *
 * @author zewang
 * @date 2026-03-04
 */
@Slf4j
@Component
@Profile("mock-ai")
public class MockInvestigationAgentHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GameService gameService;
    private GamePlayerService gamePlayerService;
    private InvestigationService investigationService;
    private SceneService sceneService;
    private ClueService clueService;
    private MemoryService memoryService;
    private RAGService ragService;

    /**
     * 监听 AI 玩家搜证通知
     * 返回预设的搜证决策，不调用大模型
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
            log.info("[Mock AI] 接收到AI玩家搜证通知: {}", message);

            Long gameId = ((Number) message.get("gameId")).longValue();
            Long playerId = ((Number) message.get("playerId")).longValue();
            List<Map<String, Object>> clueOptions = objectMapper.convertValue(
                    message.get("clueOptions"), 
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            Integer maxChances = ((Number) message.get("maxChances")).intValue();

            log.info("[Mock AI] 处理AI玩家搜证: 游戏ID={}, 玩家ID={}, 线索选项数量={}, 最大搜证次数={}",
                    gameId, playerId, clueOptions.size(), maxChances);

            lazyInitServices();

            if (!validateGameState(gameId)) {
                log.warn("[Mock AI] 游戏状态验证失败，跳过处理搜证消息: 游戏ID={}", gameId);
                return;
            }

            if (isTaskCancelled(gameId)) {
                log.warn("[Mock AI] 任务已被取消，跳过处理搜证消息: 游戏ID={}", gameId);
                return;
            }

            if (isMessageExpired(message)) {
                log.warn("[Mock AI] 消息已过期，跳过处理搜证消息: 游戏ID={}", gameId);
                return;
            }

            if (!isWorkflowContextExists(gameId)) {
                log.warn("[Mock AI] 工作流上下文不存在，跳过处理搜证消息: 游戏ID={}", gameId);
                return;
            }

            String characterName = getPlayerCharacterName(gameId, playerId);
            if (characterName == null) {
                log.warn("[Mock AI] 无法获取玩家 {} 的角色信息", playerId);
                characterName = "AI玩家" + playerId;
            }

            List<String> clueOptionStrings = clueOptions.stream()
                    .map(clue -> clue.get("clueId") + ": " + clue.get("clueName"))
                    .collect(Collectors.toList());

            String result = generateMockInvestigationResult(clueOptionStrings, maxChances);
            log.info("[Mock AI] AI玩家搜证完成: 结果={}", result);

            try {
                com.fasterxml.jackson.databind.JsonNode resultJson = ResponseUtils.extractJson(result);
                Map<String, Object> investigationResult = objectMapper.convertValue(
                        resultJson, 
                        new TypeReference<Map<String, Object>>() {}
                );
                List<Map<String, Object>> requests = objectMapper.convertValue(
                        investigationResult.get("investigationRequests"), 
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                for (Map<String, Object> request : requests) {
                    String clueIdStr = (String) request.get("clueId");

                    if (clueIdStr == null) {
                        log.warn("[Mock AI] 搜证请求格式错误，缺少必要字段: {}", request);
                        continue;
                    }

                    try {
                        Long clueId = parseClueId(clueIdStr);
                        log.info("[Mock AI] 解析AI搜证结果: 线索ID={}", clueId);

                        java.util.Optional<org.jubensha.aijubenshabackend.models.entity.Clue> clueOptional = 
                                clueService.getClueById(clueId);
                        if (clueOptional.isPresent()) {
                            org.jubensha.aijubenshabackend.models.entity.Clue clue = clueOptional.get();
                            String clueContent = clue.getName() + ": " + clue.getDescription();

                            log.info("[Mock AI] AI玩家搜证成功: 线索ID={}, 线索名称={}", clueId, clue.getName());

                            String revealDecision = generateMockRevealDecision(clueId, clueContent);
                            log.info("[Mock AI] AI玩家对线索 {} 的公开决定: {}", clueId, revealDecision);

                            try {
                                com.fasterxml.jackson.databind.JsonNode decisionJson = ResponseUtils.extractJson(revealDecision);
                                Map<String, Object> decisionResult = objectMapper.convertValue(
                                        decisionJson, 
                                        new TypeReference<Map<String, Object>>() {}
                                );
                                String decision = (String) decisionResult.get("decision");
                                String reason = (String) decisionResult.get("reason");
                                String analysis = (String) decisionResult.get("analysis");

                                log.info("[Mock AI] 解析决定结果: 决策={}, 理由={}, 分析={}", decision, reason, analysis);

                                if ("公开".equals(decision)) {
                                    log.info("[Mock AI] AI玩家选择公开线索: {}", clueId);
                                    clue.setVisibility(ClueVisibility.PUBLIC);
                                    clueService.updateClue(clueId, clue);
                                    log.info("[Mock AI] 线索 {} 可见性已修改为 PUBLIC", clueId);
                                    memoryService.storeClueMemory(gameId, 0L, clueId, clueContent, "AI玩家" + playerId);
                                    log.info("[Mock AI] 线索 {} 已存储到向量数据库，playerid=0", clueId);
                                } else {
                                    log.info("[Mock AI] AI玩家选择不公开线索: {}", clueId);
                                    clue.setVisibility(ClueVisibility.PRIVATE);
                                    clueService.updateClue(clueId, clue);
                                    log.info("[Mock AI] 线索 {} 可见性已修改为 PRIVATE", clueId);
                                    memoryService.storeClueMemory(gameId, playerId, clueId, clueContent, "AI玩家" + playerId);
                                    log.info("[Mock AI] 线索 {} 已存储到向量数据库，playerid={}", clueId, playerId);
                                }

                                consumeInvestigationChance(gameId, playerId);

                            } catch (Exception e) {
                                log.error("[Mock AI] 解析决定结果失败: {}", e.getMessage(), e);
                            }
                        } else {
                            log.warn("[Mock AI] 线索ID {} 不存在", clueId);
                        }

                    } catch (NumberFormatException e) {
                        log.warn("[Mock AI] 搜证请求中ID格式错误: 线索ID={}", clueIdStr);
                    } catch (Exception e) {
                        log.error("[Mock AI] 执行搜证操作失败: {}", e.getMessage(), e);
                    }
                }

            } catch (Exception e) {
                log.error("[Mock AI] 解析AI玩家搜证请求失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("[Mock AI] 处理AI玩家搜证通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成 Mock 搜证结果
     * 用完所有搜证次数，确保搜证阶段能正常完成
     */
    private String generateMockInvestigationResult(List<String> clueOptions, int maxChances) {
        int selectCount = Math.min(maxChances, clueOptions.size());
        List<String> selectedOptions = new ArrayList<>(clueOptions);
        Collections.shuffle(selectedOptions);
        selectedOptions = selectedOptions.subList(0, selectCount);

        StringBuilder sb = new StringBuilder("{\"investigationRequests\": [");
        for (int i = 0; i < selectedOptions.size(); i++) {
            if (i > 0) sb.append(", ");
            String[] parts = selectedOptions.get(i).split(": ", 2);
            String clueId = parts[0];
            sb.append("{\"clueId\": \"").append(clueId).append("\"}");
        }
        sb.append("]}");

        return sb.toString();
    }

    /**
     * 生成 Mock 公开决策
     * 70% 概率选择公开
     */
    private String generateMockRevealDecision(Long clueId, String clueContent) {
        boolean reveal = Math.random() < 0.7;
        String contentPreview = clueContent.length() > 20 ? clueContent.substring(0, 20) : clueContent;

        return String.format(
                "{\"decision\": \"%s\", \"reason\": \"%s\", \"analysis\": \"【Mock分析】该线索'%s...'对案件有重要参考价值\"}",
                reveal ? "公开" : "不公开",
                reveal ? "【Mock理由】对案件很重要" : "【Mock理由】需要进一步确认",
                contentPreview
        );
    }

    /**
     * 扣减搜证次数
     */
    private void consumeInvestigationChance(Long gameId, Long playerId) {
        try {
            org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext context = 
                    investigationService.getWorkflowContext(gameId);
            if (context != null) {
                int beforeCount = context.getRemainingInvestigationCount(playerId);
                boolean consumed = context.consumeInvestigationChance(playerId);
                if (consumed) {
                    int afterCount = context.getRemainingInvestigationCount(playerId);
                    log.info("[Mock AI] 扣减后剩余搜证次数: 玩家ID={}, 剩余次数={}", playerId, afterCount);

                    if (afterCount <= 0) {
                        context.markInvestigationCompleted(playerId);
                        log.info("[Mock AI] 玩家 {} 已用完所有搜证次数，标记为已完成搜证", playerId);
                    }

                    investigationService.saveWorkflowContext(gameId, context);
                    log.info("[Mock AI] 已保存更新后的工作流上下文，游戏ID={}", gameId);
                } else {
                    log.warn("[Mock AI] 扣减玩家 {} 搜证次数失败，可能已无剩余次数", playerId);
                }
            } else {
                log.warn("[Mock AI] 游戏 {} 的工作流上下文不存在，无法扣减搜证次数", gameId);
            }
        } catch (Exception e) {
            log.error("[Mock AI] 扣减搜证次数失败: {}", e.getMessage(), e);
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
     */
    private String getPlayerCharacterName(Long gameId, Long playerId) {
        try {
            return "AI玩家" + playerId;
        } catch (Exception e) {
            log.warn("[Mock AI] 获取玩家角色名称失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析线索ID
     */
    private Long parseClueId(String clueIdStr) {
        if (clueIdStr == null) {
            throw new NumberFormatException("线索ID不能为空");
        }

        if (clueIdStr.startsWith("clue_")) {
            String numericPart = clueIdStr.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                throw new NumberFormatException("线索ID格式错误，无法提取数字部分");
            }
            return Long.parseLong(numericPart);
        }

        return Long.parseLong(clueIdStr);
    }

    /**
     * 验证游戏状态是否有效
     */
    private boolean validateGameState(Long gameId) {
        try {
            var gameOpt = gameService.getGameById(gameId);
            if (!gameOpt.isPresent()) {
                log.warn("[Mock AI] 游戏不存在: 游戏ID={}", gameId);
                return false;
            }

            var game = gameOpt.get();
            var currentPhase = game.getCurrentPhase();
            if (currentPhase == null) {
                log.warn("[Mock AI] 游戏状态未设置: 游戏ID={}", gameId);
                return false;
            }

            return GamePhase.SEARCH.equals(currentPhase);
        } catch (Exception e) {
            log.error("[Mock AI] 验证游戏状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查任务是否已被取消
     */
    private boolean isTaskCancelled(Long gameId) {
        try {
            var context = investigationService.getWorkflowContext(gameId);
            if (context != null) {
                return context.isCancelled();
            }
            return false;
        } catch (Exception e) {
            log.error("[Mock AI] 检查任务取消状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查消息是否过期
     */
    private boolean isMessageExpired(Map<String, Object> message) {
        try {
            Object timestampObj = message.get("timestamp");
            if (timestampObj instanceof Number timestampNum) {
                long messageTimestamp = timestampNum.longValue();
                long currentTime = System.currentTimeMillis();
                long tenMinutesInMillis = 10 * 60 * 1000;

                return currentTime - messageTimestamp > tenMinutesInMillis;
            }
            return true;
        } catch (Exception e) {
            log.error("[Mock AI] 检查消息过期状态失败: {}", e.getMessage(), e);
            return true;
        }
    }

    /**
     * 检查工作流上下文是否存在
     */
    private boolean isWorkflowContextExists(Long gameId) {
        try {
            var context = investigationService.getWorkflowContext(gameId);
            return context != null;
        } catch (Exception e) {
            log.error("[Mock AI] 检查工作流上下文失败: {}", e.getMessage(), e);
            return false;
        }
    }
}

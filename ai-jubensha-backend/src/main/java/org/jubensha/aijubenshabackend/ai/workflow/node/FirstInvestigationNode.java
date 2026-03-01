package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.MessageQueueService;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.game.GameService;
import org.jubensha.aijubenshabackend.service.investigation.InvestigationService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import org.jubensha.aijubenshabackend.websocket.service.WebSocketService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 第一轮搜证节点
 *
 * @author zewang
 * @author zewang
 * @version 1.0
 * @date 2026-02-01
 * @since 2026
 */

@Slf4j
public class FirstInvestigationNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("FirstInvestigationNode: {}", context);
            log.info("执行节点：第一轮搜证");

            // 获取剧本ID
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法开始第一轮搜证");
                context.setErrorMessage("剧本ID为空，无法开始第一轮搜证");
                return WorkflowContext.saveContext(context);
            }

            // 获取玩家分配结果
            List<Map<String, Object>> playerAssignments = context.getPlayerAssignments();
            if (playerAssignments == null || playerAssignments.isEmpty()) {
                log.error("玩家分配结果为空，无法开始第一轮搜证");
                context.setErrorMessage("玩家分配结果为空，无法开始第一轮搜证");
                return WorkflowContext.saveContext(context);
            }

            // 获取场景列表
            List<Scene> scenes = context.getScenes();
            if (scenes == null || scenes.isEmpty()) {
                log.error("场景列表为空，无法开始第一轮搜证");
                context.setErrorMessage("场景列表为空，无法开始第一轮搜证");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 获取线索服务
                ClueService clueService = SpringContextUtil.getBean(ClueService.class);
                // 获取场景服务
                SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
                // 获取AI服务
                AIService aiService = SpringContextUtil.getBean(AIService.class);
                // 获取WebSocket服务
                WebSocketService webSocketService = SpringContextUtil.getBean(WebSocketService.class);
                // 获取消息队列服务
                MessageQueueService messageQueueService = SpringContextUtil.getBean(MessageQueueService.class);
                // 获取游戏服务
                GameService gameService = SpringContextUtil.getBean(GameService.class);

                // 获取RAG服务
                RAGService ragService = SpringContextUtil.getBean(RAGService.class);

                // 整合场景和角色信息, 存放到investigationScenes中
                // 用于存储所有搜证场景, 其中包含场景线索列表
                List<Map<String, Object>> investigationScenes = new ArrayList<>();
                for (Scene scene : scenes) {
                    // 获取场景中的线索
                    List<Clue> sceneClues = clueService.getCluesByScene(scene.getName());

                    // 暂时不存储场景线索到RAGService
                // 线索应该只在玩家实际发现时才存储到数据库中
                for (Clue clue : sceneClues) {
                    // 记录线索信息，但不提前存储到向量数据库
                    log.info("[搜证环节] 场景线索: {} - {}", clue.getName(), clue.getType());
                }

                    // 构建场景信息，只包含线索的基本信息（不包含完整描述，保持神秘感）
                    List<Map<String, Object>> clueSummaries = new ArrayList<>();
                    for (Clue clue : sceneClues) {
                        Map<String, Object> clueSummary = new java.util.HashMap<>();
                        clueSummary.put("clueId", clue.getId());
                        clueSummary.put("clueName", clue.getName());
                        clueSummary.put("type", clue.getType());
                        clueSummary.put("importance", clue.getImportance());
                        // 不返回描述，玩家需要搜证才能看到
                        clueSummaries.add(clueSummary);
                    }

                    Map<String, Object> sceneInfo = new java.util.HashMap<>();
                    sceneInfo.put("sceneId", scene.getId());
                    sceneInfo.put("sceneName", scene.getName());
                    sceneInfo.put("description", scene.getDescription());
                    sceneInfo.put("clueCount", sceneClues.size());
                    sceneInfo.put("clues", clueSummaries);
                    investigationScenes.add(sceneInfo);
                    log.info("场景 {} 中有 {} 个线索", scene.getName(), sceneClues.size());
                }

                // 初始化玩家搜证次数
                List<Long> playerIds = new ArrayList<>();
                for (Map<String, Object> assignment : playerAssignments) {
                    Long playerId = (Long) assignment.get("playerId");
                    playerIds.add(playerId);
                }
                context.initInvestigationCounts(playerIds);
                context.setCurrentInvestigationPhase("FIRST_INVESTIGATION");
                log.info("已初始化 {} 个玩家的搜证次数，每轮 {} 次", playerIds.size(), WorkflowContext.DEFAULT_INVESTIGATION_LIMIT);
                log.info("[状态转换] 进入第一轮搜证阶段，当前阶段: {}", context.getCurrentInvestigationPhase());

                // 保存工作流上下文到InvestigationService缓存
                InvestigationService investigationService = SpringContextUtil.getBean(InvestigationService.class);
                investigationService.saveWorkflowContext(context.getGameId(), context);
                log.info("[上下文管理] 已保存工作流上下文到InvestigationService缓存，游戏ID: {}", context.getGameId());

                // 更新游戏状态为搜证阶段
                if (context.getGameId() != null) {
                    var gameOpt = gameService.getGameById(context.getGameId());
                    if (gameOpt.isPresent()) {
                        Game game = gameOpt.get();
                        game.setCurrentPhase(GamePhase.SEARCH);
                        gameService.updateGame(context.getGameId(), game);
                        log.info("[状态转换] 游戏状态已更新为: SEARCH");
                    }
                }

                /*
                Map<String, Object> assignment = Map.of(
                    "playerType", "REAL",
                    "playerId", realPlayer.getId(),
                    "characterId", character.getId(),
                    "characterName", character.getName()
                );
                */
                // 通知玩家开始第一轮搜证
                for (Map<String, Object> assignment : playerAssignments) {
                    String playerType = (String) assignment.get("playerType");
                    Long playerId = (Long) assignment.get("playerId");
                    Long characterId = (Long) assignment.get("characterId");
                    String characterName = (String) assignment.get("characterName");

                    // 构建玩家搜证通知数据
                    Map<String, Object> investigationData = new java.util.HashMap<>();
                    investigationData.put("scenes", investigationScenes);
                    investigationData.put("remainingChances", WorkflowContext.DEFAULT_INVESTIGATION_LIMIT);
                    investigationData.put("totalChances", WorkflowContext.DEFAULT_INVESTIGATION_LIMIT);
                    investigationData.put("phase", "FIRST_INVESTIGATION");
                    investigationData.put("round", 1);

                    if ("AI".equals(playerType)) {
                        // 准备线索ID和名称列表
                        List<Map<String, Object>> clueOptions = new ArrayList<>();
                        for (Scene scene : scenes) {
                            List<Clue> sceneClues = clueService.getCluesByScene(scene.getName());
                            for (Clue clue : sceneClues) {
                                Map<String, Object> clueOption = new java.util.HashMap<>();
                                clueOption.put("clueId", clue.getId());
                                clueOption.put("clueName", clue.getName());
                                clueOption.put("sceneName", scene.getName());
                                clueOptions.add(clueOption);
                            }
                        }
                        
                        // 通过消息队列通知AI玩家开始搜证
                        messageQueueService.sendInvestigationNotification(
                                context.getGameId(),
                                playerId,
                                clueOptions,
                                WorkflowContext.DEFAULT_INVESTIGATION_LIMIT
                        );
                        // 线索信息已经存储到global_memory中，不需要再存储到对话记忆
                        // 对话记忆应该只存储玩家的对话内容，而不是线索信息
                        log.info("通过消息队列通知AI玩家 {} (角色: {}) 开始第一轮搜证", playerId, characterName);
                    } else {
                        // 通知真人玩家开始搜证
                        try {
                            webSocketService.notifyPlayerStartInvestigation(playerId, investigationData);
                            log.info("通过WebSocket通知真人玩家 {} (角色: {}) 开始第一轮搜证", playerId, characterName);
                        } catch (Exception e) {
                            log.warn("WebSocket通知失败，使用日志记录", e);
                            log.info("通知真人玩家 {} (角色: {}) 开始第一轮搜证", playerId, characterName);
                        }
                    }
                }

                // 更新WorkflowContext
                context.setCurrentStep("第一轮搜证");
                context.setCurrentPhase("FIRST_INVESTIGATION");
                context.setSuccess(true);

                log.info("第一轮搜证开始，共 {} 个场景可供搜证", scenes.size());
                log.info("[状态监控] 开始监控玩家搜证完成状态，等待所有玩家完成搜证");

                // 等待所有玩家完成搜证
                int waitCount = 0;
                int maxWaitTime = 3600; // 最大等待时间，单位：秒
                int checkInterval = 5; // 检查间隔，单位：秒
                boolean allCompleted = false;
                
                // 使用之前获取的InvestigationService
                
                while (!allCompleted && waitCount < maxWaitTime) {
                    // 从缓存中获取最新的WorkflowContext状态
                    WorkflowContext latestContext = investigationService.getWorkflowContext(context.getGameId());
                    if (latestContext != null) {
                        // 更新当前上下文为最新状态
                        context = latestContext;
                        allCompleted = context.isAllInvestigationCompleted();
                        
                        log.info("[状态监控] 等待玩家完成搜证，当前状态: {}", context.getPlayerInvestigationCompleted());
                        log.info("[状态监控] 剩余搜证次数: {}", context.getAllInvestigationCounts());
                        log.info("[状态监控] 已等待时间: {}秒，最大等待时间: {}秒", waitCount, maxWaitTime);
                    } else {
                        log.warn("[状态监控] 无法获取最新的WorkflowContext状态");
                    }
                    
                    if (!allCompleted) {
                        Thread.sleep(checkInterval * 1000);
                        waitCount += checkInterval;
                    }
                }

                if (allCompleted) {
                    log.info("[状态转换] 所有玩家已完成搜证，准备进入讨论阶段");
                    log.info("[状态监控] 最终搜证状态: {}", context.getPlayerInvestigationCompleted());
                    log.info("[状态监控] 总等待时间: {}秒", waitCount);
                    
                    // 更新游戏状态为讨论阶段
                    if (context.getGameId() != null) {
                        var gameOpt = gameService.getGameById(context.getGameId());
                        if (gameOpt.isPresent()) {
                            Game game = gameOpt.get();
                            game.setCurrentPhase(GamePhase.DISCUSSION);
                            gameService.updateGame(context.getGameId(), game);
                            log.info("[状态转换] 游戏状态已更新为: DISCUSSION");
                        }
                    }
                } else {
                    log.warn("[状态监控] 等待超时，强制进入讨论阶段");
                    log.warn("[状态监控] 最终搜证状态: {}", context.getPlayerInvestigationCompleted());
                    log.warn("[状态监控] 已等待时间: {}秒，超过最大等待时间: {}秒", waitCount, maxWaitTime);
                    
                    // 超时后强制更新游戏状态为讨论阶段
                    if (context.getGameId() != null) {
                        var gameOpt = gameService.getGameById(context.getGameId());
                        if (gameOpt.isPresent()) {
                            Game game = gameOpt.get();
                            game.setCurrentPhase(GamePhase.DISCUSSION);
                            gameService.updateGame(context.getGameId(), game);
                            log.info("[状态转换] 游戏状态已更新为: DISCUSSION (超时强制)");
                        }
                    }
                }

                log.info("[状态转换] 第一轮搜证阶段结束，准备进入讨论阶段");

            } catch (Exception e) {
                log.error("开始第一轮搜证失败: {}", e.getMessage(), e);
                context.setErrorMessage("开始第一轮搜证失败: " + e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }
}
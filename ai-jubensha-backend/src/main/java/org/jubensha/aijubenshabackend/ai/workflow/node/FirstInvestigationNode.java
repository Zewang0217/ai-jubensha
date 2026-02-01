package org.jubensha.aijubenshabackend.ai.workflow.node;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 第一轮搜证节点
 *
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
                
                // 整合场景和角色信息
                List<Map<String, Object>> investigationScenes = new ArrayList<>();
                for (Scene scene : scenes) {
                    // 获取场景中的线索
                    List<Clue> sceneClues = clueService.getCluesByScene(scene.getName());
                    
                    Map<String, Object> sceneInfo = Map.of(
                        "sceneId", scene.getId(),
                        "sceneName", scene.getName(),
                        "description", scene.getDescription(),
                        "clues", sceneClues
                    );
                    investigationScenes.add(sceneInfo);
                    log.info("场景 {} 中有 {} 个线索", scene.getName(), sceneClues.size());
                }
                
                // 通知玩家开始第一轮搜证
                for (Map<String, Object> assignment : playerAssignments) {
                    String playerType = (String) assignment.get("playerType");
                    Long playerId = (Long) assignment.get("playerId");
                    String characterName = (String) assignment.get("characterName");
                    
                    if ("AI".equals(playerType)) {
                        // 通知AI玩家开始搜证
                        aiService.notifyAIPlayerStartInvestigation(playerId, investigationScenes);
                        log.info("通知AI玩家 {} (角色: {}) 开始第一轮搜证", playerId, characterName);
                    } else {
                        // 通知真人玩家开始搜证
                        // 这里可以通过WebSocket或其他方式通知前端
                        log.info("通知真人玩家 {} (角色: {}) 开始第一轮搜证", playerId, characterName);
                    }
                }
                
                // 更新WorkflowContext
                context.setCurrentStep("第一轮搜证");
                context.setCurrentPhase("FIRST_INVESTIGATION");
                context.setSuccess(true);
                
                log.info("第一轮搜证开始，共 {} 个场景可供搜证", scenes.size());
                
            } catch (Exception e) {
                log.error("开始第一轮搜证失败: {}", e.getMessage(), e);
                context.setErrorMessage("开始第一轮搜证失败: " + e.getMessage());
                context.setSuccess(false);
            }
            
            return WorkflowContext.saveContext(context);
        });
    }
}
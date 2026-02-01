package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.models.entity.SceneClue;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;
import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 场景加载节点
 *
 * @author zewang
 * @version 1.0
 * @date 2026-02-01
 * @since 2026
 */

@Slf4j
public class SceneLoaderNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("SceneLoaderNode: {}", context);
            log.info("执行节点：场景加载");
            
            // 获取剧本ID
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法加载场景");
                context.setErrorMessage("剧本ID为空，无法加载场景");
                return WorkflowContext.saveContext(context);
            }
            
            // 获取剧本JSON
            String scriptJson = context.getModelOutput();
            if (scriptJson == null || scriptJson.isEmpty()) {
                log.error("剧本内容为空，无法加载场景");
                context.setErrorMessage("剧本内容为空，无法加载场景");
                return WorkflowContext.saveContext(context);
            }
            
            try {
                // 解析JSON剧本
                JsonNode rootNode = objectMapper.readTree(scriptJson);
                
                // 提取场景信息
                List<Scene> scenes = parseScenes(rootNode, scriptId);
                
                // 保存场景到数据库
                SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
                ClueService clueService = SpringContextUtil.getBean(ClueService.class);
                
                List<Scene> savedScenes = new ArrayList<>();
                for (Scene scene : scenes) {
                    Scene savedScene = sceneService.createScene(scene);
                    savedScenes.add(savedScene);
                    log.info("场景已保存到数据库，ID: {}, 名称: {}", savedScene.getId(), savedScene.getName());
                }
                
                // 关联场景和线索
                associateSceneClues(rootNode, savedScenes, clueService);
                
                // 更新WorkflowContext
                context.setCurrentStep("场景加载");
                context.setScenes(savedScenes);
                context.setSuccess(true);
                
                log.info("场景加载完成，共加载 {} 个场景", savedScenes.size());
                
            } catch (Exception e) {
                log.error("加载场景失败: {}", e.getMessage(), e);
                context.setErrorMessage("加载场景失败: " + e.getMessage());
                context.setSuccess(false);
            }
            
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 解析场景信息
     */
    private static List<Scene> parseScenes(JsonNode rootNode, Long scriptId) {
        List<Scene> scenes = new ArrayList<>();
        JsonNode scenesNode = rootNode.path("scenes");
        if (scenesNode.isArray()) {
            for (JsonNode sceneNode : scenesNode) {
                Scene scene = new Scene();
                org.jubensha.aijubenshabackend.models.entity.Script script = new org.jubensha.aijubenshabackend.models.entity.Script();
                script.setId(scriptId);
                scene.setScript(script);
                scene.setName(sceneNode.path("name").asText());
                scene.setDescription("时间: " + sceneNode.path("time").asText() + "\n" +
                    "地点: " + sceneNode.path("location").asText() + "\n" +
                    "氛围: " + sceneNode.path("atmosphere").asText() + "\n" +
                    "描述: " + sceneNode.path("description").asText());
                scene.setCreateTime(java.time.LocalDateTime.now());
                scenes.add(scene);
            }
        }
        return scenes;
    }

    /**
     * 关联场景和线索
     */
    private static void associateSceneClues(JsonNode rootNode, List<Scene> scenes, ClueService clueService) {
        // 提取线索信息
        JsonNode cluesNode = rootNode.path("clues");
        if (!cluesNode.isArray()) {
            return;
        }
        
        // 为每个场景关联线索
        for (Scene scene : scenes) {
            JsonNode scenesNode = rootNode.path("scenes");
            if (scenesNode.isArray()) {
                for (JsonNode sceneNode : scenesNode) {
                    if (sceneNode.path("name").asText().equals(scene.getName())) {
                        JsonNode cluesInSceneNode = sceneNode.path("clues");
                        if (cluesInSceneNode.isArray()) {
                            for (JsonNode clueIdNode : cluesInSceneNode) {
                                String clueId = clueIdNode.asText();
                                // 查找线索
                                // 注意：这里需要根据实际情况实现线索查找逻辑
                                // 例如，通过线索ID从数据库中查找线索
                                // Clue clue = clueService.getClueById(clueId);
                                // if (clue != null) {
                                //     // 创建场景线索关联
                                //     SceneClue sceneClue = new SceneClue();
                                //     sceneClue.setSceneId(scene.getId());
                                //     sceneClue.setClueId(clue.getId());
                                //     // 保存关联
                                //     sceneClueService.createSceneClue(sceneClue);
                                // }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}
package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.models.ImageResource;
import org.jubensha.aijubenshabackend.ai.tools.ImageSearchTool;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.exception.BusinessException;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Clue;
import org.jubensha.aijubenshabackend.models.entity.Scene;
import org.jubensha.aijubenshabackend.service.clue.ClueService;
import org.jubensha.aijubenshabackend.service.scene.SceneService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 场景加载节点
 * 用于提前加载各个场景的线索, 把各个场景的线索加载到数据库中
 * 异步且并发地拉取线索相关的网络素材图片
 *
 * @author zewang
 * @author luobo
 * @version 1.0
 * @date 2026-02-01
 * @since 2026
 */

@Slf4j
public class SceneLoaderNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 内存缓存，用于存储场景和线索数据
    private static final Map<String, SceneCacheEntry> sceneCache = new ConcurrentHashMap<>();

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
//            log.debug("SceneLoaderNode: {}", context);
            log.info("执行节点：场景加载");

            // 获取游戏ID和剧本ID
            Long gameId = context.getGameId();
            Long scriptId = context.getScriptId();

            if (scriptId == null) {
                log.error("剧本ID为空，无法加载场景");
                context.setErrorMessage("剧本ID为空，无法加载场景");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 尝试从缓存加载
                String cacheKey = generateCacheKey(gameId, scriptId);
                SceneCacheEntry cacheEntry = sceneCache.get(cacheKey);

                List<Scene> scenes;
                Map<Long, List<Clue>> sceneCluesMap;

                if (cacheEntry != null && !cacheEntry.isExpired()) {
                    log.info("从缓存加载场景数据");
                    scenes = cacheEntry.getScenes();
                    sceneCluesMap = cacheEntry.getSceneCluesMap();
                } else {
                    log.info("从数据库加载场景数据");
                    // 从数据库加载场景
                    SceneService sceneService = SpringContextUtil.getBean(SceneService.class);
                    ClueService clueService = SpringContextUtil.getBean(ClueService.class);

                    // 加载剧本的所有场景
                    scenes = sceneService.getScenesByScriptId(scriptId);

                    // 如果数据库中没有场景，尝试从JSON解析
                    if (scenes == null || scenes.isEmpty()) {
                        String scriptJson = context.getModelOutput();
                        if (scriptJson != null && !scriptJson.isEmpty()) {
                            log.info("数据库中无场景，尝试从JSON解析");
                            JsonNode rootNode = objectMapper.readTree(scriptJson);
                            scenes = parseScenes(rootNode, scriptId);

                            // 保存场景到数据库
                            List<Scene> savedScenes = new ArrayList<>();
                            for (Scene scene : scenes) {
                                Scene savedScene = sceneService.createScene(scene);
                                savedScenes.add(savedScene);
                                log.info("场景已保存到数据库，ID: {}, 名称: {}", savedScene.getId(), savedScene.getName());
                            }
                            scenes = savedScenes;
                        }
                    }

                    // 加载线索并为各个线索设置图片
                    List<Clue> allClues = clueService.getCluesByScriptId(scriptId);
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (Clue clue : allClues) {
                        if (clue.getImageUrl() == null || clue.getImageUrl().isEmpty()) {
                            log.info("线索 {} 没有图片，尝试使用图片搜索工具获取图片", clue.getName());


                            // 1. 异步执行图片搜索任务
                            CompletableFuture<ImageResource> imageFuture = CompletableFuture.supplyAsync(
                                            () -> imageSearchTool.searchImage(clue.getName())
                                    )
                                    // 2. 异常处理（避免异步任务抛异常导致程序崩溃）
                                    .exceptionally(e -> {
                                        log.warn("异步搜索图片失败：" + e.getMessage());
                                        throw new BusinessException("异步搜索图片失败：" + e.getMessage());
                                    });

                            // 3. 获取异步执行结果
                            ImageResource imageResource = imageFuture.join();

                            String imageUrl = clueService.updateClueImage(clue.getId(), imageResource.getImageUrl());
                            clue.setImageUrl(imageUrl);
                        }
                    }

                    // 加载线索并构建场景-线索映射
                    sceneCluesMap = new HashMap<>();
                    if (scenes != null && !scenes.isEmpty()) {
//                        List<Clue> allClues = clueService.getCluesByScriptId(scriptId);
                        for (Scene scene : scenes) {
                            List<Clue> sceneClues = new ArrayList<>();
                            for (Clue clue : allClues) {
                                // 线索过滤逻辑：
                                // 1. 首先检查线索的scene字段是否直接匹配场景名称
                                // 2. 如果不匹配，尝试检查线索描述中是否包含场景名称
                                // 3. 确保线索属于当前剧本
                                boolean isSceneMatch = false;

                                // 检查线索的scene字段
                                if (clue.getScene() != null) {
                                    String clueScene = clue.getScene().trim();
                                    String sceneName = scene.getName().trim();
                                    isSceneMatch = clueScene.equals(sceneName) ||
                                            clueScene.contains(sceneName) ||
                                            sceneName.contains(clueScene);
                                }

                                // 如果场景字段不匹配，检查线索描述
                                if (!isSceneMatch && clue.getDescription() != null) {
                                    String description = clue.getDescription().toLowerCase();
                                    String sceneNameLower = scene.getName().toLowerCase();
                                    isSceneMatch = description.contains(sceneNameLower);
                                }

                                // 确保线索属于当前剧本
                                if (isSceneMatch && clue.getScriptId() != null && clue.getScriptId().equals(scriptId)) {
                                    sceneClues.add(clue);
                                }
                            }
                            sceneCluesMap.put(scene.getId(), sceneClues);
                        }
                    }

                    // 更新缓存
                    sceneCache.put(cacheKey, new SceneCacheEntry(scenes, sceneCluesMap));
                }

                // 更新WorkflowContext
                context.setCurrentStep("场景加载");
                context.setScenes(scenes);
                context.setSuccess(true);

                // 存储场景-线索映射到上下文元数据
                if (context.getMetadata() == null) {
                    context.setMetadata(new HashMap<>());
                }
                context.getMetadata().put("sceneCluesMap", sceneCluesMap);
                context.getMetadata().put("cacheKey", cacheKey);

                log.info("场景加载完成，共加载 {} 个场景", scenes.size());
                for (Scene scene : scenes) {
                    List<Clue> clues = sceneCluesMap.get(scene.getId());
                    log.info("场景 {} 包含 {} 个线索", scene.getName(), clues != null ? clues.size() : 0);
                }

            } catch (Exception e) {
                log.error("加载场景失败: {}", e.getMessage(), e);
                context.setErrorMessage("加载场景失败: " + e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 生成缓存键
     */
    private static String generateCacheKey(Long gameId, Long scriptId) {
        return "scene_cache_" + (gameId != null ? gameId : "null") + "_" + scriptId;
    }

    /**
     * 解析场景信息
     * 从JSON节点中提取场景数据并转换为Scene实体对象列表
     *
     * @param rootNode 包含场景信息的JSON根节点
     * @param scriptId 剧本ID，用于关联场景与剧本
     * @return 解析后的场景列表
     */
    private static List<Scene> parseScenes(JsonNode rootNode, Long scriptId) {
        List<Scene> scenes = new ArrayList<>();
        JsonNode scenesNode = rootNode.path("scenes");
        if (scenesNode.isArray()) {
            // 遍历所有场景节点并创建Scene对象
            for (JsonNode sceneNode : scenesNode) {
                Scene scene = new Scene();
                org.jubensha.aijubenshabackend.models.entity.Script script = new org.jubensha.aijubenshabackend.models.entity.Script();
                script.setId(scriptId);
                scene.setScript(script);
                scene.setName(sceneNode.path("name").asText());
                // 构建格式化的场景描述，包含时间、地点、氛围和详细描述
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
     * 场景缓存条目
     */
    private static class SceneCacheEntry {
        @Getter
        private final List<Scene> scenes;
        @Getter
        private final Map<Long, List<Clue>> sceneCluesMap;
        private final long timestamp;
        private static final long EXPIRY_TIME = 3600000; // 1小时过期

        public SceneCacheEntry(List<Scene> scenes, Map<Long, List<Clue>> sceneCluesMap) {
            this.scenes = scenes;
            this.sceneCluesMap = sceneCluesMap;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > EXPIRY_TIME;
        }
    }
}
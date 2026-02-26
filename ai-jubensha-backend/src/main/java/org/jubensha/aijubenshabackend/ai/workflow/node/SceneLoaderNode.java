package org.jubensha.aijubenshabackend.ai.workflow.node;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.models.ImageResource;
import org.jubensha.aijubenshabackend.ai.service.util.ResponseUtils;
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
 * @author zewang
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
                        String scriptAiOutPut = context.getModelOutput();
                        // 戛然而止
                        log.debug("AI生成剧本原输出:\n\n{}\n\n", scriptAiOutPut);
                        if (scriptAiOutPut != null && !scriptAiOutPut.isEmpty()) {
                            log.info("数据库中无场景，尝试从JSON解析");
//                            JsonNode rootNode = objectMapper.readTree(scriptAiOutPut);
                            JsonNode rootNode = ResponseUtils.extractJson(scriptAiOutPut);
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

                        try {
                            // 1. 异步执行图片搜索任务
                            CompletableFuture<ImageResource> imageFuture = CompletableFuture.supplyAsync(
                                            () -> imageSearchTool.searchImage(clue.getName())
                                    )
                                    // 2. 异常处理（避免异步任务抛异常导致程序崩溃）
                                    .exceptionally(e -> {
                                        log.warn("异步搜索图片失败：{}", e.getMessage());
                                        // 返回默认图片，不抛出异常
                                        return ImageResource.builder()
                                                .description("默认图片")
                                                .imageUrl("https://images.pexels.com/photos/35637981/pexels-photo-35637981.jpeg")
                                                .build();
                                    });

                            // 3. 获取异步执行结果
                            ImageResource imageResource = imageFuture.join();

                            String imageUrl = clueService.updateClueImage(clue.getId(), imageResource.getImageUrl());
                            clue.setImageUrl(imageUrl);
                        } catch (Exception e) {
                            log.warn("处理线索图片失败：{}，继续处理其他线索", e.getMessage());
                            // 继续处理其他线索，不中断整个流程
                        }
                    }
                }

                    // 加载线索并构建场景-线索映射
                    sceneCluesMap = new HashMap<>();
                    if (scenes != null && !scenes.isEmpty()) {
//                        List<Clue> allClues = clueService.getCluesByScriptId(scriptId);
                        
                        // 第一步：使用多种匹配策略为场景分配线索
                        for (Scene scene : scenes) {
                            List<Clue> sceneClues = new ArrayList<>();
                            for (Clue clue : allClues) {
                                // 确保线索属于当前剧本
                                if (clue.getScriptId() == null || !clue.getScriptId().equals(scriptId)) {
                                    continue;
                                }
                                
                                // 线索匹配策略：
                                // 1. 精确匹配：线索场景字段与场景名称完全相同
                                // 2. 包含匹配：线索场景字段包含场景名称或反之
                                // 3. 描述匹配：线索描述中包含场景名称
                                // 4. 关键词匹配：场景名称的关键词在线索中出现
                                // 5. 模糊匹配：场景名称与线索场景字段相似度较高
                                boolean isSceneMatch = false;

                                // 1. 精确匹配
                                if (clue.getScene() != null) {
                                    String clueScene = clue.getScene().trim();
                                    String sceneName = scene.getName().trim();
                                    if (clueScene.equalsIgnoreCase(sceneName)) {
                                        isSceneMatch = true;
                                    }
                                }

                                // 2. 包含匹配
                                if (!isSceneMatch && clue.getScene() != null) {
                                    String clueScene = clue.getScene().trim().toLowerCase();
                                    String sceneName = scene.getName().trim().toLowerCase();
                                    isSceneMatch = clueScene.contains(sceneName) || sceneName.contains(clueScene);
                                }

                                // 3. 描述匹配
                                if (!isSceneMatch && clue.getDescription() != null) {
                                    String description = clue.getDescription().toLowerCase();
                                    String sceneNameLower = scene.getName().toLowerCase();
                                    isSceneMatch = description.contains(sceneNameLower);
                                }

                                // 4. 关键词匹配
                                if (!isSceneMatch) {
                                    String[] sceneKeywords = scene.getName().toLowerCase().split("\\s+");
                                    String clueText = (clue.getScene() != null ? clue.getScene() + " " : "") + 
                                                   (clue.getDescription() != null ? clue.getDescription() : "");
                                    String clueTextLower = clueText.toLowerCase();
                                    
                                    for (String keyword : sceneKeywords) {
                                        if (!keyword.isEmpty() && clueTextLower.contains(keyword)) {
                                            isSceneMatch = true;
                                            break;
                                        }
                                    }
                                }

                                // 5. 模糊匹配（简单相似度计算）
                                if (!isSceneMatch && clue.getScene() != null) {
                                    String clueScene = clue.getScene().trim().toLowerCase();
                                    String sceneName = scene.getName().trim().toLowerCase();
                                    double similarity = calculateSimilarity(clueScene, sceneName);
                                    if (similarity > 0.6) { // 相似度阈值
                                        isSceneMatch = true;
                                    }
                                }

                                if (isSceneMatch) {
                                    sceneClues.add(clue);
                                }
                            }
                            sceneCluesMap.put(scene.getId(), sceneClues);
                        }

                        // 第二步：确保每个场景至少有1个线索（兜底机制）
                        ensureEachSceneHasClues(scenes, sceneCluesMap, allClues, scriptId);
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
                
                // 线索分布统计
                int totalClues = 0;
                List<Integer> clueCounts = new ArrayList<>();
                Map<String, Integer> sceneClueCountMap = new HashMap<>();
                
                for (Scene scene : scenes) {
                    List<Clue> clues = sceneCluesMap.get(scene.getId());
                    int clueCount = clues != null ? clues.size() : 0;
                    totalClues += clueCount;
                    clueCounts.add(clueCount);
                    sceneClueCountMap.put(scene.getName(), clueCount);
                    log.info("场景 {} 包含 {} 个线索", scene.getName(), clueCount);
                }
                
                // 计算线索分布平衡性
                if (!clueCounts.isEmpty()) {
                    double averageClues = (double) totalClues / clueCounts.size();
                    double variance = 0.0;
                    for (int count : clueCounts) {
                        variance += Math.pow(count - averageClues, 2);
                    }
                    variance /= clueCounts.size();
                    double standardDeviation = Math.sqrt(variance);
                    double balanceScore = 1.0 - (standardDeviation / averageClues);
                    if (balanceScore < 0) balanceScore = 0;
                    
                    // 评估平衡性等级
                    String balanceLevel;
                    if (balanceScore > 0.8) {
                        balanceLevel = "优秀";
                    } else if (balanceScore > 0.6) {
                        balanceLevel = "良好";
                    } else if (balanceScore > 0.4) {
                        balanceLevel = "一般";
                    } else {
                        balanceLevel = "较差";
                    }
                    
                    log.info("线索分布统计：");
                    log.info("总线索数：{}", totalClues);
                    log.info("平均每个场景线索数：{}", String.format("%.2f", averageClues));
                    log.info("线索分布标准差：{}", String.format("%.2f", standardDeviation));
                    log.info("线索分布平衡性得分：{}", String.format("%.2f", balanceScore));
                    log.info("线索分布平衡性等级：{}", balanceLevel);
                    
                    // 存储线索分布统计结果到上下文元数据
                    Map<String, Object> clueDistributionStats = new HashMap<>();
                    clueDistributionStats.put("totalClues", totalClues);
                    clueDistributionStats.put("averageClues", averageClues);
                    clueDistributionStats.put("standardDeviation", standardDeviation);
                    clueDistributionStats.put("balanceScore", balanceScore);
                    clueDistributionStats.put("balanceLevel", balanceLevel);
                    clueDistributionStats.put("sceneClueCounts", sceneClueCountMap);
                    context.getMetadata().put("clueDistributionStats", clueDistributionStats);
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
     * 计算两个字符串的相似度（简单的Levenshtein距离算法）
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 相似度值，范围0-1
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * 计算Levenshtein距离
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 编辑距离
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 确保每个场景至少有一个线索（兜底机制）
     * @param scenes 场景列表
     * @param sceneCluesMap 场景-线索映射
     * @param allClues 所有线索
     * @param scriptId 剧本ID
     */
    private static void ensureEachSceneHasClues(List<Scene> scenes, Map<Long, List<Clue>> sceneCluesMap, 
                                              List<Clue> allClues, Long scriptId) {
        if (scenes == null || scenes.isEmpty() || allClues == null || allClues.isEmpty()) {
            return;
        }
        
        // 收集所有未分配的线索
        List<Clue> unassignedClues = new ArrayList<>();
        for (Clue clue : allClues) {
            if (clue.getScriptId() != null && clue.getScriptId().equals(scriptId)) {
                boolean isAssigned = false;
                for (List<Clue> clues : sceneCluesMap.values()) {
                    if (clues.contains(clue)) {
                        isAssigned = true;
                        break;
                    }
                }
                if (!isAssigned) {
                    unassignedClues.add(clue);
                }
            }
        }
        
        // 为没有线索的场景分配未分配的线索
        for (Scene scene : scenes) {
            List<Clue> sceneClues = sceneCluesMap.get(scene.getId());
            if (sceneClues == null || sceneClues.isEmpty()) {
                if (!unassignedClues.isEmpty()) {
                    // 分配第一个未分配的线索
                    Clue clue = unassignedClues.remove(0);
                    sceneClues = new ArrayList<>();
                    sceneClues.add(clue);
                    sceneCluesMap.put(scene.getId(), sceneClues);
                    log.info("为场景 {} 分配兜底线索：{}", scene.getName(), clue.getName());
                } else {
                    // 如果没有未分配的线索，从所有线索中随机分配一个
                    Clue randomClue = allClues.get((int) (Math.random() * allClues.size()));
                    sceneClues = new ArrayList<>();
                    sceneClues.add(randomClue);
                    sceneCluesMap.put(scene.getId(), sceneClues);
                    log.info("为场景 {} 随机分配线索：{}", scene.getName(), randomClue.getName());
                }
            }
        }
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
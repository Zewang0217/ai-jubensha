package org.jubensha.aijubenshabackend.ai.workflow.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.workflow.state.ScriptCreationState;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.JsonValidationUtil;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class SceneClueNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCRIPT_CREATION_STATE_KEY = "scriptCreationState";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：场景与线索填充");
            
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
                }
                
                // 获取或创建 ScriptCreationState
                ScriptCreationState creationState = getOrCreateScriptCreationState(context);
                
                // 获取大纲 JSON
                String outlineJson = creationState.getOutlineJson();
                if (outlineJson == null || outlineJson.isEmpty()) {
                    log.error("大纲 JSON 为空");
                    throw new IllegalArgumentException("大纲 JSON 不能为空");
                }
                
                log.info("大纲 JSON 长度: {}", outlineJson.length());
                
                // 获取 AI 服务实例
                ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(
                        ScriptGenerateServiceFactory.class);
                
                if (scriptGenerateServiceFactory == null) {
                    throw new IllegalStateException("ScriptGenerateServiceFactory 实例获取失败");
                }
                
                // 生成临时 ID 用于服务实例
                Long tempId = Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits());
                ScriptGenerateService generateService = scriptGenerateServiceFactory.getService(tempId);
                
                if (generateService == null) {
                    throw new IllegalStateException("获取剧本生成服务失败");
                }
                
                // 生成场景和线索（带重试机制）
                log.info("开始生成场景和线索");
                String mechanicsJson = JsonValidationUtil.generateWithRetry(() -> {
                    AtomicReference<String> mechanicsBuilder = new AtomicReference<>("");
                    CompletableFuture<Void> streamFuture = new CompletableFuture<>();
                    
                    generateService.generateMechanics(outlineJson)
                            .doOnNext(chunk -> {
                                mechanicsBuilder.updateAndGet(current -> current + chunk);
                            })
                            .doOnComplete(() -> {
                                log.info("场景和线索生成完成，总长度: {}", mechanicsBuilder.get().length());
                                streamFuture.complete(null);
                            })
                            .doOnError(error -> {
                                log.error("场景和线索生成失败: {}", error.getMessage(), error);
                                streamFuture.completeExceptionally(error);
                            })
                            .subscribe();
                    
                    // 等待流式生成完成
                    streamFuture.orTimeout(600, java.util.concurrent.TimeUnit.SECONDS).join();
                    
                    String generatedJson = mechanicsBuilder.get();
                    
                    // 验证生成结果
                    if (generatedJson == null || generatedJson.isEmpty()) {
                        throw new IllegalStateException("场景和线索生成结果为空");
                    }
                    
                    return generatedJson;
                });
                
                // 验证场景和线索结构，并确保线索的location字段严格使用场景名称
                try {
                    JsonNode rootNode = objectMapper.readTree(mechanicsJson);
                    log.debug("场景和线索 JSON 格式验证通过");
                    
                    // 验证场景和线索结构
                    JsonNode scenesNode = rootNode.path("scenes");
                    JsonNode cluesNode = rootNode.path("clues");
                    
                    if (!scenesNode.isArray()) {
                        log.error("场景列表格式错误");
                        throw new IllegalStateException("场景列表格式错误");
                    }
                    
                    if (!cluesNode.isArray()) {
                        log.error("线索列表格式错误");
                        throw new IllegalStateException("线索列表格式错误");
                    }
                    
                    log.info("生成场景 {} 个，线索 {} 个", scenesNode.size(), cluesNode.size());
                    
                    // 提取所有场景名称
                    java.util.List<String> sceneNames = new java.util.ArrayList<>();
                    for (JsonNode sceneNode : scenesNode) {
                        String sceneName = sceneNode.path("name").asText();
                        if (!sceneName.isEmpty()) {
                            sceneNames.add(sceneName);
                        }
                    }
                    
                    log.info("提取到场景名称: {}", sceneNames);
                    
                    // 验证每个场景的线索数量是否合理（4-6个）
                    com.fasterxml.jackson.databind.node.ObjectNode rootObjectNode = (com.fasterxml.jackson.databind.node.ObjectNode) rootNode;
                    com.fasterxml.jackson.databind.node.ArrayNode scenesArrayNode = (com.fasterxml.jackson.databind.node.ArrayNode) rootObjectNode.get("scenes");
                    
                    for (int i = 0; i < scenesArrayNode.size(); i++) {
                        com.fasterxml.jackson.databind.node.ObjectNode sceneObjectNode = (com.fasterxml.jackson.databind.node.ObjectNode) scenesArrayNode.get(i);
                        String sceneName = sceneObjectNode.path("name").asText();
                        JsonNode sceneCluesNode = sceneObjectNode.path("clues");
                        int sceneClueCount = sceneCluesNode.size();
                        
                        log.info("场景: {}，当前线索数量: {}", sceneName, sceneClueCount);
                        
                        // 确保每个场景有4-6个线索
                        if (sceneClueCount < 4) {
                            log.info("场景 {} 线索数量不足，需要添加线索", sceneName);
                            // 这里可以添加逻辑来为线索不足的场景生成更多线索
                        } else if (sceneClueCount > 6) {
                            log.info("场景 {} 线索数量过多，需要减少线索", sceneName);
                            // 这里可以添加逻辑来减少线索过多的场景的线索数量
                        }
                    }
                    
                    // 验证线索的location字段是否与场景名称匹配
                    int matchedClues = 0;
                    int unmatchedClues = 0;
                    for (JsonNode clueNode : cluesNode) {
                        String location = clueNode.path("location").asText();
                        if (!location.isEmpty()) {
                            boolean isMatched = false;
                            for (String sceneName : sceneNames) {
                                if (location.equals(sceneName)) {
                                    isMatched = true;
                                    matchedClues++;
                                    break;
                                }
                            }
                            if (!isMatched) {
                                unmatchedClues++;
                                log.warn("线索 location '{}' 与任何场景名称不匹配", location);
                            }
                        } else {
                            unmatchedClues++;
                            log.warn("线索缺少 location 字段");
                        }
                    }
                    
                    log.info("线索匹配情况: 匹配 {} 个，未匹配 {} 个", matchedClues, unmatchedClues);
                    
                    // 如果有未匹配的线索，尝试修复
                    if (unmatchedClues > 0) {
                        log.info("开始修复未匹配的线索 location 字段");
                        
                        // 使用之前定义的 rootObjectNode
                        com.fasterxml.jackson.databind.node.ArrayNode cluesArrayNode = (com.fasterxml.jackson.databind.node.ArrayNode) rootObjectNode.get("clues");
                        
                        // 修复每个未匹配的线索
                        for (int i = 0; i < cluesArrayNode.size(); i++) {
                            com.fasterxml.jackson.databind.node.ObjectNode clueObjectNode = (com.fasterxml.jackson.databind.node.ObjectNode) cluesArrayNode.get(i);
                            String location = clueObjectNode.path("location").asText();
                            
                            if (location.isEmpty() || !sceneNames.contains(location)) {
                                // 尝试找到最相似的场景名称
                                String bestMatch = findBestMatchingScene(location, sceneNames);
                                if (bestMatch != null) {
                                    clueObjectNode.put("location", bestMatch);
                                    log.info("修复线索 location: '{}' -> '{}'", location, bestMatch);
                                } else if (!sceneNames.isEmpty()) {
                                    // 如果没有找到相似的，使用第一个场景名称
                                    String firstSceneName = sceneNames.get(0);
                                    clueObjectNode.put("location", firstSceneName);
                                    log.info("修复线索 location: '{}' -> '{}' (使用第一个场景)", location, firstSceneName);
                                }
                            }
                        }
                        
                        // 将修复后的 ObjectNode 转换回 JSON 字符串
                        mechanicsJson = objectMapper.writeValueAsString(rootObjectNode);
                        log.info("线索 location 字段修复完成，修复后 JSON 长度: {}", mechanicsJson.length());
                    }
                    
                } catch (Exception e) {
                    log.error("场景和线索 JSON 结构验证失败: {}", e.getMessage());
                    throw new IllegalStateException("场景和线索 JSON 结构错误", e);
                }
                
                // 保存场景和线索到状态
                creationState.setMechanicsJson(mechanicsJson);
                context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
                log.info("场景和线索已保存到状态，长度: {}", mechanicsJson.length());
                
            } catch (Exception e) {
                log.error("场景与线索填充节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("场景与线索填充失败: " + e.getMessage());
                    context.setSuccess(false);
                }
            }
            
            return WorkflowContext.saveContext(WorkflowContext.getContext(state));
        });
    }
    
    /**
     * 获取或创建 ScriptCreationState
     */
    private static ScriptCreationState getOrCreateScriptCreationState(WorkflowContext context) {
        if (context.getMetadata() == null) {
            context.setMetadata(new java.util.HashMap<>());
        }
        
        ScriptCreationState creationState = (ScriptCreationState) context.getMetadata().get(SCRIPT_CREATION_STATE_KEY);
        if (creationState == null) {
            creationState = new ScriptCreationState();
            context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
        }
        
        return creationState;
    }
    
    /**
     * 找到与给定位置字符串最相似的场景名称
     * @param location 线索的location字段
     * @param sceneNames 场景名称列表
     * @return 最相似的场景名称，如果没有找到则返回null
     */
    private static String findBestMatchingScene(String location, java.util.List<String> sceneNames) {
        if (location == null || location.isEmpty() || sceneNames == null || sceneNames.isEmpty()) {
            return null;
        }
        
        String bestMatch = null;
        double highestSimilarity = 0.0;
        
        for (String sceneName : sceneNames) {
            double similarity = calculateSimilarity(location, sceneName);
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatch = sceneName;
            }
        }
        
        // 只有当相似度超过0.6时才返回匹配结果
        return highestSimilarity > 0.6 ? bestMatch : null;
    }
    
    /**
     * 计算两个字符串的相似度（简单的编辑距离算法）
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 相似度，范围0-1
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
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
        
        int editDistance = dp[s1.length()][s2.length()];
        return 1.0 - (double) editDistance / maxLength;
    }
    

}
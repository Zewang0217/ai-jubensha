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
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CharacterGenNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCRIPT_CREATION_STATE_KEY = "scriptCreationState";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：角色扩充");
            
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
                
                // 解析大纲，提取角色列表
                JsonNode rootNode = objectMapper.readTree(outlineJson);
                JsonNode charactersNode = rootNode.path("characters");
                
                if (!charactersNode.isArray()) {
                    log.error("大纲中角色列表格式错误");
                    throw new IllegalStateException("大纲中角色列表格式错误");
                }
                
                List<Map<String, String>> characters = new ArrayList<>();
                for (JsonNode characterNode : charactersNode) {
                    Map<String, String> character = new HashMap<>();
                    character.put("name", characterNode.path("name").asText());
                    character.put("role", characterNode.path("role").asText());
                    character.put("archetype", characterNode.path("archetype").asText());
                    characters.add(character);
                }
                
                if (characters.isEmpty()) {
                    log.error("大纲中未提取到角色列表");
                    throw new IllegalStateException("大纲中未提取到角色列表");
                }
                
                log.info("提取到角色列表，共 {} 个角色", characters.size());
                for (Map<String, String> character : characters) {
                    log.info("角色: {} ({})", character.get("name"), character.get("role"));
                }
                
                // 获取 AI 服务实例
                ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(
                        ScriptGenerateServiceFactory.class);
                
                if (scriptGenerateServiceFactory == null) {
                    throw new IllegalStateException("ScriptGenerateServiceFactory 实例获取失败");
                }
                
                // 为每个角色并行生成详细小传
                Map<String, String> characterScripts = new HashMap<>();
                List<CompletableFuture<Void>> characterFutures = new ArrayList<>();
                
                for (Map<String, String> character : characters) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        String characterName = character.get("name");
                        String characterRole = character.get("role");
                        String characterArchetype = character.get("archetype");
                        
                        log.info("开始生成角色 {} 的详细小传", characterName);
                        
                        try {
                            // 生成临时 ID 用于服务实例
                            Long tempId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
                            ScriptGenerateService generateService = scriptGenerateServiceFactory.getService(tempId);
                            
                            if (generateService == null) {
                                throw new IllegalStateException("获取剧本生成服务失败");
                            }
                            
                            // 构建角色生成提示词
                            String userMessage = String.format("角色为:%s\n%s, 背景参考（上帝视角，不可泄露给玩家）如下:%s", 
                                    characterName, 
                                    characterRole + (characterArchetype != null && !characterArchetype.isEmpty() ? "，性格：" + characterArchetype : ""), 
                                    outlineJson);
                            
                            // 生成角色剧本
                            AtomicReference<String> characterScriptBuilder = new AtomicReference<>("");
                            CompletableFuture<Void> streamFuture = new CompletableFuture<>();
                            
                            generateService.generateCharacterMemoir(userMessage)
                                    .doOnNext(chunk -> {
                                        characterScriptBuilder.updateAndGet(current -> current + chunk);
//                                        log.debug("收到角色 {} 生成chunk，长度: {}", characterName, chunk.length());
                                    })
                                    .doOnComplete(() -> {
                                        log.info("角色 {} 生成完成，总长度: {}", characterName, characterScriptBuilder.get().length());
                                        streamFuture.complete(null);
                                    })
                                    .doOnError(error -> {
                                        log.error("角色 {} 生成失败: {}", characterName, error.getMessage(), error);
                                        streamFuture.completeExceptionally(error);
                                    })
                                    .subscribe();
                            
                            // 等待流式生成完成
                            streamFuture.orTimeout(600, java.util.concurrent.TimeUnit.SECONDS).join();
                            
                            String characterScript = characterScriptBuilder.get();
                            
                            // 验证生成结果
                            if (characterScript == null || characterScript.isEmpty()) {
                                throw new IllegalStateException("角色 " + characterName + " 生成结果为空");
                            }
                            
                            // 预处理 JSON
                            characterScript = preprocessJson(characterScript);
                            
                            // 验证 JSON 格式
                            try {
                                objectMapper.readTree(characterScript);
                                log.debug("角色 {} JSON 格式验证通过", characterName);
                            } catch (Exception e) {
                                log.error("角色 {} JSON 格式验证失败: {}", characterName, e.getMessage());
                                throw new IllegalStateException("角色 " + characterName + " JSON 格式错误", e);
                            }
                            
                            // 保存角色剧本
                            synchronized (characterScripts) {
                                characterScripts.put(characterName, characterScript);
                            }
                            
                            log.info("角色 {} 详细小传已保存", characterName);
                            
                        } catch (Exception e) {
                            log.error("生成角色 {} 详细小传失败: {}", characterName, e.getMessage(), e);
                            throw new RuntimeException("生成角色 " + characterName + " 详细小传失败", e);
                        }
                    });
                    
                    characterFutures.add(future);
                }
                
                // 等待所有角色生成完成
                log.info("等待所有角色生成完成");
                CompletableFuture.allOf(characterFutures.toArray(new CompletableFuture[0]))
                        .orTimeout(3600, java.util.concurrent.TimeUnit.SECONDS)
                        .join();
                
                // 验证所有角色是否生成成功
                if (characterScripts.size() != characters.size()) {
                    log.error("角色生成不完整，预期 {} 个角色，实际生成 {} 个", characters.size(), characterScripts.size());
                    throw new IllegalStateException("角色生成不完整");
                }
                
                log.info("所有角色详细小传生成完成，共 {} 个角色", characterScripts.size());
                
                // 保存角色剧本到状态
                creationState.setCharacterScripts(characterScripts);
                context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
                log.info("角色剧本已保存到状态");
                
            } catch (Exception e) {
                log.error("角色扩充节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("角色扩充失败: " + e.getMessage());
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
     * 预处理 JSON，移除代码块标记并修复不完整的 JSON
     */
    private static String preprocessJson(String json) {
        if (json == null || json.isEmpty()) {
            log.warn("输入 JSON 为空");
            return "{}";
        }
        
        // 移除开头的代码块标记
        if (json.startsWith("```json")) {
            json = json.substring(7);
            log.debug("移除了开头的 JSON 代码块标记");
        } else if (json.startsWith("```")) {
            json = json.substring(3);
            log.debug("移除了开头的代码块标记");
        }
        
        // 移除结尾的代码块标记
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
            log.debug("移除了结尾的代码块标记");
        }
        
        // 去除首尾空白
        json = json.trim();
        log.debug("去除首尾空白后的 JSON 长度: {}", json.length());
        
        // 移除开头的前言文字，只保留 JSON 内容
        int jsonStartIndex = json.indexOf('{');
        if (jsonStartIndex != -1) {
            json = json.substring(jsonStartIndex);
            log.debug("移除了开头的前言文字，JSON 长度: {}", json.length());
        }
        
        return json;
    }
}
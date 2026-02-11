package org.jubensha.aijubenshabackend.ai.workflow.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.workflow.state.ScriptCreationState;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class OutlineNode {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCRIPT_CREATION_STATE_KEY = "scriptCreationState";

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：大纲生成");
            
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
                }
                
                // 获取用户提示词
                String userPrompt = context.getOriginalPrompt();
                if (userPrompt == null || userPrompt.isEmpty()) {
                    log.error("用户提示词为空");
                    throw new IllegalArgumentException("用户提示词不能为空");
                }
                
                log.info("用户提示词: {}", userPrompt);
                
                // 获取或创建 ScriptCreationState
                ScriptCreationState creationState = getOrCreateScriptCreationState(context);
                creationState.setUserPrompt(userPrompt);
                
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
                
                // 生成大纲
                log.info("开始生成大纲");
                AtomicReference<String> outlineBuilder = new AtomicReference<>("");
                CompletableFuture<Void> streamFuture = new CompletableFuture<>();
                
                generateService.generateWorldOutline(userPrompt)
                        .doOnNext(chunk -> {
                            outlineBuilder.updateAndGet(current -> current + chunk);
//                            log.debug("收到大纲生成chunk，长度: {}", chunk.length());
                        })
                        .doOnComplete(() -> {
                            log.info("大纲生成完成，总长度: {}", outlineBuilder.get().length());
                            streamFuture.complete(null);
                        })
                        .doOnError(error -> {
                            log.error("大纲生成失败: {}", error.getMessage(), error);
                            streamFuture.completeExceptionally(error);
                        })
                        .subscribe();
                
                // 等待流式生成完成
                streamFuture.orTimeout(300, java.util.concurrent.TimeUnit.SECONDS).join();
                
                String outlineJson = outlineBuilder.get();
                
                // 验证生成结果
                if (outlineJson == null || outlineJson.isEmpty()) {
                    throw new IllegalStateException("大纲生成结果为空");
                }
                
                // 预处理 JSON
                outlineJson = preprocessJson(outlineJson);
                
                // 验证 JSON 格式
                try {
                    objectMapper.readTree(outlineJson);
                    log.debug("大纲 JSON 格式验证通过");
                } catch (Exception e) {
                    log.error("大纲 JSON 格式验证失败: {}", e.getMessage());
                    throw new IllegalStateException("大纲 JSON 格式错误", e);
                }
                
                // 保存大纲到状态
                creationState.setOutlineJson(outlineJson);
                context.getMetadata().put(SCRIPT_CREATION_STATE_KEY, creationState);
                log.info("大纲已保存到状态，长度: {}", outlineJson.length());
                
            } catch (Exception e) {
                log.error("大纲生成节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("大纲生成失败: " + e.getMessage());
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
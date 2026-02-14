package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.ImageGenerationService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.service.script.ScriptService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 剧本封面生成节点
 * 在剧本生成完成后，根据剧本内容生成AI封面图片
 */
@Slf4j
public class CoverImageGeneratorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("CoverImageGeneratorNode: {}", context);
            log.info("执行节点：封面图片生成");

            // 检查前置条件
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法生成封面");
                context.setErrorMessage("剧本ID为空，无法生成封面");
                return WorkflowContext.saveContext(context);
            }

            String scriptName = context.getScriptName();
            if (scriptName == null || scriptName.isEmpty()) {
                log.error("剧本名称为空，无法生成封面");
                context.setErrorMessage("剧本名称为空，无法生成封面");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 获取服务实例
                ImageGenerationService imageGenerationService = SpringContextUtil.getBean(ImageGenerationService.class);
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);

                // 获取剧本信息
                Script script = scriptService.getScriptById(scriptId)
                        .orElseThrow(() -> new RuntimeException("找不到剧本: " + scriptId));

                // 异步生成封面图片
                CompletableFuture<String> imageFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("开始为剧本 {} 生成AI封面图片", scriptName);
                        
                        // 生成封面图片
                        String imageUrl = imageGenerationService.generateScriptCoverImage(
                                script.getName(),
                                script.getDescription(),
                                context.getScriptType() != null ? context.getScriptType() : "推理本"
                        );
                        
                        log.info("剧本 {} 封面图片生成完成: {}", scriptName, imageUrl);
                        return imageUrl;
                        
                    } catch (Exception e) {
                        log.error("生成剧本 {} 封面图片失败: {}", scriptName, e.getMessage(), e);
                        // 返回默认图片作为fallback
                        return "https://picsum.photos/1024/1024?random=" + System.currentTimeMillis();
                    }
                });

                // 等待图片生成完成（设置超时时间）
                String coverImageUrl = imageFuture.get(30, java.util.concurrent.TimeUnit.SECONDS);

                // 更新剧本的封面图片URL
                script.setCoverImageUrl(coverImageUrl);
                script.setUpdateTime(LocalDateTime.now());
                scriptService.updateScript(scriptId, script);

                // 更新WorkflowContext
                context.setCoverImageUrl(coverImageUrl);
                context.setCurrentStep("封面生成");
                context.setSuccess(true);
                log.info("剧本 {} 封面生成节点执行完成", scriptName);

            } catch (Exception e) {
                log.error("封面生成节点执行失败: {}", e.getMessage(), e);
                context.setErrorMessage("封面生成失败: " + e.getMessage());
                context.setSuccess(false);
                
                // 即使封面生成失败，也不影响整个流程，继续执行
                context.setCoverImageUrl("https://picsum.photos/1024/1024?random=" + System.currentTimeMillis());
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 创建流式封面生成节点
     */
    public static AsyncNodeAction<MessagesState<String>> createStream() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("CoverImageGeneratorNode (Stream): {}", context);
            log.info("执行节点：流式封面图片生成");

            // 检查前置条件
            Long scriptId = context.getScriptId();
            if (scriptId == null) {
                log.error("剧本ID为空，无法生成封面");
                context.setErrorMessage("剧本ID为空，无法生成封面");
                return WorkflowContext.saveContext(context);
            }

            String scriptName = context.getScriptName();
            if (scriptName == null || scriptName.isEmpty()) {
                log.error("剧本名称为空，无法生成封面");
                context.setErrorMessage("剧本名称为空，无法生成封面");
                return WorkflowContext.saveContext(context);
            }

            try {
                // 获取服务实例
                ImageGenerationService imageGenerationService = SpringContextUtil.getBean(ImageGenerationService.class);
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);

                // 获取剧本信息
                Script script = scriptService.getScriptById(scriptId)
                        .orElseThrow(() -> new RuntimeException("找不到剧本: " + scriptId));

                // 使用流式生成
                CompletableFuture<String> imageFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("开始流式生成剧本 {} 的AI封面图片", scriptName);
                        
                        StringBuilder imageUrlBuilder = new StringBuilder();
                        
                        // 订阅流式生成结果
                        imageGenerationService.generateScriptCoverImageStream(
                                script.getName(),
                                script.getDescription(),
                                context.getScriptType() != null ? context.getScriptType() : "推理本"
                        ).doOnNext(chunk -> {
                            // 处理每个生成的chunk（这里主要是进度信息）
                            log.debug("收到封面生成进度: {}", chunk);
                        }).doOnComplete(() -> {
                            log.info("流式封面生成完成");
                        }).doOnError(error -> {
                            log.error("流式封面生成出错: {}", error.getMessage(), error);
                        }).subscribe();
                        
                        // 由于当前API限制，我们还是使用同步方式获取最终结果
                        String imageUrl = imageGenerationService.generateScriptCoverImage(
                                script.getName(),
                                script.getDescription(),
                                context.getScriptType() != null ? context.getScriptType() : "推理本"
                        );
                        
                        return imageUrl;
                        
                    } catch (Exception e) {
                        log.error("流式生成剧本 {} 封面图片失败: {}", scriptName, e.getMessage(), e);
                        return "https://picsum.photos/1024/1024?random=" + System.currentTimeMillis();
                    }
                });

                // 等待图片生成完成
                String coverImageUrl = imageFuture.get(30, java.util.concurrent.TimeUnit.SECONDS);

                // 更新剧本的封面图片URL
                script.setCoverImageUrl(coverImageUrl);
                script.setUpdateTime(LocalDateTime.now());
                scriptService.updateScript(scriptId, script);

                // 更新WorkflowContext
                context.setCoverImageUrl(coverImageUrl);
                context.setCurrentStep("流式封面生成");
                context.setSuccess(true);
                log.info("剧本 {} 流式封面生成节点执行完成", scriptName);

            } catch (Exception e) {
                log.error("流式封面生成节点执行失败: {}", e.getMessage(), e);
                context.setErrorMessage("流式封面生成失败: " + e.getMessage());
                context.setSuccess(false);
                context.setCoverImageUrl("https://picsum.photos/1024/1024?random=" + System.currentTimeMillis());
            }

            return WorkflowContext.saveContext(context);
        });
    }
}
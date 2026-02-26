package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.ScriptCreationWorkflow;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 新剧本生成工作流节点
 * 用于在现有工作流中集成新的剧本生成工作流
 */
@Slf4j
public class ScriptCreationWorkflowNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：新剧本生成工作流");
            WorkflowContext finalContext = null;
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
                }
                
                // 检查是否使用现有剧本
                Boolean createNewScript = context.getCreateNewScript();
                Long existingScriptId = context.getExistingScriptId();
                log.info("检查使用现有剧本条件：createNewScript={}, existingScriptId={}", createNewScript, existingScriptId);
                
                // 如果createNewScript为false且existingScriptId不为null，则验证剧本是否存在
                if (createNewScript != null && !createNewScript && existingScriptId != null) {
                    // 验证剧本是否存在
                    try {
                        var scriptService = SpringContextUtil.getBean(org.jubensha.aijubenshabackend.service.script.ScriptService.class);
                        if (scriptService != null) {
                            var scriptOpt = scriptService.getScriptById(existingScriptId);
                            if (scriptOpt.isPresent()) {
                                log.info("使用现有剧本，跳过新剧本生成工作流，剧本ID: {}", existingScriptId);
                                context.setScriptId(existingScriptId);
                                context.setCurrentStep("使用现有剧本");
                                context.setSuccess(true);
                                context.setStartTime(java.time.LocalDateTime.now());
                                return WorkflowContext.saveContext(context);
                            } else {
                                log.warn("剧本不存在，继续执行剧本生成工作流，剧本ID: {}", existingScriptId);
                            }
                        } else {
                            log.warn("ScriptService 获取失败，继续执行剧本生成工作流");
                        }
                    } catch (Exception e) {
                        log.warn("验证剧本存在性失败: {}", e.getMessage(), e);
                        // 验证失败，继续执行剧本生成工作流
                    }
                }
                
                // 获取原始提示词
                String originalPrompt = context.getOriginalPrompt();
                if (originalPrompt == null || originalPrompt.isEmpty()) {
                    throw new IllegalArgumentException("原始提示词为空");
                }
                
                // 创建并执行新的剧本生成工作流
                ScriptCreationWorkflow scriptCreationWorkflow = new ScriptCreationWorkflow();
                var workflow = scriptCreationWorkflow.buildGraph().compile();
                
                // 执行工作流并跟踪最终上下文
                finalContext = null;
                for (var step : workflow.stream(
                        WorkflowContext.saveContext(context)
                )) {
                    log.info("新剧本生成工作流步骤：{} 完成", step.node());
                    WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                    finalContext = currentContext; // 保存最后一个上下文
                    if (currentContext != null && currentContext.getErrorMessage() != null) {
                        throw new RuntimeException("新剧本生成工作流失败：" + currentContext.getErrorMessage());
                    }
                }
                
                // 检查最终上下文
                if (finalContext != null && finalContext.getSuccess() != null && finalContext.getSuccess()) {
                    log.info("新剧本生成工作流执行成功，剧本ID: {}", finalContext.getScriptId());
                    WorkflowContext.saveContext(finalContext);
                } else {
                    throw new RuntimeException("新剧本生成工作流执行失败");
                }
                
            } catch (Exception e) {
                log.error("新剧本生成工作流节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("新剧本生成失败: " + e.getMessage());
                    context.setSuccess(false);
                }
            }
            
            return WorkflowContext.saveContext(finalContext);
        });
    }
}

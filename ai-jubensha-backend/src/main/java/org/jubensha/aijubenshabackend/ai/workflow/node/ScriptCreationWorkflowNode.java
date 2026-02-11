package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.ScriptCreationWorkflow;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;

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
            
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
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
                WorkflowContext finalContext = null;
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
            
            return WorkflowContext.saveContext(WorkflowContext.getContext(state));
        });
    }
}

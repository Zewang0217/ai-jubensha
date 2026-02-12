package org.jubensha.aijubenshabackend.ai.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.workflow.ScriptCreationWorkflow;

import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;

import java.time.LocalDateTime;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ScriptCreationAdapterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行节点：剧本创建适配器");
            
            try {
                // 获取现有上下文
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context == null) {
                    throw new IllegalArgumentException("WorkflowContext 为空");
                }
                
                // 检查是否使用现有剧本
                Boolean createNewScript = context.getCreateNewScript();
                Long existingScriptId = context.getExistingScriptId();
                
                if (createNewScript != null && !createNewScript && existingScriptId != null) {
                    log.info("使用现有剧本，跳过新剧本生成流程");
                    context.setScriptId(existingScriptId);
                    context.setCurrentStep("使用现有剧本");
                    context.setSuccess(true);
                    context.setStartTime(LocalDateTime.now());
                    return WorkflowContext.saveContext(context);
                }
                
                // 获取用户提示词
                String originalPrompt = context.getOriginalPrompt();
                if (originalPrompt == null || originalPrompt.isEmpty()) {
                    throw new IllegalArgumentException("用户提示词为空");
                }
                
                log.info("用户提示词: {}", originalPrompt);
                

                
                // 获取 ScriptCreationWorkflow 实例
                ScriptCreationWorkflow scriptCreationWorkflow = SpringContextUtil.getBean(ScriptCreationWorkflow.class);
                if (scriptCreationWorkflow == null) {
                    throw new IllegalStateException("ScriptCreationWorkflow 实例获取失败");
                }
                
                // 编译并执行新工作流
                log.info("开始执行新的剧本生成流程");
                CompiledGraph<MessagesState<String>> workflow = scriptCreationWorkflow.buildGraph().compile();
                
                WorkflowContext finalContext = null;
                int stepCounter = 1;
                
                for (NodeOutput<MessagesState<String>> step : workflow.stream(
                        WorkflowContext.saveContext(context)
                )) {
                    log.info("--- 剧本生成流程第{}步：{}完成 ---", stepCounter, step.node());
                    finalContext = WorkflowContext.getContext(step.state());
                    stepCounter++;
                }
                
                if (finalContext == null) {
                    throw new IllegalStateException("剧本生成流程执行失败，最终上下文为空");
                }
                
                // 获取最终剧本 ID
                Long finalScriptId = finalContext.getScriptId();
                if (finalScriptId == null) {
                    throw new IllegalStateException("剧本生成流程执行失败，最终剧本 ID 为空");
                }
                
                log.info("新剧本生成流程执行完成，最终剧本 ID: {}", finalScriptId);
                
                // 更新现有上下文
                context.setScriptId(finalScriptId);
                context.setCurrentStep("新剧本生成流程完成");
                context.setSuccess(true);
                context.setStartTime(LocalDateTime.now());
                
                log.info("已更新 WorkflowContext，剧本 ID: {}", finalScriptId);
                
            } catch (Exception e) {
                log.error("剧本创建适配器节点执行失败: {}", e.getMessage(), e);
                WorkflowContext context = WorkflowContext.getContext(state);
                if (context != null) {
                    context.setErrorMessage("剧本生成失败: " + e.getMessage());
                    context.setSuccess(false);
                }
            }
            
            return WorkflowContext.saveContext(WorkflowContext.getContext(state));
        });
    }
}
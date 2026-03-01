package org.jubensha.aijubenshabackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.workflow.ScriptCreationWorkflow;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 剧本生成控制器
 * 用于处理剧本生成工作流的请求
 */
@Slf4j
@RestController
@RequestMapping("/api/script-creation")
public class ScriptCreationController {

    private final ScriptCreationWorkflow scriptCreationWorkflow;

    public ScriptCreationController(ScriptCreationWorkflow scriptCreationWorkflow) {
        this.scriptCreationWorkflow = scriptCreationWorkflow;
    }

    /**
     * 启动剧本生成工作流
     *
     * @param request 包含原始提示词的请求
     * @return 工作流执行结果
     */
    @PostMapping("/start-workflow")
    public ResponseEntity<?> startScriptCreationWorkflow(@RequestBody Map<String, Object> request) {
        try {
            // 获取原始提示词
            String originalPrompt = (String) request.get("originalPrompt");
            if (originalPrompt == null || originalPrompt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "originalPrompt is required"));
            }

            // 获取流式生成参数
            Boolean useStreaming = (Boolean) request.getOrDefault("useStreaming", false);
            log.info("使用流式生成: {}", useStreaming);

            // 创建工作流上下文
            WorkflowContext context = new WorkflowContext();
            context.setOriginalPrompt(originalPrompt);
            context.setStartTime(java.time.LocalDateTime.now());

            // 构建并执行工作流
            var workflow = scriptCreationWorkflow.buildGraph().compile();

            // 执行工作流并跟踪最终上下文
            WorkflowContext finalContext = null;
            for (var step : workflow.stream(
                    WorkflowContext.saveContext(context)
            )) {
                log.info("剧本生成工作流步骤：{} 完成", step.node());
                WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                finalContext = currentContext; // 保存最后一个上下文
                if (currentContext != null && currentContext.getErrorMessage() != null) {
                    throw new RuntimeException("剧本生成工作流失败：" + currentContext.getErrorMessage());
                }
            }

            // 检查最终上下文
            if (finalContext != null && finalContext.getSuccess() != null && finalContext.getSuccess()) {
                log.info("剧本生成工作流执行成功，剧本ID: {}", finalContext.getScriptId());
            } else {
                throw new RuntimeException("剧本生成工作流执行失败");
            }

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scriptId", finalContext.getScriptId());
            response.put("scriptName", finalContext.getScriptName());
            response.put("currentStep", finalContext.getCurrentStep());
            response.put("coverImageUrl", finalContext.getCoverImageUrl());
            response.put("useStreaming", useStreaming);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启动剧本生成工作流失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start script creation workflow", "message", e.getMessage()));
        }
    }
}

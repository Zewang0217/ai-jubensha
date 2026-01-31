package org.jubensha.aijubenshabackend.ai.workflow.node;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.factory.ScriptGenerateServiceFactory;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.jubensha.aijubenshabackend.service.script.ScriptServiceImpl;
import java.time.LocalDateTime;

/**
 * 剧本生成节点
 *
 * @author zewang
 * @version 1.0
 * @date 2026-01-30 20:29
 * @since 2026
 */

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ScriptGeneratorNode {


    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("ScriptGeneratorNode: {}", context);
            log.info("执行节点：剧本生成");
            // 构建用户消息（包含原始提示词合可能的错误修复信息）
            String userMessage = buildUserMessage(context);

            // 获取 AI 服务实例
            ScriptGenerateServiceFactory scriptGenerateServiceFactory = SpringContextUtil.getBean(
                ScriptGenerateServiceFactory.class);
            log.info("获取 AI 服务实例");
            log.info("开始生成剧本");
            // 用当前时间生成一个scriptId
            Long scriptId = System.currentTimeMillis();
            ScriptGenerateService generateServiceFactoryService = scriptGenerateServiceFactory.getService(scriptId);
            String script = generateServiceFactoryService.generateScript(userMessage);

            log.info("剧本生成完成");
            Script newScript = Script.builder()
                .id(scriptId)
                .name(context.getOriginalPrompt())
                .description(script)
                .build();

            // 将script保存到数据库中
            try {
                ScriptService scriptService = SpringContextUtil.getBean(ScriptService.class);
                Script savedScript = scriptService.createScript(newScript);
                log.info("剧本已保存到数据库，ID: {}", savedScript.getId());
                context.setScriptId(scriptId);
            } catch (Exception e) {
                log.error("保存剧本到数据库失败: {}", e.getMessage(), e);
            }
            
            context.setCurrentStep("剧本生成");
            // 存储剧本详细信息
            context.setScriptName(context.getOriginalPrompt());
            context.setScriptType("推理本"); // 默认类型，后续可从用户输入中解析
            context.setScriptDifficulty("中等"); // 默认难度，后续可从用户输入中解析
            context.setModelOutput(script);
            context.setSuccess(true);
            context.setStartTime(LocalDateTime.now());
            
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 构造用户信息
     * TODO：完善userMessage
     */
    private static String buildUserMessage(WorkflowContext context) {
        StringBuilder message = new StringBuilder();
        message.append("用户需求：").append(context.getOriginalPrompt()).append("\n");
        message.append("生成要求：\n");
        message.append("- 包含完整的角色设定\n");
        message.append("- 包含详细的场景描述\n");
        message.append("- 包含合理的剧情发展\n");
        message.append("- 适合剧本杀游戏使用\n");

        if (context.getErrorMessage() != null) {
            message.append("\n之前的错误：").append(context.getErrorMessage()).append("\n");
            message.append("请避免类似错误，重新生成剧本。");
        }

        return message.toString();
    }
}

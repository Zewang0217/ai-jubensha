package org.jubensha.aijubenshabackend.ai.tools;


/**
 * 退出工具调用的工具
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-30 22:12
 * @since 2026
 */

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.stereotype.Component;

/**
 * 告诉 AI 要退出的工具
 */
@Slf4j
@Component
public class ExitTool extends BaseTool {

    @Override
    public String getToolName() {
        return "exit";
    }

    @Override
    public String getDisplayName() {
        return "退出工具调用";
    }

    /**
     * 退出工具调用
     * 当任务完成或无需继续使用工具时调用此方法
     *
     * @param args 可选参数（兼容AI模型可能传入空参数的情况）
     * @return 退出确认信息
     */
    @Tool("当任务已完成或无需继续调用工具时，使用此工具退出操作，防止循环")
    public String exit(Object args) {
        log.info("AI 请求退出工具调用，参数: {}", args);
        return "不要继续调用工具，可以输出最终结果了";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[执行结束]\n\n";
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        // 所有Agent都可以使用退出工具
        return ToolPermissionLevel.PLAYER;
    }
}

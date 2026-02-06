package org.jubensha.aijubenshabackend.ai.tools.permission;

import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.springframework.stereotype.Component;

/**
 * Judge Agent专用工具管理器
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */
@Component
public class JudgeAgentToolManager extends AgentToolManager {
    
    /**
     * 构造函数
     *
     * @param toolManager 基础工具管理器
     */
    public JudgeAgentToolManager(ToolManager toolManager) {
        super(toolManager, AgentType.JUDGE);
    }
}

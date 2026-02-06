package org.jubensha.aijubenshabackend.ai.tools.permission;

import org.jubensha.aijubenshabackend.ai.tools.BaseTool;
import org.jubensha.aijubenshabackend.ai.tools.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent专用工具管理器
 * 根据Agent类型和权限级别过滤工具
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */
public abstract class AgentToolManager {
    
    protected final ToolManager toolManager;
    protected final AgentType agentType;
    
    /**
     * 构造函数
     *
     * @param toolManager 基础工具管理器
     * @param agentType Agent类型
     */
    public AgentToolManager(ToolManager toolManager, AgentType agentType) {
        this.toolManager = toolManager;
        this.agentType = agentType;
    }
    
    /**
     * 获取当前Agent可用的工具列表
     *
     * @return 可用工具列表
     */
    public BaseTool[] getAvailableTools() {
        BaseTool[] allTools = toolManager.getAllTools();
        List<BaseTool> availableTools = new ArrayList<>();
        
        for (BaseTool tool : allTools) {
            if (tool.hasPermission(agentType)) {
                availableTools.add(tool);
            }
        }
        
        return availableTools.toArray(new BaseTool[0]);
    }
    
    /**
     * 根据工具名称获取可用的工具实例
     *
     * @param toolName 工具名称
     * @return 工具实例，如果不可用返回null
     */
    public BaseTool getTool(String toolName) {
        BaseTool tool = toolManager.getTool(toolName);
        if (tool != null && tool.hasPermission(agentType)) {
            return tool;
        }
        return null;
    }
    
    /**
     * 获取Agent类型
     *
     * @return Agent类型
     */
    public AgentType getAgentType() {
        return agentType;
    }
}

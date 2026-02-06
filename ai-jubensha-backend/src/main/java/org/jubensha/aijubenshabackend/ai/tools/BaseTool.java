package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;

/**
 * 工具基类（抽象类）
 * 定义所有工具的通用接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-30 21:08
 * @since 2026
 */

public abstract class BaseTool {

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
    
    /**
     * 检查Agent是否有权限使用此工具
     *
     * @param agentType Agent类型
     * @return 是否有权限
     */
    public boolean hasPermission(AgentType agentType) {
        return getRequiredPermissionLevel(agentType).ordinal() <= agentType.getDefaultPermissionLevel().ordinal();
    }
    
    /**
     * 获取指定Agent类型使用此工具所需的权限级别
     *
     * @param agentType Agent类型
     * @return 所需权限级别
     */
    public abstract ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType);
    
    /**
     * 执行权限检查
     *
     * @param agentType Agent类型
     * @throws SecurityException 无权限时抛出异常
     */
    protected void checkPermission(AgentType agentType) throws SecurityException {
        if (!hasPermission(agentType)) {
            throw new SecurityException("Agent类型 " + agentType.getName() + " 没有权限使用工具 " + getDisplayName());
        }
    }
}

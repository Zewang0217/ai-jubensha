package org.jubensha.aijubenshabackend.ai.tools.permission;

/**
 * Agent类型枚举
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */
public enum AgentType {
    
    /**
     * DM Agent - 游戏主持人
     */
    DM("DM Agent", "游戏主持人", ToolPermissionLevel.ADMIN),
    
    /**
     * Player Agent - 玩家代理
     */
    PLAYER("Player Agent", "玩家代理", ToolPermissionLevel.PLAYER),
    
    /**
     * Judge Agent - 法官代理
     */
    JUDGE("Judge Agent", "法官代理", ToolPermissionLevel.ADMIN),
    
    /**
     * Summary Agent - 总结代理
     */
    SUMMARY("Summary Agent", "总结代理", ToolPermissionLevel.ADMIN);
    
    private final String name;
    private final String description;
    private final ToolPermissionLevel defaultPermissionLevel;
    
    AgentType(String name, String description, ToolPermissionLevel defaultPermissionLevel) {
        this.name = name;
        this.description = description;
        this.defaultPermissionLevel = defaultPermissionLevel;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ToolPermissionLevel getDefaultPermissionLevel() {
        return defaultPermissionLevel;
    }
}

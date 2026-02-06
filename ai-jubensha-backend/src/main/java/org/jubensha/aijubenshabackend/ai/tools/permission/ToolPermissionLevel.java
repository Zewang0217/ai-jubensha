package org.jubensha.aijubenshabackend.ai.tools.permission;

/**
 * 工具权限级别枚举
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */
public enum ToolPermissionLevel {
    
    /**
     * 管理员权限 - 可以访问所有信息
     */
    ADMIN("管理员权限", "可以访问所有信息"),
    
    /**
     * 玩家权限 - 只能访问与自身角色相关的信息
     */
    PLAYER("玩家权限", "只能访问与自身角色相关的信息"),
    
    /**
     * 受限权限 - 只能访问公开信息
     */
    LIMITED("受限权限", "只能访问公开信息"),
    
    /**
     * 无权限 - 不能访问任何信息
     */
    NONE("无权限", "不能访问任何信息");
    
    private final String name;
    private final String description;
    
    ToolPermissionLevel(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}

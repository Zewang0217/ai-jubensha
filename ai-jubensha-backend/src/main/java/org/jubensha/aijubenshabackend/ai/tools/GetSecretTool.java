package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取角色秘密工具
 * 用于AI获取角色的秘密信息，支持按角色ID查询
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class GetSecretTool extends BaseTool {

    // 临时存储角色秘密，实际项目中应从数据库或配置文件加载
    private final Map<Long, String> characterSecrets = new ConcurrentHashMap<>();

    @Override
    public String getToolName() {
        return "getSecret";
    }

    @Override
    public String getDisplayName() {
        return "获取角色秘密";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long characterId = arguments.getLong("characterId");

            log.debug("获取角色秘密，角色ID: {}", characterId);

            // 获取角色秘密
            String secret = getCharacterSecret(characterId);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("角色秘密:\n");
            result.append(secret);

            return result.toString();
        } catch (Exception e) {
            log.error("获取角色秘密失败: {}", e.getMessage(), e);
            return "获取角色秘密失败: " + e.getMessage();
        }
    }

    /**
     * 获取角色秘密
     * 实际项目中应从数据库或配置文件加载
     */
    private String getCharacterSecret(Long characterId) {
        // 临时实现，实际项目中应从数据库或配置文件加载
        return characterSecrets.getOrDefault(characterId, "该角色暂无秘密信息");
    }

    /**
     * 设置角色秘密
     * 实际项目中应通过数据库或配置文件设置
     */
    public void setCharacterSecret(Long characterId, String secret) {
        characterSecrets.put(characterId, secret);
        log.debug("设置角色秘密，角色ID: {}", characterId);
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public String execute(Long characterId) {
        return getCharacterSecret(characterId);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case JUDGE:
            case SUMMARY:
                // 管理员类型的Agent可以访问所有秘密
                return ToolPermissionLevel.ADMIN;
            case PLAYER:
                // 玩家Agent只能访问自身角色的秘密
                return ToolPermissionLevel.PLAYER;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

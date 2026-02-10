package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取角色秘密工具
 * 用于AI获取角色的秘密信息，支持按角色ID查询
 * <p>
 * 工具说明：
 * - 功能：获取指定角色的秘密信息
 * - 参数：
 *   - characterId：角色ID（必填）
 * - 返回格式：角色秘密的文本内容
 * - 权限：管理员类型Agent可访问所有角色秘密，玩家Agent只能访问自身角色秘密
 * <p>
 * 调用时机：
 * 1. 陈述阶段准备角色信息时，了解角色的隐藏背景
 * 2. 分析案件动机时，获取角色可能的犯罪动机
 * 3. 制定游戏策略时，了解角色需要隐藏的信息
 * 4. 与其他玩家互动时，基于角色秘密调整交流策略
 * <p>
 * 示例调用：
 * {
 *   "toolcall": {
 *     "thought": "需要了解角色的隐藏信息",
 *     "name": "getSecret",
 *     "params": {
 *       "characterId": "1"
 *     }
 *   }
 * }
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-02-10
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
    @Tool("获取角色秘密")
    public String executeGetSecret(@P("角色ID") Long characterId) {
        // 创建参数对象
        JSONObject arguments = new JSONObject();
        arguments.put("characterId", characterId);
        // 调用核心逻辑方法
        return generateToolExecutedResult(arguments);
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

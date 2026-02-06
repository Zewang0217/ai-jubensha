package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 获取角色时间线工具
 * 用于AI获取角色的时间线信息，支持按角色ID查询
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class GetTimelineTool extends BaseTool {

    @Autowired
    private RAGService ragService;

    @Override
    public String getToolName() {
        return "getTimeline";
    }

    @Override
    public String getDisplayName() {
        return "获取角色时间线";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long characterId = arguments.getLong("characterId");
            Long scriptId = arguments.getLong("scriptId");

            log.debug("获取角色时间线，角色ID: {}, 剧本ID: {}", characterId, scriptId);

            // 调用RAGService获取时间线
            List<Map<String, Object>> timeline = ragService.searchGlobalTimelineMemory(scriptId, characterId, "", 50);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("角色时间线:\n");
            
            for (Map<String, Object> event : timeline) {
                String content = (String) event.get("content");
                String timestamp = (String) event.get("timestamp");
                
                result.append(String.format("[%s]: %s\n", timestamp, content));
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取角色时间线失败: {}", e.getMessage(), e);
            return "获取角色时间线失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public List<Map<String, Object>> execute(Long scriptId, Long characterId) {
        return ragService.searchGlobalTimelineMemory(scriptId, characterId, "", 50);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case JUDGE:
            case SUMMARY:
                // 管理员类型的Agent可以访问所有时间线
                return ToolPermissionLevel.ADMIN;
            case PLAYER:
                // 玩家Agent只能访问与自身角色相关的时间线
                return ToolPermissionLevel.PLAYER;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

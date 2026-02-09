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
 * <p>
 * 工具说明：
 * - 功能：获取指定角色的时间线事件
 * - 参数：
 *   - characterId：角色ID（必填）
 *   - scriptId：剧本ID（必填）
 * - 返回格式：按时间顺序排列的格式化时间线
 * - 权限：管理员类型Agent可访问所有时间线，玩家Agent只能访问自身角色时间线
 * <p>
 * 示例调用：
 * {
 *   "toolcall": {
 *     "thought": "需要了解角色的时间线",
 *     "name": "getTimeline",
 *     "params": {
 *       "characterId": "1",
 *       "scriptId": "1"
 *     }
 *   }
 * }
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-02-09
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
            List<Map<String, Object>> timeline = ragService.searchGlobalTimelineMemory(scriptId, characterId, "", 30);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("⏰ 角色时间线（共").append(timeline.size()).append("个事件）:\n\n");
            
            for (Map<String, Object> event : timeline) {
                String content = (String) event.getOrDefault("content", "");
                String timestamp = (String) event.getOrDefault("timestamp", "未知时间");
                
                result.append(String.format("[%s] %s\n", timestamp, content));
            }

            if (timeline.isEmpty()) {
                result.append("暂无时间线事件\n");
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取角色时间线失败: {}", e.getMessage(), e);
            return "❌ 获取角色时间线失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public List<Map<String, Object>> execute(Long scriptId, Long characterId) {
        // 限制最大返回数量
        return ragService.searchGlobalTimelineMemory(scriptId, characterId, "", 30);
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

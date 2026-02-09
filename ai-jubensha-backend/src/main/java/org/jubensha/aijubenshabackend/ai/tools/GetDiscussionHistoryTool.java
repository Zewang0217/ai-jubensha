package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 获取讨论历史工具
 * 用于AI获取之前的讨论消息，支持按游戏ID和限制数量查询
 * <p>
 * 工具说明：
 * - 功能：获取指定游戏的讨论历史消息，按时间倒序排列
 * - 参数：
 *   - gameId：游戏ID（必填）
 *   - limit：返回消息数量限制，默认20条，最大30条
 * - 返回格式：包含时间、发言人和内容的格式化讨论历史
 * - 权限：所有Agent都可以使用
 * <p>
 * 示例调用：
 * {
 *   "toolcall": {
 *     "thought": "需要了解最近的讨论情况",
 *     "name": "getDiscussionHistory",
 *     "params": {
 *       "gameId": "1",
 *       "limit": 20
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
public class GetDiscussionHistoryTool extends BaseTool {

    @Autowired
    private RAGService ragService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String getToolName() {
        return "getDiscussionHistory";
    }

    @Override
    public String getDisplayName() {
        return "获取讨论历史";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long gameId = arguments.getLong("gameId");
            int limit = 20;
            if (arguments.containsKey("limit")) {
                limit = arguments.getInt("limit");
            }

            // 限制最大返回数量，避免token过多
            if (limit > 30) {
                limit = 30;
                log.warn("讨论历史限制超过最大值30，已自动调整为30");
            }

            log.debug("获取讨论历史，游戏ID: {}, 限制: {}", gameId, limit);

            // 调用RAGService获取讨论历史
            List<Map<String, Object>> history = ragService.searchConversationMemory(gameId, null, "", limit);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("📋 讨论历史（最近").append(history.size()).append("条）:\n\n");
            
            for (Map<String, Object> message : history) {
                String playerName = (String) message.getOrDefault("player_name", "未知玩家");
                String content = (String) message.getOrDefault("content", "");
                Long timestamp = (Long) message.getOrDefault("timestamp", System.currentTimeMillis());
                
                String timeStr = dateFormat.format(new Date(timestamp));
                result.append(String.format("[%s] %s: %s\n", timeStr, playerName, content));
            }

            if (history.isEmpty()) {
                result.append("暂无讨论历史记录\n");
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取讨论历史失败: {}", e.getMessage(), e);
            return "❌ 获取讨论历史失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public List<Map<String, Object>> execute(Long gameId, int limit) {
        // 限制最大返回数量
        if (limit > 30) {
            limit = 30;
        }
        return ragService.searchConversationMemory(gameId, null, "", limit);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        // 所有Agent都可以查看讨论历史
        return ToolPermissionLevel.PLAYER;
    }
}

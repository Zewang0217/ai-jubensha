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
 * 获取讨论历史工具
 * 用于AI获取之前的讨论消息，支持按游戏ID和限制数量查询
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class GetDiscussionHistoryTool extends BaseTool {

    @Autowired
    private RAGService ragService;

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

            log.debug("获取讨论历史，游戏ID: {}, 限制: {}", gameId, limit);

            // 调用RAGService获取讨论历史
            List<Map<String, Object>> history = ragService.searchConversationMemory(gameId, null, "", limit);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("讨论历史:\n");
            
            for (Map<String, Object> message : history) {
                String playerName = (String) message.get("player_name");
                String content = (String) message.get("content");
                Long timestamp = (Long) message.get("timestamp");
                
                result.append(String.format("[%s]: %s\n", playerName, content));
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取讨论历史失败: {}", e.getMessage(), e);
            return "获取讨论历史失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public List<Map<String, Object>> execute(Long gameId, int limit) {
        return ragService.searchConversationMemory(gameId, null, "", limit);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        // 所有Agent都可以查看讨论历史
        return ToolPermissionLevel.PLAYER;
    }
}

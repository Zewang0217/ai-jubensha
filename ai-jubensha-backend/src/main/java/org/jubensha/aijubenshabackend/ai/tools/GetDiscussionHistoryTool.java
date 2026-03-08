package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.jubensha.aijubenshabackend.models.entity.Dialogue;
import org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

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
    private DialogueRepository dialogueRepository;

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

            // 使用 DialogueRepository 获取按时间倒序排列的讨论历史
            Pageable pageable = PageRequest.of(0, limit);
            List<Dialogue> dialogues = dialogueRepository.findByGameIdOrderByTimestampDesc(gameId, pageable);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("📋 讨论历史（最近").append(dialogues.size()).append("条）:\n\n");

            for (Dialogue dialogue : dialogues) {
                String playerName = dialogue.getPlayer() != null ? dialogue.getPlayer().getNickname() : "未知玩家";
                String content = dialogue.getContent();
                long timestampMillis = System.currentTimeMillis();

                // 处理 timestamp 字段
                if (dialogue.getTimestamp() != null) {
                    timestampMillis = dialogue.getTimestamp().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                }

                String timeStr = dateFormat.format(new Date(timestampMillis));
                result.append(String.format("[%s] %s: %s\n", timeStr, playerName, content));
            }

            if (dialogues.isEmpty()) {
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
    @Tool("获取讨论历史")
    public String executeGetDiscussionHistory(@P("游戏ID") Long gameId, @P("返回消息数量限制") int limit) {
        // 创建参数对象
        JSONObject arguments = new JSONObject();
        arguments.put("gameId", gameId);
        arguments.put("limit", limit);
        // 调用核心逻辑方法
        return generateToolExecutedResult(arguments);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        // 所有Agent都可以查看讨论历史
        return ToolPermissionLevel.PLAYER;
    }
}

package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * 获取玩家状态工具
 * 用于AI获取其他玩家的状态信息，支持按玩家ID查询
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class GetPlayerStatusTool extends BaseTool {

    @Autowired
    private PlayerService playerService;

    @Override
    public String getToolName() {
        return "getPlayerStatus";
    }

    @Override
    public String getDisplayName() {
        return "获取玩家状态";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long playerId = arguments.getLong("playerId");

            log.debug("获取玩家状态，玩家ID: {}", playerId);

            // 获取玩家信息
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElse(null);

            if (player == null) {
                return "玩家不存在";
            }

            // 构建玩家状态
            Map<String, Object> status = new HashMap<>();
            status.put("playerId", player.getId());
            status.put("nickname", player.getNickname());
            status.put("status", player.getStatus().name());
            status.put("role", player.getRole().name());

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("玩家状态:\n");
            result.append(String.format("ID: %d\n", status.get("playerId")));
            result.append(String.format("昵称: %s\n", status.get("nickname")));
            result.append(String.format("状态: %s\n", status.get("status")));
            result.append(String.format("角色: %s\n", status.get("role")));

            return result.toString();
        } catch (Exception e) {
            log.error("获取玩家状态失败: {}", e.getMessage(), e);
            return "获取玩家状态失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public Map<String, Object> execute(Long playerId) {
        try {
            // 获取玩家信息
            Optional<Player> playerOpt = playerService.getPlayerById(playerId);
            Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));

            // 构建玩家状态
            Map<String, Object> status = new HashMap<>();
            status.put("playerId", player.getId());
            status.put("nickname", player.getNickname());
            status.put("status", player.getStatus().name());
            status.put("role", player.getRole().name());

            return status;
        } catch (Exception e) {
            log.error("获取玩家状态失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case JUDGE:
            case SUMMARY:
                // 管理员类型的Agent可以查看所有玩家状态
                return ToolPermissionLevel.ADMIN;
            case PLAYER:
                // 玩家Agent只能查看自身状态
                return ToolPermissionLevel.PLAYER;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

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
 * 获取角色线索工具
 * 用于AI获取角色的线索信息，支持按角色ID查询，可选择是否只返回已发现的线索
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class GetClueTool extends BaseTool {

    @Autowired
    private RAGService ragService;

    @Override
    public String getToolName() {
        return "getClue";
    }

    @Override
    public String getDisplayName() {
        return "获取角色线索";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long characterId = arguments.getLong("characterId");
            Long scriptId = arguments.getLong("scriptId");
            boolean discoveredOnly = false;
            if (arguments.containsKey("discoveredOnly")) {
                discoveredOnly = arguments.getBool("discoveredOnly");
            }

            log.debug("获取角色线索，角色ID: {}, 剧本ID: {}, 只显示已发现: {}", characterId, scriptId, discoveredOnly);

            // 调用RAGService获取线索
            List<Map<String, Object>> clues = ragService.searchGlobalClueMemory(scriptId, characterId, "", 50);

            // 构建结果
            StringBuilder result = new StringBuilder();
            result.append("角色线索:\n");
            
            for (Map<String, Object> clue : clues) {
                String content = (String) clue.get("content");
                
                result.append(String.format("- %s\n", content));
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取角色线索失败: {}", e.getMessage(), e);
            return "获取角色线索失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    public List<Map<String, Object>> execute(Long scriptId, Long characterId, boolean discoveredOnly) {
        return ragService.searchGlobalClueMemory(scriptId, characterId, "", 50);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case JUDGE:
            case SUMMARY:
                // 管理员类型的Agent可以访问所有线索
                return ToolPermissionLevel.ADMIN;
            case PLAYER:
                // 玩家Agent只能访问与自身角色相关的线索
                return ToolPermissionLevel.PLAYER;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

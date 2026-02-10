package org.jubensha.aijubenshabackend.ai.tools;


import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.MessageQueueService;
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发送单聊请求工具
 * 用于AI向其他玩家发送单聊邀请，支持指定目标玩家和邀请消息
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class SendPrivateChatRequestTool extends BaseTool {

    @Autowired
    private MessageQueueService messageQueueService;

    @Override
    public String getToolName() {
        return "sendPrivateChatRequest";
    }

    @Override
    public String getDisplayName() {
        return "发送单聊请求";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        try {
            // 提取参数
            Long senderId = arguments.getLong("senderId");
            Long receiverId = arguments.getLong("receiverId");
            String message = arguments.getStr("message");

            log.debug("发送单聊请求，发送者ID: {}, 接收者ID: {}", senderId, receiverId);

            // 构建单聊邀请消息
            String invitationMessage = String.format("单聊邀请: %s", message);

            // 发送单聊邀请到消息队列
            messageQueueService.sendPrivateChatMessage(invitationMessage, senderId, receiverId);

            return "单聊请求发送成功";
        } catch (Exception e) {
            log.error("发送单聊请求失败: {}", e.getMessage(), e);
            return "发送单聊请求失败: " + e.getMessage();
        }
    }

    /**
     * 工具执行方法
     * 供AI直接调用
     */
    @Tool("发送单聊请求")
    public String executeSendPrivateChatRequest(@P("发送者ID") Long senderId, @P("接收者ID") Long receiverId, @P("邀请消息") String message) {
        // 创建参数对象
        JSONObject arguments = new JSONObject();
        arguments.put("senderId", senderId);
        arguments.put("receiverId", receiverId);
        arguments.put("message", message);
        // 调用核心逻辑方法
        return generateToolExecutedResult(arguments);
    }

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        switch (agentType) {
            case DM:
            case PLAYER:
                // DM和Player可以发送单聊请求
                return ToolPermissionLevel.PLAYER;
            case JUDGE:
            case SUMMARY:
                // Judge和Summary不应该发送单聊请求
                return ToolPermissionLevel.NONE;
            default:
                return ToolPermissionLevel.NONE;
        }
    }
}

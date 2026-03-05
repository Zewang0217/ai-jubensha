package org.jubensha.aijubenshabackend.websocket.message;

import lombok.Data;

/**
 * WebSocket 消息
 */
@Data
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 发送者ID (GamePlayerId)
     */
    private Long sender;

    /**
     * 消息内容
     */
    private Object payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, Long sender, Object payload) {
        this.type = type;
        this.sender = sender;
        this.payload = payload;
    }

    public void setType(MessageType messageType) {
        this.type = messageType.name();
    }

    public enum MessageType {
        CHAT_MESSAGE,          // 普通聊天
        SYSTEM_MESSAGE,        // DM/系统播报 (重要剧情推动)
        PHASE_CHANGE,          // ⚠️阶段切换 (重要：强制前端进入下一环节)
        TURN_CHANGE,           // 轮次切换 (通知谁正在发言)
        SCRIPT_READY,
        START_INVESTIGATION,
        PUBLIC_CLUE,
        PRIVATE_CLUE,          // 私密线索获得通知
        VOTE_REQUEST,          // 要求玩家开始投票
        VOTE_RESULT,
        PHASE_READY,           // 阶段就绪通知
        INVESTIGATION_COMPLETE // 搜证完成通知
    }
}

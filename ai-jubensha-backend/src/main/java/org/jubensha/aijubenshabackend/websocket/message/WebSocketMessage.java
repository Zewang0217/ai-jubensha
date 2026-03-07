package org.jubensha.aijubenshabackend.websocket.message;

import com.fasterxml.jackson.annotation.JsonSetter;
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

    /**
     * 设置消息类型（通过枚举）
     * 使用 @JsonSetter 注解避免与 Lombok 生成的 setType(String) 冲突
     * @param messageType 消息类型枚举
     */
    public void setMessageType(MessageType messageType) {
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
        VOTE_SUBMIT,           // 玩家提交投票（真人玩家发送）
        VOTE_SUCCESS,          // 投票成功反馈
        VOTE_ERROR,            // 投票失败反馈
        VOTE_RESULT,           // 投票结果广播
        PHASE_READY,           // 阶段就绪通知
        INVESTIGATION_COMPLETE, // 搜证完成通知
        GAME_ENDED,            // 游戏结束通知
        PLAYER_ANSWER,         // 玩家提交答案通知（用于观察者模式展示AI答题）
        AGENT_ACTION           // AI Agent 操作通知
    }
}

package org.jubensha.aijubenshabackend.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通 WebSocket 消息
 *
 * @author zewang
 * @author luobo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    // 用于指定 GameRoom
    private Long gameId;
    private WebSocketMessageType type;
    private Object payload;

    public void setType(String phaseChange) {
        this.type = WebSocketMessageType.valueOf(phaseChange);
    }

    public enum WebSocketMessageType {
        // 游戏状态更新
        GAME_STATE_UPDATE,
        // 聊天信息
        CHAT_MESSAGE,
        // 找到线索
        CLUE_FOUND,
        // 阶段更改
        PHASE_CHANGE,
        // 开始线索调查 payload: investigationScenes
        START_INVESTIGATION
    }
}

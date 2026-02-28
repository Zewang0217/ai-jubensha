package org.jubensha.aijubenshabackend.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GameRoom 隔离专用消息
 * 一般不用
 *
 * @author zewang
 * @author luobo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMessage {

    private Long gameId;
    private WebSocketMessage message;
}

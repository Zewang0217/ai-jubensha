# WebSocket 前端接入说明书

## 连接地址

```
ws://localhost:8088/ws?gameId={gameId}
```

**注意**：必须在 URL 参数中携带 `gameId`，否则连接会被拒绝。

## 连接流程

1. 用户进入游戏房间时，前端使用带 `gameId` 的地址连接 WebSocket
2. 后端自动分配 `gamePlayerId`（按连接顺序对应真人玩家列表）
3. 连接成功后即可收发消息

## 订阅主题

### 1. 聊天消息（必需）

```javascript
stompClient.subscribe('/topic/game/{gameId}/chat', function (message) {
    const chatMessage = JSON.parse(message.body);
    console.log('收到聊天消息:', chatMessage);
});
```

### 2. 个人消息（必需）

```javascript
// 用于接收剧本就绪、开始搜证等个人通知
stompClient.subscribe('/user/queue/messages', function (message) {
    const wsMessage = JSON.parse(message.body);
    handlePersonalMessage(wsMessage);
});
```

## 发送消息

### 1. 发送聊天消息

```javascript
const message = {
    type: 'CHAT_MESSAGE',
    payload: '聊天内容'
};
stompClient.send('/app/game/{gameId}/chat', {}, JSON.stringify(message));
```

### 2. 发送投票

```javascript
const voteMessage = {
    type: 'VOTE_SUBMIT',
    payload: {
        characterId: 123  // 投票给的角色ID
    }
};
stompClient.send('/app/game/{gameId}/vote', {}, JSON.stringify(voteMessage));
```

## 消息类型说明

| 消息类型                | 方向    | 说明       |
|---------------------|-------|----------|
| CHAT_MESSAGE        | 发送/接收 | 聊天消息     |
| SCRIPT_READY        | 接收    | 剧本生成完成通知 |
| START_INVESTIGATION | 接收    | 开始搜证通知   |
| PUBLIC_CLUE         | 接收    | 公开线索广播   |
| VOTE_RESULT         | 接收    | 投票结果广播   |

## 消息格式

### 通用格式

```json
{
  "type": "消息类型",
  "payload": "消息内容"
}
```

### SCRIPT_READY 示例

```json
{
  "type": "SCRIPT_READY",
  "payload": {
    "gamePlayerId": 1,
    "scriptId": 100
  }
}
```

### START_INVESTIGATION 示例

```json
{
  "type": "START_INVESTIGATION",
  "payload": {
    "scene1": [
      ...
    ],
    "scene2": [
      ...
    ]
  }
}
```

### PUBLIC_CLUE 示例

```json
{
  "type": "PUBLIC_CLUE",
  "payload": [
    {
      "id": 1,
      "name": "线索1",
      "description": "..."
    },
    {
      "id": 2,
      "name": "线索2",
      "description": "..."
    }
  ]
}
```

### VOTE_RESULT 示例

```json
{
  "type": "VOTE_RESULT",
  "payload": {
    "murdererId": 5,
    "voteDetails": {
      "1": 5,
      // playerId 1 投票给 characterId 5
      "2": 5,
      "3": 4
    },
    "dmScore": 85
  }
}
```

## 错误处理

连接可能被拒绝的情况：

- 缺少 `gameId` 参数
- `gameId` 无效
- 游戏没有真人玩家
- 真人玩家已满（连接数超过玩家数）

## 重连机制

前端应实现自动重连：

1. 连接断开时自动尝试重连
2. 使用相同的 `gameId` 重新连接
3. 后端会重新分配 `gamePlayerId`（可能分配相同的）

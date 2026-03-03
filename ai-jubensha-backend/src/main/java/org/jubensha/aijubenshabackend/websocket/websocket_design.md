###  

WebSocketMessageType

### 总体设计

1. 维护好不同的客户端连接session与Real GamePlayerId的对应关系。
2. 客户端在进入前端的GameRoom时会连接后端ws，连接时传递game_id。
   后端收到连接后通过GamePlayerService获取到真人玩家的列表，依照连接顺序从真人玩家列表中从0开始依次为每个连接分配gamePlayerId，
   如果没有剩余的Real GamePlayerId，则断开连接。
3. 带有重连机制。

### 需要实现的service

1. 向指定gameId的所有真人GamePlayer对应的ws连接广播聊天室信息，设计成前端订阅接收信息的形式。
2. notifyGamePlayerScriptReady(gamePlayerId, payload...)
   根据gamePlayerId向对应ws发送剧本生成成功通知，带有payload为scriptId和gamePlayerId。
3. notifyPlayerStartInvestigation(Long gamePlayerId, Map<String, Object> investigationScenes)
   根据gamePlayerId向对应ws发送开始调查环节通知，带有payload为investigationScenes
4. 指定gameId的所有真人GamePlayer对应的ws连接广播 公开线索(Clue), 带有payload为clue list
5. 向指定gameId的所有真人GamePlayer对应的ws连接广播最终投票结果，带有payload包括最终凶手、各个角色投票信息、dm打的分数

### 需要实现的controller

1. 前端发送到聊天室的消息(前端连接时就确定了game_id，所以就是某个gameId的game内部的消息)，使用service广播消息。
2. 前端发送的投票信息，发送内容为characterId，后端按需处理，可暂时占位。

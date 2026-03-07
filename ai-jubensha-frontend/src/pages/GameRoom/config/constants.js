/**
 * @fileoverview 常量配置
 * @description 游戏房间相关常量配置
 * @author zewang
 */

/**
 * 超时时间配置（毫秒）
 */
export const TIMEOUTS = {
  // WebSocket 连接超时
  CONNECTION: 10000,
  // 心跳间隔
  HEARTBEAT: 30000,
  // 消息发送超时
  MESSAGE_SEND: 5000,
  // 阶段切换超时
  PHASE_TRANSITION: 30000,
  // 投票超时
  VOTE_TIMEOUT: 60000,
}

/**
 * WebSocket 消息类型
 */
export const WS_MESSAGE_TYPES = {
  // 聊天消息
  CHAT_MESSAGE: 'CHAT_MESSAGE',
  CHAT_MESSAGE_LOWER: 'chat_message',

  // 系统消息
  SYSTEM_MESSAGE: 'SYSTEM_MESSAGE',

  // 投票相关
  VOTE_SUCCESS: 'VOTE_SUCCESS',
  VOTE_ERROR: 'VOTE_ERROR',
  VOTE_RESULT: 'VOTE_RESULT',

  // 阶段相关
  PHASE_CHANGE: 'PHASE_CHANGE',
  PHASE_READY: 'PHASE_READY',

  // 游戏状态
  GAME_STARTED: 'GAME_STARTED',
  GAME_ENDED: 'GAME_ENDED',

  // 玩家相关
  PLAYER_JOIN: 'PLAYER_JOIN',
  PLAYER_LEAVE: 'PLAYER_LEAVE',

  // 线索相关
  CLUE_FOUND: 'CLUE_FOUND',
  CLUE_SHARED: 'CLUE_SHARED',

  // AI 相关
  AI_MESSAGE: 'AI_MESSAGE',
  AI_ACTION: 'AI_ACTION',
}

/**
 * 游戏阶段定义
 */
export const GAME_PHASES = {
  // 准备阶段
  PREPARATION: 'PREPARATION',
  // 剧本阅读阶段
  SCRIPT_READING: 'SCRIPT_READING',
  // 搜证阶段
  INVESTIGATION: 'INVESTIGATION',
  // 讨论阶段
  DISCUSSION: 'DISCUSSION',
  // 投票阶段
  VOTING: 'VOTING',
  // 结果阶段
  RESULT: 'RESULT',
}

/**
 * 玩家角色类型
 */
export const PLAYER_ROLES = {
  // 真人玩家
  REAL: 'REAL',
  // AI 玩家
  AI: 'AI',
  // DM（主持人）
  DM: 'DM',
  // 法官
  JUDGE: 'JUDGE',
}

export default {
  TIMEOUTS,
  WS_MESSAGE_TYPES,
  GAME_PHASES,
  PLAYER_ROLES,
}

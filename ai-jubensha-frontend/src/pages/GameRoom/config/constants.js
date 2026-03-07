/**
 * @fileoverview 阶段公共常量配置
 * @description 定义游戏阶段组件使用的公共常量
 * @author zewang
 */

/**
 * 超时时间常量（毫秒）
 * @readonly
 * @enum {number}
 * @description 各种操作的超时时间配置
 */
export const TIMEOUTS = {
  /** 成功提示自动隐藏时间 */
  successFeedback: 5000,
  /** 错误提示自动隐藏时间 */
  errorFeedback: 8000,
  /** 警告提示自动隐藏时间 */
  warningFeedback: 3000,
  /** 消息发送防抖时间 */
  messageDebounce: 300,
  /** 阶段切换动画时间 */
  phaseTransition: 250,
  /** WebSocket 重连间隔 */
  wsReconnect: 3000,
  /** API 请求超时 */
  apiRequest: 10000,
}

/**
 * WebSocket 消息类型
 * @readonly
 * @enum {string}
 * @description WebSocket 消息类型枚举
 */
export const WS_MESSAGE_TYPES = {
  /** 聊天消息 */
  CHAT_MESSAGE: 'CHAT_MESSAGE',
  /** 聊天消息（小写格式） */
  CHAT_MESSAGE_LOWER: 'chat_message',
  /** 投票提交 */
  VOTE_SUBMIT: 'VOTE_SUBMIT',
  /** 投票成功 */
  VOTE_SUCCESS: 'VOTE_SUCCESS',
  /** 投票失败 */
  VOTE_ERROR: 'VOTE_ERROR',
  /** 阶段变化 */
  PHASE_CHANGE: 'PHASE_CHANGE',
  /** 阶段就绪 */
  PHASE_READY: 'PHASE_READY',
  /** 游戏结束 */
  GAME_ENDED: 'GAME_ENDED',
  /** 剧本就绪 */
  SCRIPT_READY: 'SCRIPT_READY',
  /** 开始搜证 */
  START_INVESTIGATION: 'START_INVESTIGATION',
}

/**
 * 玩家角色类型
 * @readonly
 * @enum {string}
 * @description 玩家角色类型枚举
 */
export const PLAYER_ROLES = {
  /** 真人玩家 */
  REAL: 'REAL',
  /** AI 玩家 */
  AI: 'AI',
  /** DM（主持人） */
  DM: 'DM',
  /** Judge（裁判） */
  JUDGE: 'JUDGE',
}

/**
 * 搜证次数配置
 * @readonly
 * @type {Object}
 */
export const INVESTIGATION_CONFIG = {
  /** 默认搜证次数 */
  defaultChances: 3,
  /** 最大搜证次数 */
  maxChances: 5,
}

/**
 * 分页配置
 * @readonly
 * @type {Object}
 */
export const PAGINATION_CONFIG = {
  /** 默认每页数量 */
  defaultPageSize: 10,
  /** 最大每页数量 */
  maxPageSize: 50,
}

/**
 * 消息最大长度
 * @readonly
 * @enum {number}
 */
export const MAX_LENGTH = {
  /** 聊天消息最大长度 */
  chatMessage: 500,
  /** 投票理由最大长度 */
  voteReason: 1000,
  /** 角色名称最大长度 */
  characterName: 50,
}

/**
 * 玻璃态样式类名
 * @readonly
 * @type {Object}
 * @description 用于快速应用玻璃态效果
 */
export const GLASS_CLASSES = {
  /** 基础玻璃态卡片 */
  card: 'bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D]',
  /** 高亮玻璃态卡片 */
  cardHighlight: 'bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl border border-[#E0E5EE] dark:border-[#363D4D]',
  /** 悬停效果 */
  hover: 'hover:bg-white/80 dark:hover:bg-[#222631]/80 hover:border-[#7C8CD6]/30',
  /** 输入框 */
  input: 'bg-white dark:bg-[#1A1D26] border border-[#E0E5EE] dark:border-[#363D4D]',
}

/**
 * 滚动条样式类名
 * @readonly
 * @type {string}
 */
export const SCROLLBAR_CLASS = 'scrollbar-thin'

export default {
  TIMEOUTS,
  WS_MESSAGE_TYPES,
  PLAYER_ROLES,
  INVESTIGATION_CONFIG,
  PAGINATION_CONFIG,
  MAX_LENGTH,
  GLASS_CLASSES,
  SCROLLBAR_CLASS,
}

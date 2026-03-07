/**
 * @fileoverview GameRoom 类型定义
 * @description GameRoom 相关组件的 PropTypes 定义
 * @author zewang
 */

import PropTypes from 'prop-types'

/**
 * Discussion 组件的 props 定义
 */
export const DiscussionProps = {
  /** 阶段配置 */
  config: PropTypes.object,
  /** 游戏数据 */
  gameData: PropTypes.object,
  /** 玩家数据 */
  playerData: PropTypes.array,
  /** 完成回调 */
  onComplete: PropTypes.func,
  /** 动作回调 */
  onAction: PropTypes.func,
  /** 订阅游戏聊天消息 */
  subscribeToGameChat: PropTypes.func,
  /** 订阅个人消息队列 */
  subscribeToPersonalMessages: PropTypes.func,
  /** 通用订阅方法 */
  subscribe: PropTypes.func,
  /** 发送聊天消息 */
  sendChatMessage: PropTypes.func,
  /** 发送投票 */
  sendVote: PropTypes.func,
  /** 取消订阅 */
  unsubscribe: PropTypes.func,
  /** WebSocket连接状态 */
  isConnected: PropTypes.bool,
  /** 当前真人玩家ID */
  currentPlayerId: PropTypes.number,
  /** 是否为观察者模式 */
  isObserverMode: PropTypes.bool,
}

export default {
  DiscussionProps,
}

/**
 * @fileoverview GameRoom 阶段组件 Props 类型定义
 * @description 使用 PropTypes 进行运行时类型校验
 * @author zewang
 */

import PropTypes from 'prop-types'

/**
 * 基础阶段 Props 类型定义
 * @description 所有阶段组件共享的基础 Props
 */
export const PhaseBaseProps = {
  /** 阶段配置 */
  config: PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    description: PropTypes.string,
    icon: PropTypes.string,
    allowSkip: PropTypes.bool,
    allowBack: PropTypes.bool,
  }),
  /** 游戏数据 */
  gameData: PropTypes.object,
  /** 玩家数据 */
  playerData: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.shape({
      data: PropTypes.array,
    }),
  ]),
  /** 阶段完成回调 */
  onComplete: PropTypes.func,
  /** 跳过阶段回调 */
  onSkip: PropTypes.func,
  /** 返回上一阶段回调 */
  onBack: PropTypes.func,
  /** 通用动作回调 */
  onAction: PropTypes.func,
  /** 是否为当前激活阶段 */
  isActive: PropTypes.bool,
  /** 是否为观察者模式 */
  isObserverMode: PropTypes.bool,
  /** 游戏ID */
  gameId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  /** 当前真人玩家ID */
  currentPlayerId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
}

/**
 * Discussion 阶段 Props 类型定义
 * @description 讨论投票阶段特有的 Props
 */
export const DiscussionProps = {
  ...PhaseBaseProps,
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
  /** WebSocket 连接状态 */
  isConnected: PropTypes.bool,
}

/**
 * Investigation 阶段 Props 类型定义
 * @description 搜证阶段特有的 Props
 */
export const InvestigationProps = {
  ...PhaseBaseProps,
  /** 是否所有玩家搜证完成 */
  isAllInvestigationComplete: PropTypes.bool,
}

/**
 * ScriptReading 阶段 Props 类型定义
 * @description 剧本阅读阶段特有的 Props
 */
export const ScriptReadingProps = {
  ...PhaseBaseProps,
}

/**
 * CharacterAssignment 阶段 Props 类型定义
 * @description 角色分配阶段特有的 Props
 */
export const CharacterAssignmentProps = {
  ...PhaseBaseProps,
}

/**
 * Summary 阶段 Props 类型定义
 * @description 总结阶段特有的 Props
 */
export const SummaryProps = {
  ...PhaseBaseProps,
}

/**
 * ScriptOverview 阶段 Props 类型定义
 * @description 剧本概览阶段特有的 Props
 */
export const ScriptOverviewProps = {
  ...PhaseBaseProps,
}

/**
 * 消息对象类型
 */
export const MessageType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  sender: PropTypes.string.isRequired,
  senderId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  content: PropTypes.string.isRequired,
  time: PropTypes.string.isRequired,
  isAI: PropTypes.bool,
  isSystem: PropTypes.bool,
})

/**
 * 玩家对象类型
 */
export const PlayerType = PropTypes.shape({
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  playerId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  name: PropTypes.string,
  characterName: PropTypes.string,
  isAI: PropTypes.bool,
  isDm: PropTypes.bool,
  playerRole: PropTypes.string,
  description: PropTypes.string,
  backgroundStory: PropTypes.string,
})

/**
 * 线索对象类型
 */
export const ClueType = PropTypes.shape({
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  name: PropTypes.string.isRequired,
  description: PropTypes.string,
  type: PropTypes.string,
  sceneId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  isRevealed: PropTypes.bool,
})

/**
 * 场景对象类型
 */
export const SceneType = PropTypes.shape({
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  name: PropTypes.string.isRequired,
  description: PropTypes.string,
  isLocked: PropTypes.bool,
  clueCount: PropTypes.number,
  clues: PropTypes.arrayOf(ClueType),
})

/**
 * 投票结果对象类型
 */
export const VoteResultType = PropTypes.shape({
  voterId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  targetId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  voteMessage: PropTypes.string,
  reason: PropTypes.string,
})

export default {
  PhaseBaseProps,
  DiscussionProps,
  InvestigationProps,
  ScriptReadingProps,
  CharacterAssignmentProps,
  SummaryProps,
  ScriptOverviewProps,
  MessageType,
  PlayerType,
  ClueType,
  SceneType,
  VoteResultType,
}

/**
 * @fileoverview 讨论/投票阶段 WebSocket 订阅 Hook
 * @description 封装讨论阶段的 WebSocket 订阅逻辑，包括游戏聊天、个人消息和阶段就绪消息
 * @author zewang
 */

import {useEffect, useRef, useCallback} from 'react'
import {TIMEOUTS, WS_MESSAGE_TYPES} from '../../../config'

/**
 * 讨论/投票阶段 WebSocket 订阅 Hook
 * @param {Object} options - 配置选项
 * @param {string} options.gameId - 游戏ID
 * @param {number} options.currentPlayerId - 当前玩家ID
 * @param {Array} options.realPlayerIds - 真人玩家ID列表
 * @param {Function} options.subscribeToGameChat - 订阅游戏聊天消息
 * @param {Function} options.subscribeToPersonalMessages - 订阅个人消息队列
 * @param {Function} options.subscribe - 通用订阅方法
 * @param {Function} options.unsubscribe - 取消订阅方法
 * @param {Function} options.onChatMessage - 聊天消息回调
 * @param {Function} options.onVoteResult - 投票结果回调
 * @param {Function} options.onPhaseReady - 阶段就绪回调
 * @param {Function} options.getPlayerNameById - 根据玩家ID获取玩家名称
 * @param {Function} options.formatTime - 格式化时间
 * @param {boolean} options.isConnected - WebSocket 连接状态
 * @param {boolean} options.isObserverMode - 是否为观察者模式
 * @returns {Object} WebSocket 订阅状态和方法
 */
export function useDiscussionWebSocket({
  gameId,
  currentPlayerId,
  realPlayerIds,
  subscribeToGameChat,
  subscribeToPersonalMessages,
  subscribe,
  unsubscribe,
  onChatMessage,
  onVoteResult,
  onPhaseReady,
  getPlayerNameById,
  formatTime,
  isConnected,
  isObserverMode,
}) {
  const subscriptionIdRef = useRef(null)
  const personalSubscriptionIdRef = useRef(null)
  const phaseSubscriptionIdRef = useRef(null)

  /**
   * 判断是否为真人玩家
   * @param {number} playerId - 玩家ID
   * @returns {boolean} 是否为真人玩家
   */
  const isRealPlayer = useCallback((playerId) => {
    return realPlayerIds.includes(playerId)
  }, [realPlayerIds])

  /**
   * 订阅游戏聊天消息
   */
  useEffect(() => {
    if (!subscribeToGameChat || !isConnected) {
      console.log('[useDiscussionWebSocket] WebSocket未连接或未提供订阅方法')
      return
    }

    console.log('[useDiscussionWebSocket] 开始订阅游戏聊天消息')

    subscriptionIdRef.current = subscribeToGameChat((message) => {
      console.log('[useDiscussionWebSocket] 收到WebSocket消息:', message)

      if (message.type === WS_MESSAGE_TYPES.CHAT_MESSAGE || 
          message.type === WS_MESSAGE_TYPES.CHAT_MESSAGE_LOWER) {
        const senderId = message.sender
        let content = ''
        let senderName = ''

        if (typeof message.payload === 'string') {
          content = message.payload
        } else if (message.payload && typeof message.payload === 'object') {
          content = message.payload.message || ''
          senderName = message.payload.playerName || ''
        }

        if (senderId === currentPlayerId) {
          console.log('[useDiscussionWebSocket] 忽略自己发送的消息')
          return
        }

        const newMessage = {
          id: `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          sender: senderName || getPlayerNameById(senderId),
          senderId: senderId,
          content: content,
          time: formatTime(new Date()),
          isAI: !isRealPlayer(senderId),
          isSystem: false,
        }

        console.log('[useDiscussionWebSocket] 添加消息:', newMessage)
        onChatMessage?.(newMessage)
      }
    })

    return () => {
      if (subscriptionIdRef.current && unsubscribe) {
        console.log('[useDiscussionWebSocket] 取消订阅游戏聊天消息')
        unsubscribe(subscriptionIdRef.current)
      }
    }
  }, [
    subscribeToGameChat, 
    unsubscribe, 
    isConnected, 
    currentPlayerId, 
    getPlayerNameById, 
    isRealPlayer, 
    formatTime,
    onChatMessage
  ])

  /**
   * 订阅个人消息队列（投票结果）
   */
  useEffect(() => {
    if (!subscribeToPersonalMessages || !isConnected) {
      console.log('[useDiscussionWebSocket] WebSocket未连接或未提供个人消息订阅方法')
      return
    }

    console.log('[useDiscussionWebSocket] 开始订阅个人消息队列')

    personalSubscriptionIdRef.current = subscribeToPersonalMessages((message) => {
      console.log('[useDiscussionWebSocket] 收到个人消息:', message)

      if (message.type === WS_MESSAGE_TYPES.VOTE_SUCCESS) {
        console.log('[useDiscussionWebSocket] 投票成功:', message.payload)
        onVoteResult?.({
          type: 'success',
          message: message.payload?.message || '投票成功',
          data: message.payload,
        })
      }

      if (message.type === WS_MESSAGE_TYPES.VOTE_ERROR) {
        console.log('[useDiscussionWebSocket] 投票失败:', message.payload)
        onVoteResult?.({
          type: 'error',
          message: message.payload?.message || '投票失败，请重试',
          data: message.payload,
        })
      }
    })

    return () => {
      if (personalSubscriptionIdRef.current && unsubscribe) {
        console.log('[useDiscussionWebSocket] 取消订阅个人消息队列')
        unsubscribe(personalSubscriptionIdRef.current)
      }
    }
  }, [subscribeToPersonalMessages, unsubscribe, isConnected, onVoteResult])

  /**
   * 订阅答题阶段就绪消息
   */
  useEffect(() => {
    if (!subscribe || !isConnected || !gameId) {
      console.log('[useDiscussionWebSocket] WebSocket未连接或未提供订阅方法或gameId不存在')
      return
    }

    const topic = `/topic/game/${gameId}/phase`
    console.log('[useDiscussionWebSocket] 开始订阅答题阶段就绪消息, topic:', topic)

    phaseSubscriptionIdRef.current = subscribe(topic, (message) => {
      console.log('[useDiscussionWebSocket] 收到答题阶段消息:', message)

      if (message.nodeName === 'ANSWER_PHASE' && message.isReady === true) {
        console.log('[useDiscussionWebSocket] 答题阶段已就绪')
        onPhaseReady?.({
          type: 'answer_phase_ready',
          message: '答题阶段已开始，请前往投票Tab进行投票',
        })
      }
    })

    return () => {
      if (phaseSubscriptionIdRef.current && unsubscribe) {
        console.log('[useDiscussionWebSocket] 取消订阅答题阶段就绪消息')
        unsubscribe(phaseSubscriptionIdRef.current)
      }
    }
  }, [subscribe, unsubscribe, isConnected, gameId, onPhaseReady])

  return {
    subscriptionIdRef,
    personalSubscriptionIdRef,
    phaseSubscriptionIdRef,
  }
}

export default useDiscussionWebSocket

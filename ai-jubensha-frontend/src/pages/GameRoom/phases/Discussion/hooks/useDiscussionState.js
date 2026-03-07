/**
 * @fileoverview 讨论阶段状态管理 Hook
 * @description 封装讨论阶段的状态管理逻辑，包括消息列表、投票状态等
 * @author zewang
 */

import {useState, useCallback, useRef, useEffect} from 'react'
import {TIMEOUTS} from '../../../config'

/**
 * @typedef {Object} Message
 * @property {string} id - 消息ID
 * @property {string} sender - 发送者名称
 * @property {number} senderId - 发送者ID
 * @property {string} content - 消息内容
 * @property {string} time - 消息时间
 * @property {boolean} isAI - 是否为AI消息
 * @property {boolean} isSystem - 是否为系统消息
 */

/**
 * @typedef {Object} VoteFeedback
 * @property {string} type - 反馈类型 ('success' | 'error')
 * @property {string} message - 反馈消息
 * @property {Object} data - 反馈数据
 */

/**
 * 讨论阶段状态管理 Hook
 * @param {Object} initialState - 初始状态
 * @param {Array} initialState.messages - 初始消息列表
 * @param {boolean} initialState.hasVoted - 是否已投票
 * @returns {Object} 讨论状态和操作方法
 */
export function useDiscussionState(initialState = {}) {
  const [messages, setMessages] = useState(initialState.messages || [])
  const [inputText, setInputText] = useState('')
  const [selectedTarget, setSelectedTarget] = useState(null)
  const [hasVoted, setHasVoted] = useState(initialState.hasVoted || false)
  const [voteMessage, setVoteMessage] = useState('')
  const [activeTab, setActiveTab] = useState('discussion')
  const [hoveredPlayer, setHoveredPlayer] = useState(null)
  const [hoveredClue, setHoveredClue] = useState(null)
  const [voteFeedback, setVoteFeedback] = useState(null)

  const messagesEndRef = useRef(null)
  const feedbackTimeoutRef = useRef(null)

  /**
   * 添加消息到列表
   * @param {Message} message - 消息对象
   */
  const addMessage = useCallback((message) => {
    setMessages(prev => [...prev, message])
  }, [])

  /**
   * 添加系统消息
   * @param {string} content - 消息内容
   * @param {Function} formatTime - 时间格式化函数
   */
  const addSystemMessage = useCallback((content, formatTime) => {
    const systemMessage = {
      id: `system-${Date.now()}`,
      sender: '系统',
      content: content,
      time: formatTime(new Date()),
      isAI: false,
      isSystem: true,
    }
    setMessages(prev => [...prev, systemMessage])
  }, [])

  /**
   * 清空输入框
   */
  const clearInput = useCallback(() => {
    setInputText('')
  }, [])

  /**
   * 设置投票反馈
   * @param {VoteFeedback} feedback - 反馈对象
   */
  const setFeedback = useCallback((feedback) => {
    setVoteFeedback(feedback)

    if (feedbackTimeoutRef.current) {
      clearTimeout(feedbackTimeoutRef.current)
    }

    const timeout = feedback?.type === 'success' 
      ? TIMEOUTS.successFeedback 
      : TIMEOUTS.errorFeedback

    feedbackTimeoutRef.current = setTimeout(() => {
      setVoteFeedback(null)
    }, timeout)
  }, [])

  /**
   * 清除投票反馈
   */
  const clearFeedback = useCallback(() => {
    if (feedbackTimeoutRef.current) {
      clearTimeout(feedbackTimeoutRef.current)
    }
    setVoteFeedback(null)
  }, [])

  /**
   * 重置投票状态
   */
  const resetVoteState = useCallback(() => {
    setSelectedTarget(null)
    setVoteMessage('')
    setHasVoted(false)
    clearFeedback()
  }, [clearFeedback])

  /**
   * 滚动到最新消息
   */
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({behavior: 'smooth'})
  }, [])

  /**
   * 消息更新时自动滚动
   */
  useEffect(() => {
    scrollToBottom()
  }, [messages, scrollToBottom])

  /**
   * 清理定时器
   */
  useEffect(() => {
    return () => {
      if (feedbackTimeoutRef.current) {
        clearTimeout(feedbackTimeoutRef.current)
      }
    }
  }, [])

  return {
    messages,
    setMessages,
    addMessage,
    addSystemMessage,
    inputText,
    setInputText,
    clearInput,
    selectedTarget,
    setSelectedTarget,
    hasVoted,
    setHasVoted,
    voteMessage,
    setVoteMessage,
    activeTab,
    setActiveTab,
    hoveredPlayer,
    setHoveredPlayer,
    hoveredClue,
    setHoveredClue,
    voteFeedback,
    setFeedback,
    clearFeedback,
    resetVoteState,
    messagesEndRef,
    scrollToBottom,
  }
}

export default useDiscussionState

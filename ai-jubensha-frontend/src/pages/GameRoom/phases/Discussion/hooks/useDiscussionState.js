/**
 * @fileoverview 讨论/投票阶段状态管理 Hook
 * @description 封装讨论阶段的状态管理逻辑
 * @author zewang
 */

import {useState, useRef, useCallback} from 'react'

/**
 * 讨论/投票阶段状态管理 Hook
 * @returns {Object} 状态和方法
 */
export function useDiscussionState() {
  // 消息列表
  const [messages, setMessages] = useState([])

  // 输入框文本
  const [inputText, setInputText] = useState('')

  // 选中的投票目标
  const [selectedTarget, setSelectedTarget] = useState(null)

  // 是否已投票
  const [hasVoted, setHasVoted] = useState(false)

  // 投票理由
  const [voteMessage, setVoteMessage] = useState('')

  // 当前激活的标签页
  const [activeTab, setActiveTab] = useState('vote')

  // 悬停的玩家
  const [hoveredPlayer, setHoveredPlayer] = useState(null)

  // 悬停的线索
  const [hoveredClue, setHoveredClue] = useState(null)

  // 投票反馈信息
  const [voteFeedback, setVoteFeedback] = useState(null)

  // 消息列表底部引用（用于自动滚动）
  const messagesEndRef = useRef(null)

  /**
   * 添加消息
   * @param {Object} message - 消息对象
   */
  const addMessage = useCallback((message) => {
    setMessages((prev) => [...prev, message])
  }, [])

  /**
   * 添加系统消息
   * @param {string} content - 消息内容
   */
  const addSystemMessage = useCallback((content) => {
    const systemMessage = {
      id: `sys-${Date.now()}`,
      sender: '系统',
      content,
      time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}),
      isSystem: true,
    }
    setMessages((prev) => [...prev, systemMessage])
  }, [])

  /**
   * 清空输入
   */
  const clearInput = useCallback(() => {
    setInputText('')
  }, [])

  /**
   * 设置反馈信息
   * @param {Object} feedback - 反馈对象
   */
  const setFeedback = useCallback((feedback) => {
    setVoteFeedback(feedback)
  }, [])

  /**
   * 清除反馈信息
   */
  const clearFeedback = useCallback(() => {
    setVoteFeedback(null)
  }, [])

  return {
    // 状态
    messages,
    inputText,
    selectedTarget,
    hasVoted,
    voteMessage,
    activeTab,
    hoveredPlayer,
    hoveredClue,
    voteFeedback,
    messagesEndRef,

    // 设置器
    setMessages,
    setInputText,
    setSelectedTarget,
    setHasVoted,
    setVoteMessage,
    setActiveTab,
    setHoveredPlayer,
    setHoveredClue,

    // 方法
    addMessage,
    addSystemMessage,
    clearInput,
    setFeedback,
    clearFeedback,
  }
}

export default useDiscussionState

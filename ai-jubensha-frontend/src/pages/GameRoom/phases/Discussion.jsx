/**
 * @fileoverview Discussion 组件 - 透明背景 + 玻璃态卡片
 * @description 讨论投票阶段，与 CharacterAssignment/ScriptReading/Investigation 风格保持一致
 */

import {memo, useCallback, useEffect, useMemo, useRef, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {Bot, ChevronRight, Eye, FileText, MessageCircle, Send, User, Vote} from 'lucide-react'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'

// =============================================================================
// 动画配置
// =============================================================================

const containerVariants = {
  hidden: {opacity: 0},
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.1,
    },
  },
}

const itemVariants = {
  hidden: {opacity: 0, y: 12},
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.4,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

// =============================================================================
// 背景装饰
// =============================================================================

const BackgroundDecor = memo(() => (
    <>
      <motion.div
          className="absolute top-0 left-0 w-64 h-64 rounded-full opacity-30 blur-3xl"
          style={{
            background: 'radial-gradient(circle, rgba(124, 140, 214, 0.4) 0%, transparent 70%)',
          }}
          animate={{
            scale: [1, 1.1, 1],
            opacity: [0.3, 0.4, 0.3],
          }}
          transition={{duration: 4, repeat: Infinity}}
      />
      <motion.div
          className="absolute bottom-0 right-0 w-80 h-80 rounded-full opacity-20 blur-3xl"
          style={{
            background: 'radial-gradient(circle, rgba(167, 139, 250, 0.4) 0%, transparent 70%)',
          }}
          animate={{
            scale: [1, 1.15, 1],
            opacity: [0.2, 0.35, 0.2],
          }}
          transition={{duration: 5, repeat: Infinity, delay: 1}}
      />
      {[...Array(6)].map((_, i) => (
          <motion.div
              key={i}
              className="absolute w-1 h-1 rounded-full"
              style={{
                backgroundColor: i % 2 === 0 ? '#7C8CD6' : '#F5A9C9',
                top: `${15 + i * 12}%`,
                left: `${10 + i * 8}%`,
              }}
              animate={{
                opacity: [0.2, 0.8, 0.2],
                scale: [0.8, 1.2, 0.8],
              }}
              transition={{
                duration: 2 + i * 0.3,
                repeat: Infinity,
                delay: i * 0.2,
              }}
          />
      ))}
    </>
))

BackgroundDecor.displayName = 'BackgroundDecor'

// =============================================================================
// 线索列表项
// =============================================================================

const ClueListItem = memo(({clue, index, onHover}) => (
    <motion.div
        initial={{opacity: 0, x: -10}}
        animate={{opacity: 1, x: 0}}
        transition={{delay: index * 0.05}}
        onMouseEnter={() => onHover?.(clue)}
        onMouseLeave={() => onHover?.(null)}
        className="group p-2.5 rounded-lg bg-white/40 dark:bg-[#222631]/40 backdrop-blur-sm border border-transparent hover:bg-white/60 dark:hover:bg-[#222631]/60 hover:border-[#7C8CD6]/30 transition-all cursor-pointer"
    >
      <div className="flex items-start gap-2.5">
        <div
            className="w-6 h-6 rounded-md bg-gradient-to-br from-[#7C8CD6]/20 to-[#A78BFA]/20 flex items-center justify-center flex-shrink-0">
          <FileText className="w-3 h-3 text-[#7C8CD6]"/>
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-[#2D3748] dark:text-[#E8ECF2] text-xs font-medium leading-tight line-clamp-2">
            {clue.name}
          </p>
          <span className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] mt-0.5 block">
          {clue.type}
        </span>
        </div>
      </div>
    </motion.div>
))

ClueListItem.displayName = 'ClueListItem'

// =============================================================================
// 消息气泡
// =============================================================================

const MessageBubble = memo(({message, isSelf}) => {
  const isSystem = message.isSystem

  return (
      <div
          className={`flex gap-3 ${isSelf ? 'flex-row-reverse' : ''}`}
      >
        {!isSystem && (
            <div
                className={`
            w-9 h-9 rounded-lg flex-shrink-0 flex items-center justify-center
            ${isSelf
                    ? 'bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] text-white'
                    : message.isAI
                        ? 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#7C8CD6]'
                        : 'bg-[#F5A9C9]/20 text-[#F5A9C9]'
                }
          `}
            >
              {message.isAI ? <Bot className="w-4 h-4"/> : <User className="w-4 h-4"/>}
            </div>
        )}

        <div className={`max-w-[75%] ${isSelf ? 'text-right' : ''}`}>
          {!isSystem && (
              <p className="text-[#8C96A5] dark:text-[#6B7788] text-[10px] mb-1">
                {message.sender} • {message.time}
              </p>
          )}
          <div
              className={`
            inline-block px-3 py-2 rounded-xl text-xs leading-relaxed
            ${isSystem
                  ? 'bg-[#7C8CD6]/10 text-[#7C8CD6] w-full text-center border border-[#7C8CD6]/20'
                  : isSelf
                      ? 'bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] text-white'
                      : 'bg-white/80 dark:bg-[#222631]/80 text-[#2D3748] dark:text-[#E8ECF2] border border-[#E0E5EE] dark:border-[#363D4D]'
              }
          `}
          >
            {message.content}
          </div>
        </div>
      </div>
  )
})

MessageBubble.displayName = 'MessageBubble'

// =============================================================================
// 投票候选人卡片
// =============================================================================

const CandidateCard = memo(({player, isSelected, hasVoted, onVote, onHover}) => {
  // 获取玩家ID（兼容不同的数据格式）
  const playerId = player.playerId || player.id
  
  return (
    <div
        onMouseEnter={() => onHover?.(player)}
        onMouseLeave={() => onHover?.(null)}
        className={`
      w-full p-3 rounded-xl transition-all border flex items-center gap-3 cursor-pointer
      ${isSelected
            ? 'bg-[#F5A9C9]/10 border-[#F5A9C9]/50'
            : hasVoted
                ? 'bg-white/30 dark:bg-[#222631]/30 border-[#E0E5EE]/50 dark:border-[#363D4D]/50 opacity-50'
                : 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:bg-white/80 dark:hover:bg-[#222631]/80'
        }
    `}
    >
      {/* 头像 */}
      <div
          className={`
        w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm flex-shrink-0
        ${isSelected
              ? 'bg-gradient-to-br from-[#F5A9C9] to-[#E879A9] text-white'
              : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#8C96A5]'
          }
      `}
      >
        {(player.name || player.characterName || '?').charAt(0)}
      </div>

      {/* 信息 */}
      <div className="flex-1 min-w-0">
        <h4 className={`font-medium text-sm ${isSelected ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
          {player.name || player.characterName || '未知玩家'}
        </h4>
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788]">{player.role || player.characterRole || '角色'}</p>
      </div>

      {/* 选择状态 */}
      {!hasVoted ? (
          <div
              onClick={() => onVote(playerId)}
              className={`text-xs px-2 py-1 rounded ${isSelected ? 'text-[#F5A9C9] font-medium' : 'text-[#8C96A5]'}`}
          >
            {isSelected ? '已选择' : '选择'}
          </div>
      ) : (
          isSelected && (
              <div className="w-5 h-5 rounded-full bg-[#5DD9A8]/20 flex items-center justify-center">
                <span className="text-[#5DD9A8] text-xs">✓</span>
              </div>
          )
      )}
    </div>
  )
})

CandidateCard.displayName = 'CandidateCard'

// =============================================================================
// 投票结果卡片（观察者模式专用）
// =============================================================================

/**
 * 投票结果卡片组件
 * @description 显示单个玩家的投票信息，包括投票对象和投票理由
 * @param {Object} props - 组件属性
 * @param {Object} props.vote - 投票信息对象
 * @param {string} props.vote.voterName - 投票者名称
 * @param {string} props.vote.voterRole - 投票者角色
 * @param {string} props.vote.targetName - 被投票者名称
 * @param {string} props.vote.voteMessage - 投票理由
 * @param {number} props.index - 索引，用于动画延迟
 * @returns {JSX.Element} 投票结果卡片组件
 */
const VoteResultCard = memo(({vote, index}) => {
  return (
    <motion.div
      initial={{opacity: 0, y: 10}}
      animate={{opacity: 1, y: 0}}
      transition={{delay: index * 0.1}}
      className="p-4 rounded-xl bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] hover:bg-white/80 dark:hover:bg-[#222631]/80 transition-all"
    >
      {/* 投票者信息 */}
      <div className="flex items-center gap-3 mb-3 pb-3 border-b border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
        <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
          {vote.voterName.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <h4 className="font-medium text-sm text-[#2D3748] dark:text-[#E8ECF2]">
            {vote.voterName}
          </h4>
          <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788]">
            {vote.voterRole}
          </p>
        </div>
        <div className="flex items-center gap-1.5 px-2 py-1 rounded-lg bg-[#F5A9C9]/10 border border-[#F5A9C9]/20">
          <Vote className="w-3 h-3 text-[#F5A9C9]"/>
          <span className="text-xs text-[#F5A9C9] font-medium">已投票</span>
        </div>
      </div>

      {/* 投票对象 */}
      <div className="mb-3">
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider mb-1.5">
          投票对象
        </p>
        <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#EEF1F6]/50 dark:bg-[#2A2F3C]/50 border border-[#E0E5EE] dark:border-[#363D4D]">
          <div className="w-6 h-6 rounded-md bg-gradient-to-br from-[#F5A9C9] to-[#E879A9] flex items-center justify-center text-white font-bold text-xs">
            {vote.targetName.charAt(0)}
          </div>
          <span className="text-sm text-[#2D3748] dark:text-[#E8ECF2] font-medium">
            {vote.targetName}
          </span>
        </div>
      </div>

      {/* 投票理由 */}
      <div>
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider mb-1.5">
          投票理由
        </p>
        <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed px-3 py-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40 border border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
          {vote.voteMessage || '未提供投票理由'}
        </p>
      </div>
    </motion.div>
  )
})

VoteResultCard.displayName = 'VoteResultCard'

// =============================================================================
// 标签切换器
// =============================================================================

const TabSwitcher = memo(({activeTab, onChange, hasVoted}) => (
    <div
        className="inline-flex p-1 rounded-xl bg-[#EEF1F6]/80 dark:bg-[#222631]/80 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D]">
      <button
          onClick={() => onChange('discussion')}
          className={`
        px-4 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer
        ${activeTab === 'discussion'
              ? 'bg-white dark:bg-[#2A2F3C] text-[#7C8CD6] shadow-sm'
              : 'text-[#8C96A5] hover:text-[#5A6978]'
          }
      `}
      >
        <MessageCircle className="w-3.5 h-3.5"/>
        讨论
      </button>
      <button
          onClick={() => onChange('vote')}
          className={`
        px-4 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer
        ${activeTab === 'vote'
              ? 'bg-white dark:bg-[#2A2F3C] text-[#F5A9C9] shadow-sm'
              : 'text-[#8C96A5] hover:text-[#5A6978]'
          }
      `}
      >
        <Vote className="w-3.5 h-3.5"/>
        投票
        {hasVoted && <span className="w-1.5 h-1.5 rounded-full bg-[#5DD9A8]"/>}
      </button>
    </div>
))

TabSwitcher.displayName = 'TabSwitcher'

// =============================================================================
// 主要组件
// =============================================================================

/**
 * Discussion 组件
 * @description 讨论投票阶段组件，支持玩家模式和与观察者模式
 * @param {Object} props - 组件属性
 * @param {Object} props.config - 阶段配置
 * @param {Object} props.gameData - 游戏数据
 * @param {Object} props.playerData - 玩家数据
 * @param {Function} props.onComplete - 完成回调
 * @param {Function} props.onAction - 动作回调
 * @param {Function} props.subscribeToGameChat - 订阅游戏聊天消息
 * @param {Function} props.subscribeToPersonalMessages - 订阅个人消息队列
 * @param {Function} props.subscribe - 通用订阅方法（用于订阅特定topic）
 * @param {Function} props.sendChatMessage - 发送聊天消息
 * @param {Function} props.sendVote - 发送投票
 * @param {Function} props.unsubscribe - 取消订阅
 * @param {boolean} props.isConnected - WebSocket连接状态
 * @param {number} props.currentPlayerId - 当前真人玩家ID
 * @param {boolean} props.isObserverMode - 是否为观察者模式
 * @returns {JSX.Element} Discussion 组件
 */
function Discussion({
  config: _config,
  gameData,
  playerData,
  onComplete,
  onAction,
  subscribeToGameChat,
  subscribeToPersonalMessages,
  subscribe,
  sendChatMessage,
  sendVote,
  unsubscribe,
  isConnected,
  currentPlayerId,
  isObserverMode = false,
}) {
  const [messages, setMessages] = useState([])
  const [inputText, setInputText] = useState('')
  const [selectedTarget, setSelectedTarget] = useState(null)
  const [hasVoted, setHasVoted] = useState(false)
  const [voteMessage, setVoteMessage] = useState('')
  const [activeTab, setActiveTab] = useState('discussion')
  const [hoveredPlayer, setHoveredPlayer] = useState(null)
  const [hoveredClue, setHoveredClue] = useState(null)
  const [voteFeedback, setVoteFeedback] = useState(null) // 投票反馈消息
  const messagesEndRef = useRef(null)
  const subscriptionIdRef = useRef(null)
  const personalSubscriptionIdRef = useRef(null) // 个人消息订阅ID
  const phaseSubscriptionIdRef = useRef(null) // 答题阶段订阅ID

  // 公开线索列表
  const publicClues = gameData?.publicClues || []

  // 玩家列表 - 从 gameData.players 获取
  const players = gameData?.players || []

  // 获取真人玩家ID列表（用于判断消息来源）
  const realPlayerIds = useMemo(() => {
    const realPlayers = playerData?.filter(p => p.playerRole === 'REAL') || []
    return realPlayers.map(p => p.playerId)
  }, [playerData])

  // 获取当前真人玩家信息
  const currentPlayer = useMemo(() => {
    return players.find(p => p.playerId === currentPlayerId) || players.find(p => p.isSelf)
  }, [players, currentPlayerId])

  // 其他玩家（排除自己）
  const otherPlayers = useMemo(() => {
    return players.filter(p => p.playerId !== currentPlayerId && !p.isSelf)
  }, [players, currentPlayerId])

  // 投票结果数据（观察者模式使用）
  // 从 gameData 中获取所有玩家的投票信息
  const voteResults = useMemo(() => {
    if (!isObserverMode) return []
    
    // 从 gameData.votes 或 gameData.playerVotes 获取投票数据
    const votes = gameData?.votes || gameData?.playerVotes || []
    
    return votes.map(vote => ({
      voterName: getPlayerNameById(vote.voterId || vote.playerId),
      voterRole: players.find(p => p.playerId === (vote.voterId || vote.playerId))?.role || '角色',
      targetName: getPlayerNameById(vote.targetId),
      voteMessage: vote.voteMessage || vote.reason || '',
    }))
  }, [isObserverMode, gameData, players, getPlayerNameById])

  // 格式化时间
  const formatTime = useCallback((date) => {
    return date.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})
  }, [])

  // 根据玩家ID获取玩家名称
  const getPlayerNameById = useCallback((playerId) => {
    const player = players.find(p => p.playerId === playerId || p.id === playerId)
    return player?.name || player?.characterName || `玩家${playerId}`
  }, [players])

  // 判断是否为真人玩家
  const isRealPlayer = useCallback((playerId) => {
    return realPlayerIds.includes(playerId)
  }, [realPlayerIds])

  // 订阅游戏聊天消息
  useEffect(() => {
    if (!subscribeToGameChat || !isConnected) {
      console.log('[Discussion] WebSocket未连接或未提供订阅方法')
      return
    }

    console.log('[Discussion] 开始订阅游戏聊天消息')
    
    subscriptionIdRef.current = subscribeToGameChat((message) => {
      console.log('[Discussion] 收到WebSocket消息:', message)
      
      // 处理聊天消息
      if (message.type === 'CHAT_MESSAGE' || message.type === 'chat_message') {
        const senderId = message.sender
        const content = message.payload
        
        // 忽略自己发送的消息（已经在本地添加了）
        if (senderId === currentPlayerId) {
          console.log('[Discussion] 忽略自己发送的消息')
          return
        }
        
        const newMessage = {
          id: `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          sender: getPlayerNameById(senderId),
          senderId: senderId,
          content: content,
          time: formatTime(new Date()),
          isAI: !isRealPlayer(senderId),
          isSystem: false,
        }
        
        console.log('[Discussion] 添加AI玩家消息:', newMessage)
        setMessages(prev => [...prev, newMessage])
      }
    })

    return () => {
      if (subscriptionIdRef.current && unsubscribe) {
        console.log('[Discussion] 取消订阅游戏聊天消息')
        unsubscribe(subscriptionIdRef.current)
      }
    }
  }, [subscribeToGameChat, unsubscribe, isConnected, currentPlayerId, getPlayerNameById, isRealPlayer, formatTime])

  // 订阅个人消息队列（投票结果）
  useEffect(() => {
    if (!subscribeToPersonalMessages || !isConnected) {
      console.log('[Discussion] WebSocket未连接或未提供个人消息订阅方法')
      return
    }

    console.log('[Discussion] 开始订阅个人消息队列')
    
    personalSubscriptionIdRef.current = subscribeToPersonalMessages((message) => {
      console.log('[Discussion] 收到个人消息:', message)
      
      // 处理投票成功消息
      if (message.type === 'VOTE_SUCCESS') {
        console.log('[Discussion] 投票成功:', message.payload)
        setVoteFeedback({
          type: 'success',
          message: message.payload?.message || '投票成功',
          data: message.payload,
        })
        // 更新投票状态
        setHasVoted(true)
        
        // 5秒后自动隐藏成功提示
        setTimeout(() => {
          setVoteFeedback(null)
        }, 5000)
      }
      
      // 处理投票失败消息
      if (message.type === 'VOTE_ERROR') {
        console.log('[Discussion] 投票失败:', message.payload)
        setVoteFeedback({
          type: 'error',
          message: message.payload?.message || '投票失败，请重试',
          data: message.payload,
        })
        
        // 8秒后自动隐藏错误提示（给用户更多时间查看）
        setTimeout(() => {
          setVoteFeedback(null)
        }, 8000)
      }
    })

    return () => {
      if (personalSubscriptionIdRef.current && unsubscribe) {
        console.log('[Discussion] 取消订阅个人消息队列')
        unsubscribe(personalSubscriptionIdRef.current)
      }
    }
  }, [subscribeToPersonalMessages, unsubscribe, isConnected])

  // 订阅答题阶段就绪消息
  useEffect(() => {
    if (!subscribe || !isConnected || !gameData?.gameId) {
      console.log('[Discussion] WebSocket未连接或未提供订阅方法或gameId不存在')
      return
    }

    const gameId = gameData.gameId
    const topic = `/topic/game/${gameId}/phase`
    
    console.log('[Discussion] 开始订阅答题阶段就绪消息, topic:', topic)
    
    phaseSubscriptionIdRef.current = subscribe(topic, (message) => {
      console.log('[Discussion] 收到答题阶段消息:', message)
      
      // 检查是否为答题阶段就绪消息
      if (message.nodeName === 'ANSWER_PHASE' && message.isReady === true) {
        console.log('[Discussion] 答题阶段已就绪，自动切换到投票Tab')
        
        // 自动切换到投票Tab
        setActiveTab('vote')
        
        // 可选：显示提示消息
        setMessages(prev => [...prev, {
          id: `system-${Date.now()}`,
          sender: '系统',
          content: '答题阶段已开始，请前往投票Tab进行投票',
          time: formatTime(new Date()),
          isAI: false,
          isSystem: true,
        }])
      }
    })

    return () => {
      if (phaseSubscriptionIdRef.current && unsubscribe) {
        console.log('[Discussion] 取消订阅答题阶段就绪消息')
        unsubscribe(phaseSubscriptionIdRef.current)
      }
    }
  }, [subscribe, unsubscribe, isConnected, gameData?.gameId, formatTime])

  // 滚动到最新消息
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({behavior: 'smooth'})
  }, [messages])

  // 发送消息
  const handleSendMessage = useCallback(() => {
    if (!inputText.trim()) return

    const content = inputText.trim()
    
    // 添加本地消息显示
    const newMessage = {
      id: `msg-${Date.now()}`,
      sender: currentPlayer?.name || '你',
      senderId: currentPlayerId,
      content: content,
      time: formatTime(new Date()),
      isAI: false,
      isSystem: false,
    }
    
    console.log('[Discussion] 发送消息:', newMessage)
    setMessages(prev => [...prev, newMessage])
    setInputText('')
    
    // 发送到后端
    if (sendChatMessage) {
      console.log('[Discussion] 通过WebSocket发送消息到后端')
      sendChatMessage(content)
    } else {
      console.warn('[Discussion] sendChatMessage方法未提供，无法发送消息到后端')
    }
    
    // 调用onAction通知父组件
    onAction?.('send_chat', {message: content})
  }, [inputText, sendChatMessage, onAction, currentPlayer, currentPlayerId, formatTime])

  // 提交投票
  const handleVote = useCallback(() => {
    if (!selectedTarget || !voteMessage.trim()) return
    
    console.log('[Discussion] 提交投票:', {
      targetId: selectedTarget,
      voteMessage: voteMessage.trim(),
      currentPlayerId,
    })
    
    // 清除之前的反馈消息
    setVoteFeedback(null)
    
    // 发送投票到后端
    // 消息格式：{ type: 'VOTE_SUBMIT', payload: { targetId, playerId, voteMessage } }
    if (sendVote) {
      console.log('[Discussion] 通过WebSocket发送投票到后端')
      sendVote({
        type: 'VOTE_SUBMIT',
        payload: {
          targetId: selectedTarget,
          playerId: currentPlayerId,
          voteMessage: voteMessage.trim(),
        },
      })
    } else {
      console.warn('[Discussion] sendVote方法未提供，无法发送投票到后端')
      // 如果没有提供 sendVote 方法，则本地更新状态（兼容旧逻辑）
      setHasVoted(true)
    }
    
    // 调用onAction通知父组件
    onAction?.('vote_cast', {
      targetId: selectedTarget,
      playerId: currentPlayerId,
      voteMessage: voteMessage.trim(),
    })
  }, [sendVote, onAction, selectedTarget, voteMessage, currentPlayerId])

  // 完成讨论阶段
  const handleComplete = useCallback(() => {
    console.log('[Discussion] 完成讨论阶段')
    onAction?.('discussion_complete', {messageCount: messages.length, hasVoted})
    onComplete?.()
  }, [onAction, onComplete, messages.length, hasVoted])

  return (
      <div className="h-full relative overflow-hidden">
        {/* 背景装饰 */}
        <div className="absolute inset-0 pointer-events-none">
          <BackgroundDecor/>
        </div>

        {/* 主内容 */}
        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-8 relative z-10"
        >
          {/* 标题区 */}
          <motion.div variants={itemVariants} className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <div>
                <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                  讨论投票
                </h2>
                <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
                  {isObserverMode ? '观察者模式 - 仅查看' : hasVoted ? '已投票' : '推理讨论，投票指认'}
                </p>
              </div>
              {/* 观察者模式标识 */}
              {isObserverMode && (
                <motion.div
                  initial={{opacity: 0, scale: 0.8}}
                  animate={{opacity: 1, scale: 1}}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#7C8CD6]/10 border border-[#7C8CD6]/20"
                >
                  <Eye className="w-4 h-4 text-[#7C8CD6]"/>
                  <span className="text-xs text-[#7C8CD6] font-medium">观察者模式</span>
                </motion.div>
              )}
            </div>
            {/* 观察者模式下隐藏标签切换器 */}
            {!isObserverMode && (
              <TabSwitcher activeTab={activeTab} onChange={setActiveTab} hasVoted={hasVoted}/>
            )}
          </motion.div>

          {/* 主内容区 */}
          <div className="flex-1 min-h-0 flex gap-6">
            {/* 左侧：公开线索区域（列表 + 详情） */}
            <motion.nav variants={itemVariants} className="w-80 flex-none hidden md:flex flex-col gap-3">
              {/* 线索列表 */}
              <div
                  className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
                <div className="flex items-center gap-2 mb-3 px-1">
                  <FileText className="w-4 h-4 text-[#7C8CD6]"/>
                  <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider">
                    公开线索
                  </p>
                  <span className="ml-auto text-[10px] px-2 py-0.5 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6]">
                  {publicClues.length}
                </span>
                </div>
                <div className="flex-1 overflow-y-auto scrollbar-thin space-y-2 pr-1">
                  {publicClues.map((clue, index) => (
                      <ClueListItem key={clue.id} clue={clue} index={index} onHover={setHoveredClue}/>
                  ))}
                </div>
            </div>

              {/* 线索详情面板 */}
              <div
                  className="h-40 bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex flex-col">
                <p className="text-[#8C96A5] dark:text-[#6B7788] text-[10px] font-medium uppercase tracking-wider mb-2">
                  线索详情
                </p>
                <AnimatePresence mode="wait">
                  {hoveredClue ? (
                      <motion.div
                          key="clue-details"
                          initial={{opacity: 0}}
                          animate={{opacity: 1}}
                          exit={{opacity: 0}}
                          className="flex-1 flex flex-col"
                      >
                        <div className="flex items-center gap-2 mb-2">
                          <div
                              className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7C8CD6]/20 to-[#A78BFA]/20 flex items-center justify-center">
                            <FileText className="w-4 h-4 text-[#7C8CD6]"/>
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-[#2D3748] dark:text-[#E8ECF2] text-xs font-medium truncate">
                              {hoveredClue.name}
                            </p>
                            <span className="text-[10px] text-[#7C8CD6]">
                          {hoveredClue.type}
                        </span>
                          </div>
                        </div>
                        <p className="text-[#5A6978] dark:text-[#9CA8B8] text-xs leading-relaxed flex-1 overflow-y-auto scrollbar-thin">
                          这是一条重要的{hoveredClue.type}线索，可能与案件的关键环节有关。仔细观察，或许能发现隐藏的真相。
                        </p>
                      </motion.div>
                  ) : (
                      <motion.div
                          key="clue-placeholder"
                          initial={{opacity: 0}}
                          animate={{opacity: 1}}
                          exit={{opacity: 0}}
                          className="flex-1 flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]"
                      >
                        <FileText className="w-6 h-6 mb-1 opacity-50"/>
                        <p className="text-xs">悬停查看线索详情</p>
                      </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </motion.nav>

            {/* 右侧：讨论/投票区域 */}
            <motion.div variants={itemVariants} className="flex-1 min-w-0 flex flex-col relative">
              {/* 卡片光晕 */}
              <div
                  className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

              <div
                  className="relative flex-1 min-h-0 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden flex flex-col">
                {/* 顶部渐变线 */}
                <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

                <AnimatePresence mode="wait">
                  {/* 观察者模式：显示讨论消息和投票结果 */}
                  {isObserverMode ? (
                    <motion.div
                      key="observer-mode"
                      initial={{opacity: 0}}
                      animate={{opacity: 1}}
                      exit={{opacity: 0}}
                      transition={{duration: 0.2}}
                      className="flex-1 min-h-0 flex flex-col"
                    >
                      {/* 消息列表 */}
                      <div className="flex-1 min-h-0 overflow-y-auto scrollbar-thin p-4 space-y-3">
                        {messages.length === 0 ? (
                          <div className="h-full min-h-[200px] flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]">
                            <div className="w-12 h-12 rounded-xl bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center mb-2">
                              <MessageCircle className="w-5 h-5 text-[#7C8CD6]"/>
                            </div>
                            <p className="text-xs">讨论即将开始...</p>
                            <p className="text-[10px] mt-0.5 opacity-60">观察者模式 - 仅查看</p>
                          </div>
                        ) : (
                          <>
                            {messages.map((msg) => (
                              <MessageBubble key={msg.id} message={msg} isSelf={false}/>
                            ))}
                            <div ref={messagesEndRef} className="h-0"/>
                          </>
                        )}
                      </div>

                      {/* 投票结果面板 */}
                      {voteResults.length > 0 && (
                        <div className="border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/40 dark:bg-[#222631]/40">
                          <div className="p-4">
                            <div className="flex items-center gap-2 mb-3">
                              <Vote className="w-4 h-4 text-[#F5A9C9]"/>
                              <h3 className="text-sm font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                                投票结果
                              </h3>
                              <span className="ml-auto text-[10px] px-2 py-0.5 rounded-full bg-[#F5A9C9]/10 text-[#F5A9C9]">
                                {voteResults.length} 票
                              </span>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-60 overflow-y-auto scrollbar-thin">
                              {voteResults.map((vote, index) => (
                                <VoteResultCard key={index} vote={vote} index={index}/>
                              ))}
                            </div>
                          </div>
                        </div>
                      )}
                    </motion.div>
                  ) : (
                    // 玩家模式：原有的讨论和投票界面
                    activeTab === 'discussion' ? (
                      <motion.div
                        key="discussion"
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        exit={{opacity: 0}}
                        transition={{duration: 0.2}}
                        className="flex-1 min-h-0 flex flex-col"
                      >
                        {/* 消息列表 */}
                        <div className="flex-1 min-h-0 overflow-y-auto scrollbar-thin p-4 space-y-3">
                          {messages.length === 0 ? (
                            <div className="h-full min-h-[200px] flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]">
                              <div className="w-12 h-12 rounded-xl bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center mb-2">
                                <MessageCircle className="w-5 h-5 text-[#7C8CD6]"/>
                              </div>
                              <p className="text-xs">讨论即将开始...</p>
                              <p className="text-[10px] mt-0.5 opacity-60">分享你的推理</p>
                            </div>
                          ) : (
                            <>
                              {messages.map((msg) => (
                                <MessageBubble key={msg.id} message={msg} isSelf={!msg.isAI}/>
                              ))}
                              <div ref={messagesEndRef} className="h-0"/>
                            </>
                          )}
                        </div>

                        {/* 输入框区域 */}
                        <div className="p-3 border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/60 dark:bg-[#222631]/60">
                          <div className="flex gap-2">
                            <input
                              type="text"
                              value={inputText}
                              onChange={(e) => setInputText(e.target.value)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                  handleSendMessage()
                                }
                              }}
                              placeholder="输入你的推理..."
                              className="flex-1 px-4 py-2.5 rounded-lg bg-white dark:bg-[#1A1D26] border border-[#E0E5EE] dark:border-[#363D4D] text-[#2D3748] dark:text-[#E8ECF2] placeholder-[#8C96A5] focus:outline-none focus:border-[#7C8CD6] text-sm"
                            />
                            <GhostButton
                              onClick={handleSendMessage}
                              disabled={!inputText.trim()}
                              className={!inputText.trim() ? 'opacity-40' : ''}
                            >
                              <Send className="w-4 h-4"/>
                            </GhostButton>
                          </div>
                        </div>
                      </motion.div>
                    ) : (
                      <motion.div
                        key="vote"
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        exit={{opacity: 0}}
                        transition={{duration: 0.2}}
                        className="flex-1 min-h-0 flex flex-col p-5"
                      >
                        {/* 投票标题 */}
                        <div className="flex items-center justify-between mb-4 pb-4 border-b border-[#E0E5EE] dark:border-[#363D4D]">
                          <div>
                            <h3 className="text-lg font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                              投票指认
                            </h3>
                            <p className="text-sm text-[#8C96A5] dark:text-[#6B7788] mt-1">
                              选择你认为的凶手
                            </p>
                          </div>
                          {hasVoted && (
                            <span className="px-3 py-1 rounded-full bg-[#5DD9A8]/10 text-[#5DD9A8] text-xs font-medium border border-[#5DD9A8]/20">
                              已投票
                            </span>
                          )}
                        </div>

                        {/* 投票反馈提示 */}
                        <AnimatePresence>
                          {voteFeedback && (
                            <motion.div
                              initial={{opacity: 0, y: -10}}
                              animate={{opacity: 1, y: 0}}
                              exit={{opacity: 0, y: -10}}
                              className={`mb-4 p-3 rounded-lg border ${
                                voteFeedback.type === 'success'
                                  ? 'bg-[#5DD9A8]/10 border-[#5DD9A8]/30 text-[#5DD9A8]'
                                  : 'bg-[#F87171]/10 border-[#F87171]/30 text-[#F87171]'
                              }`}
                            >
                              <div className="flex items-center gap-2">
                                <span className="text-sm font-medium">
                                  {voteFeedback.type === 'success' ? '✓' : '✕'}
                                </span>
                                <span className="text-sm">{voteFeedback.message}</span>
                              </div>
                            </motion.div>
                          )}
                        </AnimatePresence>

                        {/* 左右布局：左侧候选人列表，右侧详情和答题输入 */}
                        <div className="flex-1 min-h-0 flex gap-4">
                          {/* 左侧：候选人列表（单列） */}
                          <div className="w-1/2 min-h-0 overflow-y-auto scrollbar-thin pr-1">
                            <div className="flex flex-col gap-3">
                              {otherPlayers.map((player) => (
                                <CandidateCard
                                  key={player.playerId || player.id}
                                  player={player}
                                  isSelected={selectedTarget === (player.playerId || player.id)}
                                  hasVoted={hasVoted}
                                  onVote={(playerId) => setSelectedTarget(playerId)}
                                  onHover={setHoveredPlayer}
                                />
                              ))}
                            </div>
                          </div>

                          {/* 右侧：详情面板和答题输入 */}
                          <div className="w-1/2 min-h-0 flex flex-col gap-4">
                            {/* 人物详情 */}
                            <AnimatePresence mode="wait">
                              {hoveredPlayer ? (
                                <motion.div
                                  key="details"
                                  initial={{opacity: 0, x: 10}}
                                  animate={{opacity: 1, x: 0}}
                                  exit={{opacity: 0, x: -10}}
                                  transition={{duration: 0.2}}
                                  className="flex-1 rounded-xl bg-white/80 dark:bg-[#222631]/80 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] p-4 flex flex-col"
                                >
                                  <div className="flex items-center gap-3 mb-4 pb-3 border-b border-[#E0E5EE] dark:border-[#363D4D]">
                                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] flex items-center justify-center text-white font-bold text-lg">
                                      {hoveredPlayer.name.charAt(0)}
                                    </div>
                                    <div>
                                      <h4 className="font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                                        {hoveredPlayer.name}
                                      </h4>
                                      <p className="text-sm text-[#8C96A5] dark:text-[#6B7788]">
                                        {hoveredPlayer.role}
                                      </p>
                                    </div>
                                  </div>
                                  <div className="flex-1">
                                    <p className="text-xs text-[#8C96A5] dark:text-[#6B7788] mb-2 uppercase tracking-wider">
                                      人物简介
                                    </p>
                                    <p className="text-sm text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed">
                                      {hoveredPlayer.isAI
                                        ? `这是一位由AI控制的角色，在游戏中扮演${hoveredPlayer.role}。仔细观察TA的言行举止，找出破绽。`
                                        : '这是你自己，作为调查员你需要揭露真相。'
                                      }
                                    </p>
                                  </div>
                                </motion.div>
                              ) : (
                                <motion.div
                                  key="placeholder"
                                  initial={{opacity: 0}}
                                  animate={{opacity: 1}}
                                  exit={{opacity: 0}}
                                  className="flex-1 rounded-xl bg-white/40 dark:bg-[#222631]/40 backdrop-blur-sm border border-dashed border-[#E0E5EE] dark:border-[#363D4D] flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]"
                                >
                                  <User className="w-8 h-8 mb-2 opacity-50"/>
                                  <p className="text-sm">悬停查看嫌疑人详情</p>
                                </motion.div>
                              )}
                            </AnimatePresence>

                            {/* 答题输入区域 */}
                            <div className="rounded-xl bg-white/80 dark:bg-[#222631]/80 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] p-4">
                              <p className="text-sm font-medium text-[#2D3748] dark:text-[#E8ECF2] mb-2">
                                详细答题
                              </p>
                              <textarea
                                value={voteMessage}
                                onChange={(e) => setVoteMessage(e.target.value)}
                                placeholder="请详细描述你的推理过程和投票理由..."
                                className="w-full px-3 py-2.5 rounded-lg bg-white dark:bg-[#1A1D26] border border-[#E0E5EE] dark:border-[#363D4D] text-[#2D3748] dark:text-[#E8ECF2] placeholder-[#8C96A5] focus:outline-none focus:border-[#7C8CD6] text-sm h-32 resize-none"
                                disabled={hasVoted}
                              />
                              <div className="mt-3 flex justify-end">
                                <GhostButton
                                  onClick={handleVote}
                                  disabled={!selectedTarget || !voteMessage.trim() || hasVoted}
                                  className={(!selectedTarget || !voteMessage.trim() || hasVoted) ? 'opacity-40' : ''}
                                >
                                  提交投票
                                </GhostButton>
                              </div>
                            </div>
                          </div>
                        </div>
                      </motion.div>
                    )
                  )}
                </AnimatePresence>

                {/* 底部导航 */}
                <div className="p-4 border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/40 dark:bg-[#222631]/40 flex items-center justify-between">
                  <span className="text-xs text-[#8C96A5] dark:text-[#6B7788]">
                    {isObserverMode ? '观察者模式' : `${messages.length} 条消息`}
                  </span>
                  <GhostButton
                    onClick={handleComplete}
                    disabled={!isObserverMode && !hasVoted}
                    className={!isObserverMode && !hasVoted ? 'opacity-40' : ''}
                  >
                    <span className="flex items-center gap-1">
                      {isObserverMode ? '结束观察' : hasVoted ? '结束讨论' : '请先完成投票'}
                      {(isObserverMode || hasVoted) && (
                        <motion.span
                          animate={{x: [0, 4, 0]}}
                          transition={{duration: 1.5, repeat: Infinity}}
                        >
                          <ChevronRight className="w-4 h-4"/>
                        </motion.span>
                      )}
                    </span>
                  </GhostButton>
                </div>
            </div>
            </motion.div>
          </div>
        </motion.div>
      </div>
  )
}

Discussion.displayName = 'Discussion'
Discussion.phaseType = PHASE_TYPE.DISCUSSION

export default memo(Discussion)

/**
 * @fileoverview Discussion 组件 - 讨论投票阶段
 * @description 重构后的讨论投票阶段组件，采用模块化设计
 * - 透明背景 + 玻璃态卡片
 * - 支持玩家模式和观察者模式
 * @author zewang
 */

import {memo, useCallback, useEffect, useMemo, useState} from 'react'
import PropTypes from 'prop-types'
import {AnimatePresence, motion} from 'framer-motion'
import {ChevronRight, Eye, FileText, MessageCircle, Send, User, Vote} from 'lucide-react'
import {PHASE_TYPE} from '../../types'
import {containerVariants, itemVariants} from '../../config/animations'
import {PHASE_COLORS} from '../../config/theme'
import GhostButton from '../../../../components/ui/GhostButton'
import PhaseBackgroundDecor from '../../../../components/common/PhaseBackgroundDecor'
import {DiscussionProps} from '../../types/props'

import {
  ClueListItem,
  MessageBubble,
  CandidateCard,
  VoteResultCard,
  TabSwitcher,
} from './components'
import {useDiscussionWebSocket, useDiscussionState} from './hooks'

/**
 * 线索详情面板组件
 * @param {Object} props - 组件属性
 * @param {Object} props.hoveredClue - 当前悬停的线索
 * @returns {JSX.Element} 线索详情面板
 */
const ClueDetailPanel = memo(({hoveredClue}) => (
  <div className="h-40 bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex flex-col">
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
              className="w-8 h-8 rounded-lg flex items-center justify-center"
              style={{
                background: `linear-gradient(to bottom right, ${PHASE_COLORS.primary}20, ${PHASE_COLORS.secondary}20)`
              }}
            >
              <FileText className="w-4 h-4" style={{color: PHASE_COLORS.primary}}/>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-[#2D3748] dark:text-[#E8ECF2] text-xs font-medium truncate">
                {hoveredClue.name}
              </p>
              <span className="text-[10px]" style={{color: PHASE_COLORS.primary}}>
                {hoveredClue.type}
              </span>
            </div>
          </div>
          <p className="text-[#5A6978] dark:text-[#9CA8B8] text-xs leading-relaxed flex-1 overflow-y-auto scrollbar-thin">
            {hoveredClue?.description || 
              `这是一条重要的${hoveredClue?.type || '未知'}线索，可能与案件的关键环节有关。仔细观察，或许能发现隐藏的真相。`
            }
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
))

ClueDetailPanel.displayName = 'ClueDetailPanel'

/**
 * 玩家详情面板组件
 * @param {Object} props - 组件属性
 * @param {Object} props.hoveredPlayer - 当前悬停的玩家
 * @returns {JSX.Element} 玩家详情面板
 */
const PlayerDetailPanel = memo(({hoveredPlayer}) => (
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
          <div 
            className="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-lg"
            style={{
              background: `linear-gradient(to bottom right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary})`
            }}
          >
            {(hoveredPlayer?.name || hoveredPlayer?.characterName || '?').charAt(0)}
          </div>
          <div>
            <h4 className="font-bold text-[#2D3748] dark:text-[#E8ECF2]">
              {hoveredPlayer?.name || hoveredPlayer?.characterName || '未知玩家'}
            </h4>
            <p className="text-sm text-[#8C96A5] dark:text-[#6B7788]">
              {hoveredPlayer?.characterName || hoveredPlayer?.name || '角色'}
            </p>
          </div>
        </div>
        <div className="flex-1">
          <p className="text-xs text-[#8C96A5] dark:text-[#6B7788] mb-2 uppercase tracking-wider">
            人物简介
          </p>
          <div className="h-full overflow-y-auto scrollbar-thin pr-1 max-h-40">
            <p className="text-sm text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed whitespace-pre-wrap">
              {hoveredPlayer?.description || hoveredPlayer?.backgroundStory || 
                (hoveredPlayer?.isAI
                  ? `这是一位由AI控制的角色，在游戏中扮演${hoveredPlayer?.characterName || hoveredPlayer?.name || '某个角色'}。仔细观察TA的言行举止，找出破绽。`
                  : '这是你自己，作为调查员你需要揭露真相。'
                )
              }
            </p>
          </div>
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
))

PlayerDetailPanel.displayName = 'PlayerDetailPanel'

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
 * @param {Function} props.subscribe - 通用订阅方法
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
  const {
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
    messagesEndRef,
  } = useDiscussionState()
  
  // 观察者模式：玩家答案消息列表
  const [playerAnswers, setPlayerAnswers] = useState([])

  const publicClues = gameData?.publicClues || []
  const players = gameData?.players || []

  const realPlayerIds = useMemo(() => {
    const realPlayers = playerData?.filter(p => p.playerRole === 'REAL') || []
    return realPlayers.map(p => p.playerId)
  }, [playerData])

  const currentPlayer = useMemo(() => {
    return players.find(p => p.playerId === currentPlayerId) || players.find(p => p.isSelf)
  }, [players, currentPlayerId])

  const otherPlayers = useMemo(() => {
    const uniquePlayers = new Map()
    
    players.forEach(p => {
      if (p.playerId === currentPlayerId || p.isSelf) return
      if (p.isDm === true || p.playerRole === 'DM') return
      if (p.playerRole === 'JUDGE') return
      
      if (!uniquePlayers.has(p.playerId)) {
        uniquePlayers.set(p.playerId, p)
      }
    })
    
    return Array.from(uniquePlayers.values())
  }, [players, currentPlayerId])

  const formatTime = useCallback((date) => {
    return date.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})
  }, [])

  const getPlayerNameById = useCallback((playerId) => {
    const player = players.find(p => p.playerId === playerId || p.id === playerId)
    return player?.name || player?.characterName || `玩家${playerId}`
  }, [players])

  const voteResults = useMemo(() => {
    if (!isObserverMode) return []
    
    const votes = gameData?.votes || gameData?.playerVotes || []
    
    return votes.map(vote => ({
      voterName: getPlayerNameById(vote.voterId || vote.playerId),
      voterRole: players.find(p => p.playerId === (vote.voterId || vote.playerId))?.role || '角色',
      targetName: getPlayerNameById(vote.targetId),
      voteMessage: vote.voteMessage || vote.reason || '',
    }))
  }, [isObserverMode, gameData, players, getPlayerNameById])

  useDiscussionWebSocket({
    gameId: gameData?.gameId,
    currentPlayerId,
    realPlayerIds,
    subscribeToGameChat,
    subscribeToPersonalMessages,
    subscribe,
    unsubscribe,
    onChatMessage: addMessage,
    onVoteResult: (result) => {
      setFeedback(result)
      if (result.type === 'success') {
        setHasVoted(true)
      }
    },
    onPhaseReady: (data) => {
      setActiveTab('vote')
      addSystemMessage(data.message, formatTime)
    },
    onPlayerAnswer: (answerData) => {
      // 观察者模式：添加玩家答案到列表
      console.log('[Discussion] 收到玩家答案:', answerData)
      setPlayerAnswers(prev => {
        // 避免重复添加
        if (prev.some(a => a.playerId === answerData.playerId)) {
          return prev
        }
        return [...prev, answerData]
      })
    },
    getPlayerNameById,
    formatTime,
    isConnected,
    isObserverMode,
  })

  const handleSendMessage = useCallback(() => {
    if (!inputText.trim()) return

    const content = inputText.trim()
    
    const newMessage = {
      id: `msg-${Date.now()}`,
      sender: currentPlayer?.name || '你',
      senderId: currentPlayerId,
      content: content,
      time: formatTime(new Date()),
      isAI: false,
      isSystem: false,
    }
    
    addMessage(newMessage)
    clearInput()
    
    if (sendChatMessage) {
      sendChatMessage(content)
    }
    
    onAction?.('send_chat', {message: content})
  }, [inputText, sendChatMessage, onAction, currentPlayer, currentPlayerId, formatTime, addMessage, clearInput])

  const handleVote = useCallback(() => {
    if (!selectedTarget || !voteMessage.trim()) return
    
    clearFeedback()
    
    if (sendVote) {
      sendVote({
        type: 'VOTE_SUBMIT',
        payload: {
          targetId: selectedTarget,
          playerId: currentPlayerId,
          voteMessage: voteMessage.trim(),
        },
      })
    } else {
      setHasVoted(true)
    }
    
    onAction?.('vote_cast', {
      targetId: selectedTarget,
      playerId: currentPlayerId,
      voteMessage: voteMessage.trim(),
    })
  }, [sendVote, onAction, selectedTarget, voteMessage, currentPlayerId, clearFeedback])

  const handleComplete = useCallback(() => {
    onAction?.('discussion_complete', {messageCount: messages.length, hasVoted})
    onComplete?.()
  }, [onAction, onComplete, messages.length, hasVoted])

  return (
    <div className="h-full relative overflow-hidden">
      <div className="absolute inset-0 pointer-events-none">
        <PhaseBackgroundDecor/>
      </div>

      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        className="h-full flex flex-col p-8 relative z-10"
      >
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
            {isObserverMode && (
              <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg border"
                style={{
                  backgroundColor: `${PHASE_COLORS.primary}10`,
                  borderColor: `${PHASE_COLORS.primary}20`
                }}
              >
                <Eye className="w-4 h-4" style={{color: PHASE_COLORS.primary}}/>
                <span className="text-xs font-medium" style={{color: PHASE_COLORS.primary}}>观察者模式</span>
              </motion.div>
            )}
          </div>
          {!isObserverMode && (
            <TabSwitcher activeTab={activeTab} onChange={setActiveTab} hasVoted={hasVoted}/>
          )}
        </motion.div>

        <div className="flex-1 min-h-0 flex gap-6">
          <motion.nav variants={itemVariants} className="w-80 flex-none hidden md:flex flex-col gap-3">
            <div className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
              <div className="flex items-center gap-2 mb-3 px-1">
                <FileText className="w-4 h-4" style={{color: PHASE_COLORS.primary}}/>
                <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider">
                  公开线索
                </p>
                <span 
                  className="ml-auto text-[10px] px-2 py-0.5 rounded-full"
                  style={{
                    backgroundColor: `${PHASE_COLORS.primary}10`,
                    color: PHASE_COLORS.primary
                  }}
                >
                  {publicClues.length}
                </span>
              </div>
              <div className="flex-1 overflow-y-auto scrollbar-thin space-y-2 pr-1">
                {publicClues.map((clue, index) => (
                  <ClueListItem key={clue.id} clue={clue} index={index} onHover={setHoveredClue}/>
                ))}
              </div>
            </div>
            <ClueDetailPanel hoveredClue={hoveredClue}/>
          </motion.nav>

          <motion.div variants={itemVariants} className="flex-1 min-w-0 flex flex-col relative">
            <div
              className="absolute -inset-0.5 rounded-2xl blur-lg opacity-50"
              style={{
                background: `linear-gradient(to right, ${PHASE_COLORS.primary}20, ${PHASE_COLORS.secondary}20)`
              }}
            />

            <div className="relative flex-1 min-h-0 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden flex flex-col">
              <div 
                className="h-1"
                style={{
                  background: `linear-gradient(to right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary}, ${PHASE_COLORS.accent})`
                }}
              />

              <AnimatePresence mode="wait">
                {isObserverMode ? (
                  <motion.div
                    key="observer-mode"
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    exit={{opacity: 0}}
                    transition={{duration: 0.2}}
                    className="flex-1 min-h-0 flex flex-col"
                  >
                    <div className="flex-1 min-h-0 overflow-y-auto scrollbar-thin p-4 space-y-3">
                      {messages.length === 0 ? (
                        <div className="h-full min-h-[200px] flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]">
                          <div className="w-12 h-12 rounded-xl bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center mb-2">
                            <MessageCircle className="w-5 h-5" style={{color: PHASE_COLORS.primary}}/>
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

                    {voteResults.length > 0 && (
                      <div className="border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/40 dark:bg-[#222631]/40">
                        <div className="p-4">
                          <div className="flex items-center gap-2 mb-3">
                            <Vote className="w-4 h-4" style={{color: PHASE_COLORS.accent}}/>
                            <h3 className="text-sm font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                              投票结果
                            </h3>
                            <span 
                              className="ml-auto text-[10px] px-2 py-0.5 rounded-full"
                              style={{
                                backgroundColor: `${PHASE_COLORS.accent}10`,
                                color: PHASE_COLORS.accent
                              }}
                            >
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
                ) : activeTab === 'discussion' ? (
                  <motion.div
                    key="discussion"
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    exit={{opacity: 0}}
                    transition={{duration: 0.2}}
                    className="flex-1 min-h-0 flex flex-col"
                  >
                    <div className="flex-1 min-h-0 overflow-y-auto scrollbar-thin p-4 space-y-3">
                      {messages.length === 0 ? (
                        <div className="h-full min-h-[200px] flex flex-col items-center justify-center text-[#8C96A5] dark:text-[#6B7788]">
                          <div className="w-12 h-12 rounded-xl bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center mb-2">
                            <MessageCircle className="w-5 h-5" style={{color: PHASE_COLORS.primary}}/>
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
                    {/* 观察者模式：玩家答案消息横幅 */}
                    {isObserverMode && playerAnswers.length > 0 && (
                      <motion.div
                        initial={{opacity: 0, y: -10}}
                        animate={{opacity: 1, y: 0}}
                        className="mb-4 bg-gradient-to-r from-[#7C8CD6]/5 to-[#A78BFA]/5 rounded-xl border border-[#7C8CD6]/20 overflow-hidden"
                      >
                        <div className="p-3 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl">
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <div className="w-6 h-6 rounded-lg bg-[#7C8CD6]/10 flex items-center justify-center">
                                <Vote className="w-3.5 h-3.5 text-[#7C8CD6]"/>
                              </div>
                              <h4 className="text-sm font-semibold text-[#2D3748] dark:text-[#E8ECF2]">
                                玩家投票动态
                              </h4>
                            </div>
                            <span className="text-xs text-[#8C96A5]">
                              {playerAnswers.length} / {players.filter(p => p.playerRole !== 'DM' && p.playerRole !== 'JUDGE').length} 人已投票
                            </span>
                          </div>
                          
                          {/* 消息列表 */}
                          <div className="max-h-32 overflow-y-auto scrollbar-thin space-y-1.5">
                            <AnimatePresence mode="pop">
                              {playerAnswers.slice(-5).reverse().map((answer) => (
                                <motion.div
                                  key={answer.id}
                                  initial={{opacity: 0, x: -20}}
                                  animate={{opacity: 1, x: 0}}
                                  exit={{opacity: 0, x: 20}}
                                  className={`p-2 rounded-lg ${
                                    answer.isAI 
                                      ? 'bg-white/50 dark:bg-[#222631]/50 border border-[#E0E5EE]/50 dark:border-[#363D4D]/50' 
                                      : 'bg-[#5DD9A8]/10 border border-[#5DD9A8]/20'
                                  }`}
                                >
                                  <div className="flex items-start gap-2">
                                    {/* 状态图标 */}
                                    <div className={`w-5 h-5 rounded flex items-center justify-center flex-shrink-0 mt-0.5 ${
                                      answer.isAI ? 'bg-[#7C8CD6]/20' : 'bg-[#5DD9A8]/20'
                                    }`}>
                                      <User className={`w-3 h-3 ${answer.isAI ? 'text-[#7C8CD6]' : 'text-[#5DD9A8]'}`}/>
                                    </div>
                                    
                                    <div className="flex-1 min-w-0">
                                      <div className="flex items-center gap-1.5 mb-0.5">
                                        <span className="text-xs font-medium text-[#2D3748] dark:text-[#E8ECF2]">
                                          {answer.playerName}
                                        </span>
                                        {answer.isAI && (
                                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#7C8CD6]/10 text-[#7C8CD6]">
                                            AI
                                          </span>
                                        )}
                                        <span className="text-[10px] text-[#8C96A5]">
                                          {answer.time}
                                        </span>
                                      </div>
                                      <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed line-clamp-2">
                                        {answer.answer}
                                      </p>
                                    </div>
                                  </div>
                                </motion.div>
                              ))}
                            </AnimatePresence>
                          </div>
                        </div>
                      </motion.div>
                    )}
                    
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
                        <span 
                          className="px-3 py-1 rounded-full text-xs font-medium border"
                          style={{
                            backgroundColor: `${PHASE_COLORS.success}10`,
                            color: PHASE_COLORS.success,
                            borderColor: `${PHASE_COLORS.success}20`
                          }}
                        >
                          已投票
                        </span>
                      )}
                    </div>

                    <AnimatePresence>
                      {voteFeedback && (
                        <motion.div
                          initial={{opacity: 0, y: -10}}
                          animate={{opacity: 1, y: 0}}
                          exit={{opacity: 0, y: -10}}
                          className={`mb-4 p-3 rounded-lg border ${
                            voteFeedback.type === 'success'
                              ? `bg-[${PHASE_COLORS.success}]/10 border-[${PHASE_COLORS.success}]/30 text-[${PHASE_COLORS.success}]`
                              : `bg-[#F87171]/10 border-[#F87171]/30 text-[#F87171]`
                          }`}
                          style={voteFeedback.type === 'success' ? {
                            backgroundColor: `${PHASE_COLORS.success}10`,
                            borderColor: `${PHASE_COLORS.success}30`,
                            color: PHASE_COLORS.success
                          } : {}}
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

                    <div className="flex-1 min-h-0 flex gap-4">
                      <div className="w-1/2 min-h-0 overflow-y-auto scrollbar-thin pr-1">
                        <div className="flex flex-col gap-3">
                          {otherPlayers.map((player) => (
                            <CandidateCard
                              key={player.id}
                              player={player}
                              isSelected={selectedTarget === player.playerId}
                              hasVoted={hasVoted}
                              onVote={(playerId) => setSelectedTarget(playerId)}
                              onHover={setHoveredPlayer}
                            />
                          ))}
                        </div>
                      </div>

                      <div className="w-1/2 min-h-0 flex flex-col gap-4">
                        <PlayerDetailPanel hoveredPlayer={hoveredPlayer}/>

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
                )}
              </AnimatePresence>

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
Discussion.propTypes = DiscussionProps

export default memo(Discussion)

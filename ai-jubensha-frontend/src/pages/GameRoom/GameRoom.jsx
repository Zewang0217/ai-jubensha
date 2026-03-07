/**
 * @fileoverview GameRoom 组件 - 现代扁平化 + 科技简约 + 二次元萌系风格
 * @description 游戏房间主组件，采用低饱和度冷色调设计
 *
 * 设计特点：
 * - 低饱和度冷色调背景
 * - 淡紫主强调色 + 淡粉萌系点缀
 * - 玻璃态效果
 * - 简洁扁平化设计
 */

import React, {memo, Suspense, useCallback, useEffect, useMemo, useRef, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {AnimatePresence, motion} from 'framer-motion'
import {getGameById, getGamePlayers, exitGame} from '../../services/api'
import {adaptGameData, adaptPhase} from '../../services/api/gameDataAdapter'
import Loading from '../../components/common/Loading'
import {useWebSocket} from '../../hooks/useWebSocket'
import {Bug, X} from 'lucide-react'
import {saveGameState, loadGameState, clearGameState} from '../../utils/gameStateStorage'

// 阶段系统导入
import {DEFAULT_PHASE_SEQUENCE, PHASE_CONFIG, PHASE_TYPE, usePhaseManager,} from './phases'

// 调试模式导入
import {useDebugMode} from './hooks/useDebugMode'

// 布局组件导入
import GameRoomHeader from './components/GameRoomHeader'
import GameRoomFooter from './components/GameRoomFooter'
import ExitConfirmModal from './components/ExitConfirmModal'

// =============================================================================
// 延迟加载阶段组件
// =============================================================================

const PhaseComponents = {
  [PHASE_TYPE.SCRIPT_OVERVIEW]: React.lazy(() => import('./phases/ScriptOverview')),
  [PHASE_TYPE.CHARACTER_ASSIGNMENT]: React.lazy(() => import('./phases/CharacterAssignment')),
  [PHASE_TYPE.SCRIPT_READING]: React.lazy(() => import('./phases/ScriptReading')),
  [PHASE_TYPE.INVESTIGATION]: React.lazy(() => import('./phases/Investigation')),
  [PHASE_TYPE.DISCUSSION]: React.lazy(() => import('./phases/Discussion')),
  [PHASE_TYPE.SUMMARY]: React.lazy(() => import('./phases/Summary')),
}

// =============================================================================
// 常量配置
// =============================================================================

const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080'
const DEFAULT_DEBUG_MODE = false // 默认关闭调试模式，使用真实数据

const TRANSITION_CONFIG = {
  initial: {opacity: 0, y: 8},
  animate: {opacity: 1, y: 0},
  exit: {opacity: 0, y: -8},
  transition: {duration: 0.25, ease: 'easeOut'},
}

// =============================================================================
// 风格化子组件
// =============================================================================

/**
 * 阶段加载占位符 - 简洁萌系风格
 */
const PhaseLoadingPlaceholder = memo(() => (
    <div className="h-full flex items-center justify-center">
      <div className="flex flex-col items-center gap-3">
        <div className="relative">
          <div
              className="w-12 h-12 rounded-xl bg-[var(--color-primary-100)] dark:bg-[var(--color-primary-800)]/30 flex items-center justify-center">
            <motion.div
                animate={{rotate: 360}}
                transition={{duration: 1.5, repeat: Infinity, ease: 'linear'}}
                className="w-6 h-6 border-2 border-[var(--color-primary-400)] border-t-transparent rounded-full"
            />
          </div>
        </div>
        <p className="text-[var(--color-secondary-500)] dark:text-[var(--color-secondary-400)] text-sm">加载中...</p>
      </div>
    </div>
))

PhaseLoadingPlaceholder.displayName = 'PhaseLoadingPlaceholder'

/**
 * 阶段包装器
 */
const PhaseWrapper = memo(({phase, children}) => (
    <AnimatePresence mode="popLayout" initial={false}>
      <motion.div
          key={phase}
          {...TRANSITION_CONFIG}
          className="h-full will-change-transform"
      >
        {children}
      </motion.div>
    </AnimatePresence>
))

PhaseWrapper.displayName = 'PhaseWrapper'

/**
 * 背景装饰 - 简洁几何图案
 */
const BackgroundDecoration = memo(() => {
  return (
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        {/* 基础渐变背景 */}
        <div
            className="absolute inset-0 bg-gradient-to-br from-[var(--color-primary-50)] via-[var(--color-secondary-50)] to-[var(--color-primary-100)]/30 dark:from-[var(--color-secondary-900)] dark:via-[var(--color-secondary-800)] dark:to-[var(--color-primary-900)]/20"/>

        {/* 装饰性圆点 - 左上 */}
        <div
            className="absolute top-20 left-10 w-32 h-32 rounded-full bg-[var(--color-primary-200)]/20 dark:bg-[var(--color-primary-700)]/10 blur-2xl"/>

        {/* 装饰性圆点 - 右下 */}
        <div
            className="absolute bottom-20 right-10 w-48 h-48 rounded-full bg-[var(--color-accent-100)]/30 dark:bg-[var(--color-accent-700)]/10 blur-3xl"/>

        {/* 网格图案 */}
        <div className="absolute inset-0 opacity-[0.02] dark:opacity-[0.03]"
             style={{
               backgroundImage: `linear-gradient(var(--color-secondary-800) 1px, transparent 1px), linear-gradient(90deg, var(--color-secondary-800) 1px, transparent 1px)`,
               backgroundSize: '40px 40px'
             }}
        />
      </div>
  )
})

BackgroundDecoration.displayName = 'BackgroundDecoration'

/**
 * 现代化调试面板
 */
const DebugPanel = memo(({
                               isOpen,
                               onClose,
                               currentPhase,
                               onPhaseChange,
                               isDebugMode,
                               onToggleDebug,
                             }) => {
  if (!isOpen) return null

  return (
      <motion.div
          initial={{opacity: 0, y: 16, scale: 0.96}}
          animate={{opacity: 1, y: 0, scale: 1}}
          exit={{opacity: 0, y: 16, scale: 0.96}}
          transition={{duration: 0.2}}
          className="fixed bottom-4 right-4 z-50 w-72 bg-white/90 dark:bg-[var(--color-secondary-800)]/90 backdrop-blur-xl border border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] rounded-xl shadow-lg shadow-black/5"
      >
        {/* 头部 */}
        <div
            className="flex items-center justify-between px-4 py-3 border-b border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)]">
          <div className="flex items-center gap-2">
            <Bug className="w-4 h-4 text-[var(--color-primary-500)]"/>
            <span
                className="text-sm font-medium text-[var(--color-secondary-700)] dark:text-[var(--color-secondary-200)]">调试面板</span>
          </div>
          <button
              onClick={onClose}
              className="p-1 hover:bg-[var(--color-secondary-100)] dark:hover:bg-[var(--color-secondary-700)] text-[var(--color-secondary-500)] dark:text-[var(--color-secondary-400)] transition-colors rounded-md"
          >
            <X className="w-4 h-4"/>
          </button>
        </div>

        {/* 内容 */}
        <div className="p-4 space-y-4">
          {/* 调试模式开关 */}
          <div
              className="flex items-center justify-between p-3 bg-[var(--color-secondary-50)] dark:bg-[var(--color-secondary-900)]/50 rounded-lg">
            <span
                className="text-sm text-[var(--color-secondary-600)] dark:text-[var(--color-secondary-300)]">模拟模式</span>
            <button
                onClick={onToggleDebug}
                className={`relative w-11 h-6 rounded-full transition-colors ${
                    isDebugMode ? 'bg-[var(--color-primary-500)]' : 'bg-[var(--color-secondary-300)] dark:bg-[var(--color-secondary-600)]'
                }`}
            >
              <motion.div
                  initial={false}
                  animate={{x: isDebugMode ? 22 : 2}}
                  transition={{duration: 0.2}}
                  className="absolute top-1 w-4 h-4 bg-white rounded-full shadow-sm"
              />
            </button>
          </div>

          {/* 阶段切换 */}
          {isDebugMode && (
              <>
                <div
                    className="border-t border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] pt-4">
                  <p className="text-xs text-[var(--color-secondary-500)] uppercase tracking-wider mb-3">
                    跳转阶段
                  </p>
                  <div className="grid grid-cols-2 gap-2">
                    {DEFAULT_PHASE_SEQUENCE.map((phase) => (
                        <button
                            key={phase}
                            onClick={() => onPhaseChange(phase)}
                            className={`
                      px-3 py-2 text-xs transition-all rounded-lg
                      ${currentPhase === phase
                                ? 'bg-[var(--color-primary-100)] dark:bg-[var(--color-primary-800)]/30 text-[var(--color-primary-700)] dark:text-[var(--color-primary-300)] border border-[var(--color-primary-300)] dark:border-[var(--color-primary-700)]'
                                : 'bg-[var(--color-secondary-50)] dark:bg-[var(--color-secondary-900)]/50 text-[var(--color-secondary-600)] dark:text-[var(--color-secondary-400)] border border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] hover:border-[var(--color-primary-300)] dark:hover:border-[var(--color-primary-700)]'
                            }
                    `}
                        >
                          {PHASE_CONFIG[phase]?.title}
                        </button>
                    ))}
                  </div>
                </div>

                <div
                    className="border-t border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] pt-4">
                  <p className="text-xs text-[var(--color-secondary-500)] uppercase tracking-wider mb-2">
                    当前阶段
                  </p>
                  <div
                      className="p-3 bg-[var(--color-secondary-50)] dark:bg-[var(--color-secondary-900)]/50 border border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] rounded-lg">
                    <code
                        className="text-xs text-[var(--color-primary-600)] dark:text-[var(--color-primary-400)] font-mono">{currentPhase}</code>
                  </div>
                </div>
              </>
          )}

          {!isDebugMode && (
              <p className="text-xs text-[var(--color-secondary-400)] text-center italic">
                启用模拟模式进行调试
              </p>
          )}
        </div>
      </motion.div>
  )
})

DebugPanel.displayName = 'DebugPanel'

// =============================================================================
// 主组件
// =============================================================================

function GameRoom() {
  const {id} = useParams()
  const navigate = useNavigate()
  // eslint-disable-next-line no-unused-vars
  const _queryClient = useQueryClient()

  // 调试模式
  const [showDebugPanel, setShowDebugPanel] = useState(false)
  const [showExitModal, setShowExitModal] = useState(false)

  const {
    isDebugMode,
    isLoading: debugIsLoading,
    gameData: debugGameData,
    playerData: debugPlayerData,
    isConnected: debugIsConnected,
    sendMessage: _debugSendMessage,
    sendChatMessage: debugSendChatMessage,
    sendVote: debugSendVote,
    onMessage: _debugOnMessage,
    toggleDebugMode,
    forcePhaseChange,
  } = useDebugMode({enabled: DEFAULT_DEBUG_MODE})

  // 使用 ref 存储 adaptedGameData，供 onPhaseChange 回调使用
  // 必须在 usePhaseManager 之前定义，因为 onPhaseChange 回调需要使用它
  const adaptedGameDataRef = useRef(null)
  
  // 使用 ref 存储 goToPhase 函数，避免订阅 useEffect 因 goToPhase 变化而重新执行
  const goToPhaseRef = useRef(null)

  // 阶段管理
  const {
    currentPhase,
    currentConfig,
    sequence,
    progress,
    goToNext,
    goToPrevious,
    canGoBack,
    updatePhaseData,
    getPhaseData,
    goToPhase,
    // 阶段同步相关状态
    isBackendReady: _isBackendReady,
    waitingMessage,
    isCheckingBackend: _isCheckingBackend,
    isWaitingForBackend,
    confirmCurrentPhase,
    setWaitingMessage,
    setIsBackendReady,
    handlePhaseChange,
  } = usePhaseManager({
    sequence: DEFAULT_PHASE_SEQUENCE,
    onPhaseChange: useCallback((newPhase, prevPhase) => {
      // 如果阶段没有实际变化，不执行任何操作
      if (newPhase === prevPhase) {
        console.log(`[GameRoom] 阶段未变化，跳过: ${newPhase}`)
        return
      }
      console.log(`[GameRoom] 阶段切换: ${prevPhase} -> ${newPhase}`)
      // 阶段变化时保存到 localStorage
      if (id) {
        // 使用 ref 获取最新的 adaptedGameData，避免初始化顺序问题
        const currentGameData = adaptedGameDataRef.current
        saveGameState({
          gameId: id,
          currentPhase: newPhase,
          realPlayerCount: currentGameData?.realPlayerCount,
          totalPlayerCount: currentGameData?.players?.length,
          scriptId: currentGameData?.scriptId,
        })
      }
    }, [id]),
    onComplete: useCallback(() => {
      console.log('[GameRoom] 游戏完成')
      clearGameState()
    }, []),
  })
  
  // 保持 goToPhase ref 同步
  goToPhaseRef.current = goToPhase

  // 从 localStorage 恢复游戏状态
  const [restoredPhase, setRestoredPhase] = useState(null)
  const [isStateRestored, setIsStateRestored] = useState(false)

  // 数据获取
  const {
    data: apiGameData,
    isLoading: apiIsLoading,
    error: apiError,
  } = useQuery({
    queryKey: ['game', id],
    queryFn: async () => {
      const response = await getGameById(id)
      return response?.data || response
    },
    staleTime: 30000,
    enabled: !isDebugMode && !!id,
  })

  const {
    data: apiPlayerData,
    refetch: refetchPlayerData,
  } = useQuery({
    queryKey: ['gamePlayers', id],
    queryFn: async () => {
      console.log('[GameRoom] ========== 获取玩家数据 ==========')
      console.log('[GameRoom] 游戏ID:', id)
      try {
        const response = await getGamePlayers(id)
        console.log('[GameRoom] 玩家数据响应:', response)
        console.log('[GameRoom] 响应类型:', typeof response)
        console.log('[GameRoom] 是否为数组:', Array.isArray(response))
        console.log('[GameRoom] 数组长度:', response?.length)
        console.log('[GameRoom] ==============================')
        return response?.data || response
      } catch (error) {
        console.error('[GameRoom] 获取玩家数据失败:', error)
        throw error
      }
    },
    enabled: !isDebugMode && !!id,
    staleTime: 60000,
  })

  // 使用 ref 追踪状态恢复，避免循环依赖
  const stateRestoredRef = useRef(false)

  // 从后端 API 恢复阶段（优先级高于 localStorage）
  useEffect(() => {
    if (isDebugMode || !id) return

    // 如果已经恢复过状态，不再重复执行
    if (stateRestoredRef.current) return

    // 等待 API 数据加载完成
    if (apiIsLoading) return

    // 如果 API 返回了阶段数据，使用 API 的阶段
    if (apiGameData?.currentPhase) {
      const targetPhase = adaptPhase(apiGameData.currentPhase)
      if (DEFAULT_PHASE_SEQUENCE.includes(targetPhase) && targetPhase !== currentPhase) {
        console.log('[GameRoom] 从 API 恢复阶段:', targetPhase)
        goToPhase(targetPhase, false)
      }
      stateRestoredRef.current = true
      setIsStateRestored(true)
      return
    }

    // 如果 API 没有返回阶段数据，尝试从 localStorage 恢复
    const savedState = loadGameState()
    if (savedState && savedState.gameId === id) {
      console.log('[GameRoom] 从 localStorage 恢复阶段:', savedState.currentPhase)
      setRestoredPhase(savedState.currentPhase)
    }
    stateRestoredRef.current = true
    setIsStateRestored(true)
  }, [isDebugMode, id, apiGameData, apiIsLoading, currentPhase, goToPhase])

  // 应用 localStorage 恢复的阶段（仅当 API 没有返回阶段时）
  useEffect(() => {
    if (!restoredPhase || !isStateRestored) return

    // 如果 API 已经有阶段数据，跳过 localStorage 恢复
    if (apiGameData?.currentPhase) return

    if (DEFAULT_PHASE_SEQUENCE.includes(restoredPhase)) {
      console.log('[GameRoom] 应用 localStorage 恢复的阶段:', restoredPhase)
      goToPhase(restoredPhase, false)
    }

    // 清除恢复状态
    setRestoredPhase(null)
  }, [restoredPhase, isStateRestored, apiGameData, goToPhase])

  // 适配后的游戏数据
  const [adaptedGameData, setAdaptedGameData] = useState(null)
  const [isAdapting, setIsAdapting] = useState(false)

  // 同步 adaptedGameData 到 ref
  useEffect(() => {
    adaptedGameDataRef.current = adaptedGameData
  }, [adaptedGameData])

  // 搜证完成状态
  const [isAllInvestigationComplete, setIsAllInvestigationComplete] = useState(false)

  // 数据适配 - 将后端数据转换为前端所需格式
  useEffect(() => {
    if (isDebugMode) {
      setAdaptedGameData(debugGameData)
      return
    }

    if (!apiGameData || !apiPlayerData) {
      return
    }

    const adaptData = async () => {
      setIsAdapting(true)
      try {
        const adapted = await adaptGameData(apiGameData, apiPlayerData)
        // 保留已有的 result 数据（如评分数据），避免被覆盖
        setAdaptedGameData(prev => ({
          ...adapted,
          result: prev?.result || adapted?.result || null
        }))
      } catch (err) {
        console.error('[GameRoom] Failed to adapt game data:', err)
      } finally {
        setIsAdapting(false)
      }
    }

    adaptData()
  }, [isDebugMode, apiGameData, apiPlayerData, debugGameData])

  // 合并数据
  const gameData = useMemo(() => {
    if (isDebugMode) {
      return {data: debugGameData}
    }
    return {data: adaptedGameData}
  }, [isDebugMode, debugGameData, adaptedGameData])

  const playerData = useMemo(() => {
    if (isDebugMode) {
      return {data: debugPlayerData}
    }
    return {data: apiPlayerData}
  }, [isDebugMode, debugPlayerData, apiPlayerData])

  const isLoading = isDebugMode ? debugIsLoading : (apiIsLoading || isAdapting)
  const error = isDebugMode ? null : apiError

  /**
   * 计算是否为观察者模式
   * @description 当 realPlayerCount === 0 时为观察者模式，真人用户只能查看不能操作
   * @returns {boolean} true - 观察者模式（无真人玩家），false - 真人模式（有真人玩家）
   */
  const isObserverMode = useMemo(() => {
    // 从游戏数据中获取真人玩家数量，默认值为 1（真人模式）
    const realPlayerCount = gameData?.data?.realPlayerCount ?? 1
    // 判断是否为观察者模式：真人玩家数量为 0 时为观察者模式
    const isObserver = realPlayerCount === 0
    
    // 详细的模式判断日志
    console.log('[GameRoom] ========== 模式判断 ==========')
    console.log('[GameRoom] realPlayerCount:', realPlayerCount)
    console.log('[GameRoom] 判断逻辑: realPlayerCount === 0 ?')
    console.log('[GameRoom] isObserverMode:', isObserver)
    console.log('[GameRoom] 模式类型:', isObserver ? '观察者模式（无真人玩家）' : '真人模式（有真人玩家）')
    console.log('[GameRoom] ==============================')
    
    return isObserver
  }, [gameData])

  // WebSocket - 使用新的 STOMP over SockJS 连接
  // 后端要求连接格式: /ws?gameId={gameId}
  const wsBaseUrl = WS_BASE_URL.replace('ws://', 'http://').replace('wss://', 'https://')

  const {
    isConnected: apiIsConnected,
    sendChatMessage: apiSendChatMessage,
    sendVote: apiSendVote,
    subscribeToGameChat,
    subscribeToPersonalMessages,
    subscribe,
    unsubscribe,
  } = useWebSocket(isDebugMode ? null : wsBaseUrl, isDebugMode ? null : id)

  // 获取真人玩家ID
  const currentPlayerId = useMemo(() => {
    try {
      const players = playerData?.data || playerData
      if (!Array.isArray(players) || players.length === 0) {
        console.log('[GameRoom] currentPlayerId - 玩家数据为空或不是数组')
        return null
      }
      
      // 打印第一个玩家的完整结构
      console.log('[GameRoom] currentPlayerId - 第一个玩家:', JSON.stringify(players[0], null, 2))
      
      // 打印所有玩家的 playerRole 字段
      console.log('[GameRoom] currentPlayerId - 玩家列表:', players.map(p => ({ 
        playerId: p.playerId, 
        playerRole: p.playerRole,
        id: p.id,
        role: p.role,
        player: p.player
      })))
      
      // 尝试多种方式查找真人玩家
      // 1. 直接检查 playerRole 字段
      let realPlayer = players.find(p => p.playerRole === 'REAL' || p.playerRole === 'real')
      
      // 2. 检查 player.role 字段
      if (!realPlayer) {
        realPlayer = players.find(p => p.player?.role === 'REAL' || p.player?.role === 'real')
      }
      
      // 3. 检查 role 字段
      if (!realPlayer) {
        realPlayer = players.find(p => p.role === 'REAL' || p.role === 'real')
      }
      
      console.log('[GameRoom] currentPlayerId - 找到的真人玩家:', realPlayer)
      
      // 返回真人玩家的 playerId 或 id
      return realPlayer?.playerId || realPlayer?.id || null
    } catch (e) {
      console.warn('[GameRoom] 获取真人玩家ID失败:', e)
      return null
    }
  }, [playerData])

  // 根据模式选择使用调试模式或真实 WebSocket
  const sendChatMessage = useCallback((content) => {
    if (isDebugMode) {
      return debugSendChatMessage?.(content)
    }
    // 真实模式下发送消息到后端，包含 sender（真人玩家ID）
    if (apiSendChatMessage && currentPlayerId) {
      return apiSendChatMessage({
        type: 'CHAT_MESSAGE',
        sender: currentPlayerId,
        payload: content,
      })
    }
    return false
  }, [isDebugMode, debugSendChatMessage, apiSendChatMessage, currentPlayerId])

  const sendVote = useCallback((voteData) => {
    if (isDebugMode) {
      return debugSendVote?.(voteData)
    }
    // 真实模式下发送投票消息
    // voteData 格式: { type: 'VOTE_SUBMIT', payload: { targetId, playerId, voteMessage } }
    if (apiSendVote && currentPlayerId) {
      console.log('[GameRoom] 发送投票数据:', voteData)
      return apiSendVote({
        type: voteData?.type || 'VOTE_SUBMIT',
        sender: currentPlayerId,
        payload: voteData?.payload || voteData,
      })
    }
    return false
  }, [isDebugMode, debugSendVote, apiSendVote, currentPlayerId])

  const isConnected = isDebugMode ? debugIsConnected : apiIsConnected

  // WebSocket 消息订阅
  useEffect(() => {
    if (isDebugMode || !isConnected) return

    console.log('[GameRoom] 订阅 WebSocket 消息')

    // 订阅游戏聊天消息
    const chatSubscriptionId = subscribeToGameChat((message) => {
      console.log('[GameRoom] 收到聊天消息:', message)
      // 处理聊天消息
    })

    // 订阅个人消息
    const personalSubscriptionId = subscribeToPersonalMessages((message) => {
      console.log('[GameRoom] 收到个人消息:', message)
      // 处理个人消息，如剧本就绪、开始搜证等
      if (message?.type === 'SCRIPT_READY') {
        console.log('[GameRoom] 剧本已就绪:', message.payload)
      } else if (message?.type === 'START_INVESTIGATION') {
        console.log('[GameRoom] 开始搜证:', message.payload)
        // 更新 adaptedGameData 中的搜证次数信息
        if (message.payload) {
          setAdaptedGameData(prev => ({
            ...prev,
            remainingChances: message.payload.remainingChances ?? 3,
            totalChances: message.payload.totalChances ?? 3,
            scenes: message.payload.scenes || prev.scenes,
          }))
        }
      }
    })

    return () => {
      // 清理订阅
      if (chatSubscriptionId) {
        console.log('[GameRoom] 取消聊天订阅')
      }
      if (personalSubscriptionId) {
        console.log('[GameRoom] 取消个人消息订阅')
      }
    }
  }, [isDebugMode, isConnected, subscribeToGameChat, subscribeToPersonalMessages])

  // WebSocket 监听阶段就绪消息
  useEffect(() => {
    if (isDebugMode || !isConnected || !id) return

    /**
     * 处理阶段就绪消息
     * @param {Object} data - 消息数据
     * @param {boolean} data.isReady - 是否就绪
     * @param {string} [data.message] - 等待消息
     */
    const handlePhaseReady = (data) => {
      console.log('[GameRoom] 收到阶段就绪消息:', data)
      if (data?.isReady) {
        setIsBackendReady(true)
        setWaitingMessage(null)
      } else {
        setIsBackendReady(false)
        setWaitingMessage(data?.message || '等待其他玩家...')
      }
    }

    // 订阅阶段就绪消息主题
    const phaseReadySubscriptionId = subscribe(`/topic/game/${id}/phase-ready`, handlePhaseReady)

    return () => {
      // 清理订阅
      if (phaseReadySubscriptionId) {
        console.log('[GameRoom] 取消阶段就绪订阅')
      }
    }
  }, [isDebugMode, isConnected, id, subscribe, setIsBackendReady, setWaitingMessage])

  // WebSocket 监听阶段变化消息（PHASE_CHANGE）
  useEffect(() => {
    if (isDebugMode || !isConnected || !id) return

    /**
     * 处理阶段变化消息
     * @description 后端主导模式下，前端被动接收阶段变化通知
     * @param {Object} data - 消息数据
     * @param {string} data.type - 消息类型 (PHASE_CHANGE, PHASE_READY)
     * @param {Object} data.payload - 消息负载
     */
    const handlePhaseChangeMessage = (data) => {
      console.log('[GameRoom] 收到阶段主题消息:', data)
      
      // 处理消息结构：可能是 {type: '...', payload: {...}} 或直接 {...}
      const messageType = data?.type
      const payload = data?.payload || data
      
      // 根据消息类型分发处理
      switch (messageType) {
        case 'PHASE_CHANGE':
          // 阶段变化消息 - 切换阶段
          console.log('[GameRoom] PHASE_CHANGE 消息:', payload)
          // 角色分配完成后，刷新玩家数据以获取新创建的真人玩家
          if (payload?.previousPhase === 'character_assignment' || payload?.prevPhase === 'character_assignment') {
            console.log('[GameRoom] 角色分配完成，刷新玩家数据...')
            refetchPlayerData?.()
          }
          handlePhaseChange(data)
          break
          
        case 'PHASE_READY':
          // 阶段就绪消息 - 更新等待状态
          console.log('[GameRoom] PHASE_READY 消息:', payload)
          // 如果是剧本阅读阶段，刷新玩家数据
          if (payload?.nodeName === 'script_reader' || payload?.phase === 'script_reading') {
            console.log('[GameRoom] 剧本阅读阶段，刷新玩家数据...')
            refetchPlayerData?.()
          }
          if (payload?.isReady) {
            setIsBackendReady(true)
            setWaitingMessage(null)
          } else {
            setIsBackendReady(false)
            setWaitingMessage(payload?.message || '等待其他玩家...')
          }
          break
          
        default:
          // 兼容旧格式：直接包含 newPhase 字段
          if (payload?.newPhase) {
            console.log('[GameRoom] 兼容格式 PHASE_CHANGE 消息:', payload)
            handlePhaseChange(data)
          } else {
            console.log('[GameRoom] 未知消息类型:', messageType, 'payload:', payload)
          }
      }
    }

    // 订阅阶段变化消息主题
    const phaseChangeSubscriptionId = subscribe(`/topic/game/${id}/phase`, handlePhaseChangeMessage)

    return () => {
      if (phaseChangeSubscriptionId) {
        console.log('[GameRoom] 取消阶段变化订阅')
      }
    }
  }, [isDebugMode, isConnected, id, subscribe, handlePhaseChange, refetchPlayerData, clearGameState, navigate, setWaitingMessage])

  // WebSocket 监听搜证完成消息
  useEffect(() => {
    if (isDebugMode || !isConnected || !id) return

    /**
     * 处理搜证完成消息
     * @param {Object} data - 消息数据
     * @param {string} [data.message] - 提示消息
     */
    const handleInvestigationComplete = (data) => {
      console.log('[GameRoom] 收到搜证完成通知:', data)
      setIsAllInvestigationComplete(true)
      // 显示提示
      setWaitingMessage(data?.message || '所有玩家搜证完毕，可以进入讨论阶段')
    }

    const investigationSubscriptionId = subscribe(`/topic/game/${id}/investigation`, handleInvestigationComplete)

    return () => {
      if (investigationSubscriptionId) {
        console.log('[GameRoom] 取消搜证完成订阅')
      }
    }
  }, [isDebugMode, isConnected, id, subscribe, setWaitingMessage])

  // WebSocket 监听游戏状态消息（GAME_ENDED）
  useEffect(() => {
    if (isDebugMode || !isConnected || !id) return

    /**
     * 处理游戏状态消息
     * @description 接收游戏结束消息，包含评分数据和真相揭晓内容
     * @param {Object} data - 消息数据
     */
    const handleGameStatusMessage = (data) => {
      console.log('[GameRoom] 收到游戏状态消息:', data)
      
      // 处理消息结构：可能是 {type: '...', payload: {...}} 或直接 {...}
      const messageType = data?.type
      const payload = data?.payload || data
      
      // 检查是否为游戏结束消息
      if (messageType === 'GAME_ENDED' || data?.message?.includes('游戏结束')) {
        console.log('[GameRoom] 游戏结束消息:', payload)
        
        // 提取评分数据
        const gameResult = {
          scores: payload?.scores || data?.scores || [],
          ending: payload?.ending || data?.ending || '',
          summary: payload?.summary || data?.summary || '',
          playerAnswers: payload?.playerAnswers || data?.playerAnswers || {},
          message: payload?.message || data?.message || '游戏已结束'
        }
        
        console.log('[GameRoom] 游戏结果数据:', gameResult)
        
        // 存储评分数据到 adaptedGameData
        setAdaptedGameData(prev => ({
          ...prev,
          result: gameResult
        }))
        
        // 使用 ref 调用 goToPhase，避免依赖变化导致订阅重建
        if (goToPhaseRef.current) {
          goToPhaseRef.current('summary', false)
        }
      }
    }

    // 订阅游戏状态消息主题
    const statusSubscriptionId = subscribe(`/topic/game/${id}/status`, handleGameStatusMessage)

    return () => {
      if (statusSubscriptionId && unsubscribe) {
        console.log('[GameRoom] 取消游戏状态订阅')
        unsubscribe(statusSubscriptionId)
      }
    }
  }, [isDebugMode, isConnected, id, subscribe, unsubscribe])

  // 事件处理
  /**
   * 处理阶段完成
   * @description 后端主导模式：调用后端 API 确认阶段完成，等待后端广播 PHASE_CHANGE 消息
   */
  const handlePhaseComplete = useCallback(async () => {
    console.log('[GameRoom] handlePhaseComplete 被调用，当前阶段:', currentPhase, '是否观察者模式:', isObserverMode)
    
    // 如果正在等待后端响应，不重复处理
    if (isWaitingForBackend) {
      console.log('[GameRoom] 正在等待后端响应，跳过重复请求')
      return
    }

    // 剧本概览阶段：直接进入下一阶段，不需要后端确认
    // 因为剧本概览阶段是游戏的开始阶段，后端工作流可能还没有完全启动
    if (currentPhase === 'script_overview') {
      console.log('[GameRoom] 剧本概览阶段，直接进入下一阶段')
      goToNext()
      return
    }

    // 非 script_overview 阶段：需要调用 confirmPhase API 通知后端
    console.log('[GameRoom] 当前阶段:', currentPhase, '，继续执行阶段确认逻辑...')

    // 根据阶段类型执行不同的检查逻辑
    const phaseChecks = {
      // 剧本概览、角色分配、剧本阅读：直接允许进入下一阶段
      script_overview: () => true,
      character_assignment: () => true,
      script_reading: () => true,
      
      // 搜证阶段：需要检查后端是否所有玩家完成搜证
      investigation: () => {
        // 观察者模式下，需要等待 AI 玩家完成搜证
        if (isObserverMode && !isAllInvestigationComplete) {
          setWaitingMessage('AI 玩家正在搜证，请稍候...');
          setTimeout(() => setWaitingMessage(null), 3000);
          return false;
        }
        // 真人模式下，需要等待所有玩家完成搜证
        if (!isObserverMode && !isAllInvestigationComplete) {
          setWaitingMessage('有玩家未完成搜证，请等待');
          setTimeout(() => setWaitingMessage(null), 3000);
          return false;
        }
        return true;
      },
      
      // 讨论阶段：需要检查是否完成投票（非观察者模式）
      discussion: () => {
        if (!isObserverMode) {
          // 玩家模式需要完成投票
          // 投票检查由 Discussion 组件内部处理
          // 这里直接返回 true，因为 Discussion 组件已经限制了按钮
          return true;
        }
        return true;
      },
    };

    // 执行阶段检查
    const checkFn = phaseChecks[currentPhase];
    if (checkFn && !checkFn()) {
      return;
    }

    // 获取真人玩家ID（从 playerData 中查找 playerRole 为 REAL 的玩家）
    const players = playerData?.data || playerData || [];
    console.log('[GameRoom] 所有玩家数据:', JSON.stringify(players));
    const realPlayer = players.find(p => p.playerRole === 'REAL');
    const currentPlayerId = realPlayer?.playerId;
    console.log('[GameRoom] 找到的真人玩家:', realPlayer, 'playerId:', currentPlayerId);
    console.log('[GameRoom] 阶段确认 - 真人玩家ID:', currentPlayerId, '是否观察者:', !currentPlayerId);

    if (id) {
      // 显示等待提示
      setWaitingMessage('正在等待其他玩家确认...')
      
      try {
        // 调用后端确认 API，后端会在所有玩家确认后推进阶段并广播 PHASE_CHANGE
        // 观察者模式下 currentPlayerId 为 null，后端会设置 observerConfirmed = true
        const response = await confirmCurrentPhase(id, currentPlayerId || null);
        console.log('[GameRoom] 阶段确认响应:', response);
        
        // 检查响应
        if (response?.phaseAdvanced) {
          // 后端已自动推进阶段，等待 PHASE_CHANGE 消息
          console.log('[GameRoom] 后端已推进阶段，等待 PHASE_CHANGE 消息');
        } else if (response?.allConfirmed === false) {
          // 还有玩家未确认，显示等待提示
          setWaitingMessage(`等待其他玩家确认... (${response.unconfirmedPlayers?.length || 0} 人未确认)`);
        }
      } catch (error) {
        console.error('[GameRoom] 阶段确认失败:', error);
        setWaitingMessage('阶段确认失败，请重试');
        setTimeout(() => setWaitingMessage(null), 3000);
      }
    }
  }, [confirmCurrentPhase, id, playerData, currentPhase, isAllInvestigationComplete, isObserverMode, setWaitingMessage, isWaitingForBackend, goToNext])

  // WebSocket 监听讨论超时消息
  useEffect(() => {
    if (isDebugMode || !isConnected || !id) return

    /**
     * 处理讨论超时消息
     * @param {Object} data - 消息数据
     * @param {string} [data.message] - 提示消息
     */
    const handleDiscussionTimeout = (data) => {
      console.log('[GameRoom] 收到讨论超时通知:', data)
      // 自动进入下一阶段
      handlePhaseComplete()
    }

    const discussionTimeoutSubscriptionId = subscribe(`/topic/game/${id}/discussion-timeout`, handleDiscussionTimeout)

    return () => {
      if (discussionTimeoutSubscriptionId) {
        console.log('[GameRoom] 取消讨论超时订阅')
      }
    }
  }, [isDebugMode, isConnected, id, subscribe, handlePhaseComplete])

  const handlePhaseSkip = useCallback(() => goToNext(), [goToNext])
  const handlePhaseBack = useCallback(() => goToPrevious(), [goToPrevious])

  /**
   * 处理玩家动作
   * @param {string} action - 动作类型
   * @param {*} payload - 动作数据
   */
  const handleAction = useCallback((action, payload) => {
    console.log('[GameRoom] 玩家动作:', action, payload)

    // 根据动作类型处理
    if (action === 'send_chat' && payload?.message) {
      sendChatMessage(payload.message)
    } else if (action === 'vote' && payload?.characterId) {
      sendVote(payload.characterId)
    } else if (action === 'script_reading_complete') {
      // 剧本阅读完成，调用阶段完成处理
      console.log('[GameRoom] 剧本阅读完成，调用 handlePhaseComplete...')
      handlePhaseComplete()
    } else if (action === 'investigation_complete') {
      // 搜证完成，调用阶段完成处理
      console.log('[GameRoom] 搜证完成，调用 handlePhaseComplete...')
      handlePhaseComplete()
    } else if (action === 'character_assignment_complete') {
      // 观察者模式：角色分配确认，统一使用 handlePhaseComplete 处理
      // 避免重复调用 confirmPhase API
      if (isObserverMode) {
        console.log('[GameRoom] 观察者模式角色分配确认，调用 handlePhaseComplete...')
        handlePhaseComplete()
      }
    }

    updatePhaseData(currentPhase, {[action]: payload})

    if (action === 'return_home') navigate('/')
    if (action === 'play_again') navigate('/games')
  }, [currentPhase, navigate, sendChatMessage, sendVote, updatePhaseData, handlePhaseComplete, isObserverMode, id])

  /**
   * 处理退出游戏
   * @description 显示退出确认弹窗
   */
  const handleExit = useCallback(() => {
    setShowExitModal(true)
  }, [])

  /**
   * 处理确认退出
   * @description 确认退出游戏，调用后端API停止后台任务，清理本地状态并跳转到游戏列表页
   */
  const handleConfirmExit = useCallback(async () => {
    console.log('[GameRoom] 开始退出游戏流程, gameId:', id)
    
    // 关闭弹窗
    setShowExitModal(false)
    
    try {
      // 调用后端API优雅退出游戏
      console.log('[GameRoom] 调用 exitGame API...')
      await exitGame(id)
      console.log('[GameRoom] exitGame API 调用成功')
    } catch (error) {
      console.error('[GameRoom] exitGame API 调用失败:', error)
      // 即使API调用失败，也继续清理本地状态
    } finally {
      // 清理 localStorage 中的游戏状态
      console.log('[GameRoom] 清理本地游戏状态')
      clearGameState()
      
      // 跳转到游戏列表页
      console.log('[GameRoom] 跳转到游戏列表页')
      navigate('/games')
    }
  }, [id, navigate])

  const handleDebugPhaseChange = useCallback((phase) => {
    if (isDebugMode) {
      forcePhaseChange(phase)
      goToPhase(phase, false)
    }
  }, [isDebugMode, forcePhaseChange, goToPhase])

  const handlePhaseSelect = useCallback((phase) => {
    if (isDebugMode) {
      handleDebugPhaseChange(phase)
    }
  }, [isDebugMode, handleDebugPhaseChange])

  // 渲染当前阶段
  const renderCurrentPhase = useCallback(() => {
    const PhaseComponent = PhaseComponents[currentPhase]

    if (!PhaseComponent) {
      return (
          <div className="h-full flex items-center justify-center text-[var(--color-secondary-500)]">
            <p>未知阶段: {currentPhase}</p>
          </div>
      )
    }

    // 基础props
    const baseProps = {
      config: currentConfig,
      gameData: gameData?.data,
      playerData: playerData?.data,
      phaseData: getPhaseData(currentPhase),
      onComplete: handlePhaseComplete,
      onSkip: handlePhaseSkip,
      onBack: handlePhaseBack,
      onAction: handleAction,
      isObserverMode,
      gameId: id,
      currentPlayerId,
    }

    // 为Investigation阶段添加搜证完成状态和WebSocket相关props
    if (currentPhase === PHASE_TYPE.INVESTIGATION) {
      return (
          <PhaseComponent
              {...baseProps}
              isAllInvestigationComplete={isAllInvestigationComplete}
              isConnected={isConnected}
              subscribe={subscribe}
              unsubscribe={unsubscribe}
          />
      )
    }

    // 为Discussion阶段添加WebSocket相关props
    if (currentPhase === PHASE_TYPE.DISCUSSION) {
      return (
          <PhaseComponent
              {...baseProps}
              isConnected={isConnected}
              subscribeToGameChat={subscribeToGameChat}
              subscribeToPersonalMessages={subscribeToPersonalMessages}
              subscribe={subscribe}
              sendChatMessage={sendChatMessage}
              sendVote={sendVote}
              unsubscribe={unsubscribe}
              currentPlayerId={currentPlayerId}
          />
      )
    }

    return <PhaseComponent {...baseProps} />
  }, [
    currentPhase,
    currentConfig,
    gameData,
    playerData,
    getPhaseData,
    handlePhaseComplete,
    handlePhaseSkip,
    handlePhaseBack,
    handleAction,
    isConnected,
    subscribeToGameChat,
    sendChatMessage,
    sendVote,
    unsubscribe,
    currentPlayerId,
    isObserverMode,
    id,
    isAllInvestigationComplete,
  ])

  // 渲染状态
  if (isLoading) {
    return (
        <Loading
            fullScreen
            text="正在加载游戏..."
            className="bg-[var(--color-primary-50)] dark:bg-[var(--color-secondary-900)] text-[var(--color-secondary-700)] dark:text-[var(--color-secondary-200)]"
        />
    )
  }

  if (error) {
    return (
        <div
            className="h-screen w-screen flex items-center justify-center bg-[var(--color-primary-50)] dark:bg-[var(--color-secondary-900)]">
          <div
              className="text-center p-8 bg-white/80 dark:bg-[var(--color-secondary-800)]/80 backdrop-blur-sm border border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] rounded-2xl shadow-lg">
            <h2 className="text-2xl font-medium text-[var(--color-secondary-800)] dark:text-[var(--color-secondary-100)] mb-2">游戏加载失败</h2>
            <p className="text-[var(--color-secondary-500)] mb-4">无法加载游戏数据，请稍后重试。</p>
            <div className="flex gap-3 justify-center">
              <button
                  onClick={() => navigate('/games')}
                  className="px-4 py-2 border border-[var(--color-secondary-300)] dark:border-[var(--color-secondary-600)] text-[var(--color-secondary-600)] dark:text-[var(--color-secondary-300)] hover:bg-[var(--color-secondary-100)] dark:hover:bg-[var(--color-secondary-700)] transition-colors rounded-lg"
              >
                返回游戏列表
              </button>
              <button
                  onClick={() => toggleDebugMode()}
                  className="px-4 py-2 bg-[var(--color-primary-100)] dark:bg-[var(--color-primary-800)]/30 text-[var(--color-primary-700)] dark:text-[var(--color-primary-300)] hover:bg-[var(--color-primary-200)] dark:hover:bg-[var(--color-primary-700)]/30 transition-colors rounded-lg"
              >
                启用模拟模式
              </button>
            </div>
          </div>
        </div>
    )
  }

  const game = gameData?.data

  return (
      <div
          className="h-screen w-screen bg-[var(--color-primary-50)] dark:bg-[var(--color-secondary-900)] flex flex-col overflow-hidden">
        {/* 背景装饰 */}
        <BackgroundDecoration/>

        {/* 顶部导航栏 */}
        <GameRoomHeader
            id={id}
            currentPhase={currentPhase}
            sequence={sequence}
            isConnected={isConnected}
            isDebugMode={isDebugMode}
            showDebugPanel={showDebugPanel}
            onToggleDebugPanel={() => setShowDebugPanel(!showDebugPanel)}
            onExit={handleExit}
            onPhaseClick={handlePhaseSelect}
        />

        {/* 阶段同步等待提示 */}
        <AnimatePresence>
          {waitingMessage && (
              <motion.div
                  initial={{opacity: 0, y: -20}}
                  animate={{opacity: 1, y: 0}}
                  exit={{opacity: 0, y: -20}}
                  className="fixed top-20 left-1/2 transform -translate-x-1/2 z-50"
              >
                <div className="bg-amber-50 dark:bg-amber-900/80 border border-amber-200 dark:border-amber-700 rounded-xl px-6 py-4 shadow-lg flex items-center gap-3">
                  <motion.div
                      animate={{rotate: 360}}
                      transition={{duration: 1.5, repeat: Infinity, ease: 'linear'}}
                      className="w-5 h-5 border-2 border-amber-500 border-t-transparent rounded-full"
                  />
                  <span className="text-amber-800 dark:text-amber-200 font-medium">
                    {waitingMessage}
                  </span>
                  <button
                      onClick={() => setWaitingMessage(null)}
                      className="ml-2 text-amber-600 hover:text-amber-800 dark:text-amber-300 dark:hover:text-amber-100 transition-colors"
                      title="关闭提示"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>
              </motion.div>
          )}
        </AnimatePresence>

        {/* 主内容区 */}
        <main className="relative z-10 flex-1 min-h-0">
          <div
              className="h-full overflow-hidden">
            {/* 内容区域 */}
            <div className="h-full p-4 sm:p-2">
              <PhaseWrapper phase={currentPhase}>
                <Suspense fallback={<PhaseLoadingPlaceholder/>}>
                  {renderCurrentPhase()}
                </Suspense>
              </PhaseWrapper>
            </div>
          </div>
        </main>

        {/* 底部状态栏 */}
        <GameRoomFooter
            currentPhase={currentPhase}
            progress={progress}
            canGoBack={canGoBack}
            onBack={handlePhaseBack}
            gameStatus={game?.status}
            sequence={sequence}
            onPhaseSelect={handlePhaseSelect}
        />

        {/* 调试面板 */}
        <AnimatePresence>
          {showDebugPanel && (
              <DebugPanel
                  isOpen={showDebugPanel}
                  onClose={() => setShowDebugPanel(false)}
                  currentPhase={currentPhase}
                  onPhaseChange={handleDebugPhaseChange}
                  isDebugMode={isDebugMode}
                  onToggleDebug={toggleDebugMode}
              />
          )}
        </AnimatePresence>

        {/* 退出确认弹窗 */}
        <AnimatePresence>
          {showExitModal && (
              <ExitConfirmModal
                  isOpen={showExitModal}
                  onClose={() => setShowExitModal(false)}
                  onConfirm={handleConfirmExit}
              />
          )}
        </AnimatePresence>
      </div>
  )
}

export default memo(GameRoom)
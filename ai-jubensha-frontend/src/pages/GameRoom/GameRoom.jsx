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

import React, {memo, Suspense, useCallback, useEffect, useMemo, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {AnimatePresence, motion} from 'framer-motion'
import {getGameById, getGamePlayers} from '../../services/api'
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

  // 调试模式
  const [showDebugPanel, setShowDebugPanel] = useState(false)

  const {
    isDebugMode,
    isLoading: debugIsLoading,
    gameData: debugGameData,
    playerData: debugPlayerData,
    isConnected: debugIsConnected,
    sendMessage: debugSendMessage,
    onMessage: debugOnMessage,
    toggleDebugMode,
    forcePhaseChange,
  } = useDebugMode({enabled: DEFAULT_DEBUG_MODE})

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
    isBackendReady,
    waitingMessage,
    isCheckingBackend,
    confirmCurrentPhase,
    setWaitingMessage,
    setIsBackendReady,
  } = usePhaseManager({
    sequence: DEFAULT_PHASE_SEQUENCE,
    onPhaseChange: (newPhase, prevPhase) => {
      console.log(`[GameRoom] 阶段切换: ${prevPhase} -> ${newPhase}`)
      // 阶段变化时保存到 localStorage
      if (id) {
        saveGameState({
          gameId: id,
          currentPhase: newPhase,
          realPlayerCount: adaptedGameData?.realPlayerCount,
          totalPlayerCount: adaptedGameData?.players?.length,
          scriptId: adaptedGameData?.scriptId,
        })
      }
    },
    onComplete: () => {
      console.log('[GameRoom] 游戏完成')
      clearGameState()
    },
  })

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
  } = useQuery({
    queryKey: ['gamePlayers', id],
    queryFn: async () => {
      const response = await getGamePlayers(id)
      return response?.data || response
    },
    enabled: !isDebugMode && !!id,
    staleTime: 60000,
  })

  useEffect(() => {
    if (isDebugMode || !id || apiGameData) return

    const savedState = loadGameState()
    if (savedState && savedState.gameId === id) {
      console.log('[GameRoom] 发现保存的游戏状态:', savedState)
      setRestoredPhase(savedState.currentPhase)
    }
    setIsStateRestored(true)
  }, [isDebugMode, id, apiGameData])

  // 应用恢复的阶段
  useEffect(() => {
    if (!restoredPhase || !isStateRestored || !apiGameData) return

    const targetPhase = adaptPhase(apiGameData.currentPhase) || restoredPhase
    if (DEFAULT_PHASE_SEQUENCE.includes(targetPhase)) {
      console.log('[GameRoom] 恢复到保存的阶段:', targetPhase)
      goToPhase(targetPhase, false)
    }

    // 清除恢复状态
    setRestoredPhase(null)
  }, [restoredPhase, isStateRestored, apiGameData, goToPhase])

  // 适配后的游戏数据
  const [adaptedGameData, setAdaptedGameData] = useState(null)
  const [isAdapting, setIsAdapting] = useState(false)

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
        setAdaptedGameData(adapted)
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

  // WebSocket - 使用新的 STOMP over SockJS 连接
  // 后端要求连接格式: /ws?gameId={gameId}
  const wsBaseUrl = WS_BASE_URL.replace('ws://', 'http://').replace('wss://', 'https://')

  const {
    isConnected: apiIsConnected,
    sendChatMessage,
    sendVote,
    subscribeToGameChat,
    subscribeToPersonalMessages,
    subscribe,
  } = useWebSocket(isDebugMode ? null : wsBaseUrl, isDebugMode ? null : id)

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

  // 事件处理
  /**
   * 处理阶段完成
   * @description 通知后端阶段确认后进入下一阶段
   */
  const handlePhaseComplete = useCallback(async () => {
    // 获取真人玩家ID（从 playerData 中查找 role 为 REAL 的玩家）
    // 注意：playerData 结构可能是 { data: [GamePlayer] } 或直接是 [GamePlayer]
    const players = playerData?.data || playerData || []
    const realPlayer = players.find(p => p.player?.role === 'REAL' || p.role === 'REAL')
    const currentPlayerId = realPlayer?.id || realPlayer?.player?.id

    if (id) {
      // 如果有真人玩家ID，发送玩家确认；否则发送观察者确认（playerId = null）
      console.log('[GameRoom] 阶段确认 - 真人玩家ID:', currentPlayerId, '是否观察者:', !currentPlayerId)
      await confirmCurrentPhase(id, currentPlayerId || null)
    }

    goToNext()
  }, [goToNext, confirmCurrentPhase, id, playerData])

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
    }

    updatePhaseData(currentPhase, {[action]: payload})

    if (action === 'return_home') navigate('/')
    if (action === 'play_again') navigate('/games')
  }, [currentPhase, navigate, sendChatMessage, sendVote, updatePhaseData])

  const handleExit = useCallback(() => {
    if (window.confirm('确定要退出游戏吗？未保存的进度将会丢失。')) {
      // 断开 WebSocket 连接
      navigate('/games')
    }
  }, [navigate])

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

    return (
        <PhaseComponent
            config={currentConfig}
            gameData={gameData?.data}
            playerData={playerData?.data}
            phaseData={getPhaseData(currentPhase)}
            onComplete={handlePhaseComplete}
            onSkip={handlePhaseSkip}
            onBack={handlePhaseBack}
            onAction={handleAction}
        />
    )
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
      </div>
  )
}

export default memo(GameRoom)
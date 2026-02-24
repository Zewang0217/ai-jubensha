/**
 * @fileoverview GameRoom 组件 - Film Noir 侦探事务所风格
 * @description 游戏房间主组件，采用复古黑色电影美学
 *
 * 设计灵感：1940年代私人侦探事务所
 * - 深色木质纹理与金属质感
 * - 档案柜、打字机、台灯元素
 */

import React, {memo, Suspense, useCallback, useEffect, useMemo, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {AnimatePresence, motion} from 'framer-motion'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'
import {useWebSocket} from '../../hooks/useWebSocket'
import {Bug, X} from 'lucide-react'

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

const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8088'
const DEFAULT_DEBUG_MODE = import.meta.env.DEV || import.meta.env.VITE_DEBUG_MODE === 'true'

const TRANSITION_CONFIG = {
  initial: {opacity: 0, y: 10},
  animate: {opacity: 1, y: 0},
  exit: {opacity: 0, y: -10},
  transition: {duration: 0.2, ease: 'easeOut'},
}

// =============================================================================
// Film Noir 风格子组件
// =============================================================================

/**
 * 阶段加载占位符 - 打字机风格
 */
const PhaseLoadingPlaceholder = memo(() => (
    <div className="h-full flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="relative">
          <div className="w-16 h-16 border-2 border-amber-700/30 rounded-lg flex items-center justify-center">
            <motion.div
                animate={{opacity: [0.3, 1, 0.3]}}
                transition={{duration: 1.5, repeat: Infinity}}
                className="text-amber-600/60 font-serif text-2xl"
            >
              T
            </motion.div>
          </div>
          <motion.div
              className="absolute -bottom-1 -right-1 w-4 h-4 bg-amber-600/20 rounded-full"
              animate={{scale: [1, 1.2, 1]}}
              transition={{duration: 2, repeat: Infinity}}
          />
        </div>
        <p className="text-stone-500 text-sm font-serif">Loading file...</p>
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
 * Film Noir 背景 - 百叶窗光影效果
 */
const NoirBackground = memo(() => {
  return (
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        {/* 基础渐变 */}
        <div className="absolute inset-0 bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950"/>
      </div>
  )
})

NoirBackground.displayName = 'NoirBackground'

/**
 * 档案抽屉式调试面板
 */
const DetectiveNotes = memo(({
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
          initial={{opacity: 0, y: 20, scale: 0.95}}
          animate={{opacity: 1, y: 0, scale: 1}}
          exit={{opacity: 0, y: 20, scale: 0.95}}
          transition={{duration: 0.2}}
          className="fixed bottom-4 right-4 z-50 w-80 bg-stone-900 border-2 border-stone-700 shadow-2xl shadow-black/50"
      >
        {/* 抽屉把手 */}
        <div className="absolute -top-3 left-1/2 -translate-x-1/2 w-16 h-3 bg-stone-700 rounded-t-sm"/>

        {/* 头部 */}
        <div className="flex items-center justify-between px-4 py-3 bg-stone-800 border-b border-stone-700">
          <div className="flex items-center gap-2">
            <Bug className="w-4 h-4 text-amber-600"/>
            <span className="text-sm font-serif text-stone-300">Detective&apos;s Notes</span>
          </div>
          <button
              onClick={onClose}
              className="p-1 hover:bg-stone-700 text-stone-500 hover:text-stone-300 transition-colors"
          >
            <X className="w-4 h-4"/>
          </button>
        </div>

        {/* 内容 */}
        <div className="p-4 space-y-4">
          {/* 调试模式开关 */}
          <div className="flex items-center justify-between p-3 bg-stone-800/50 border border-stone-700">
            <span className="text-sm text-stone-400">Simulation Mode</span>
            <button
                onClick={onToggleDebug}
                className={`relative w-12 h-6 border transition-colors ${
                    isDebugMode ? 'border-amber-600 bg-amber-900/30' : 'border-stone-600 bg-stone-800'
                }`}
            >
              <motion.div
                  initial={false}
                  animate={{x: isDebugMode ? 26 : 2}}
                  transition={{duration: 0.2}}
                  className="absolute top-1 w-3 h-3 bg-stone-400"
              />
            </button>
          </div>

          {/* 阶段切换 */}
          {isDebugMode && (
              <>
                <div className="border-t border-stone-700 pt-4">
                  <p className="text-xs text-stone-500 uppercase tracking-widest mb-3">
                    Jump to Phase
                  </p>
                  <div className="grid grid-cols-2 gap-2">
                    {DEFAULT_PHASE_SEQUENCE.map((phase) => (
                        <button
                            key={phase}
                            onClick={() => onPhaseChange(phase)}
                            className={`
                      px-3 py-2 text-xs font-serif transition-all
                      ${currentPhase === phase
                                ? 'bg-amber-900/30 text-amber-400 border border-amber-700/50'
                                : 'bg-stone-800/50 text-stone-400 border border-stone-700 hover:border-stone-600'
                            }
                    `}
                        >
                          {PHASE_CONFIG[phase]?.title}
                        </button>
                    ))}
                  </div>
                </div>

                <div className="border-t border-stone-700 pt-4">
                  <p className="text-xs text-stone-500 uppercase tracking-widest mb-2">
                    Current Phase
                  </p>
                  <div className="p-3 bg-stone-950 border border-stone-800">
                    <code className="text-xs text-amber-600/80 font-mono">{currentPhase}</code>
                  </div>
                </div>
              </>
          )}

          {!isDebugMode && (
              <p className="text-xs text-stone-600 text-center font-serif italic">
                &quot;Sometimes the best clues are hidden in plain sight...&quot;
              </p>
          )}
        </div>

        {/* 底部档案标签 */}
        <div className="px-4 py-2 bg-stone-800 border-t border-stone-700 flex justify-between items-center">
          <span className="text-[10px] text-stone-500 uppercase">Confidential</span>
          <div className="w-16 h-4 bg-stone-700/50"/>
        </div>
      </motion.div>
  )
})

DetectiveNotes.displayName = 'DetectiveNotes'

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
  } = usePhaseManager({
    sequence: DEFAULT_PHASE_SEQUENCE,
    onPhaseChange: (newPhase, prevPhase) => {
      console.log(`[GameRoom] Phase: ${prevPhase} -> ${newPhase}`)
    },
    onComplete: () => {
      console.log('[GameRoom] Case closed')
    },
  })

  // 数据获取
  const {
    data: apiGameData,
    isLoading: apiIsLoading,
    error: apiError,
  } = useQuery({
    queryKey: ['game', id],
    queryFn: () => gameApi.getGame(id),
    staleTime: 30000,
    enabled: !isDebugMode,
  })

  const {
    data: apiPlayerData,
  } = useQuery({
    queryKey: ['player', id],
    queryFn: () => gameApi.getPlayer(id),
    enabled: !isDebugMode && !!id,
    staleTime: 60000,
  })

  // 合并数据
  const gameData = useMemo(() =>
          isDebugMode ? {data: debugGameData} : apiGameData,
      [isDebugMode, debugGameData, apiGameData]
  )

  const playerData = useMemo(() =>
          isDebugMode ? {data: debugPlayerData} : apiPlayerData,
      [isDebugMode, debugPlayerData, apiPlayerData]
  )

  const isLoading = isDebugMode ? debugIsLoading : apiIsLoading
  const error = isDebugMode ? null : apiError

  // WebSocket
  const wsUrl = useMemo(() => `${WS_BASE_URL}/ws/game/${id}`, [id])

  const {
    isConnected: apiIsConnected,
    sendMessage: apiSendMessage,
    onMessage: apiOnMessage,
  } = useWebSocket(isDebugMode ? null : wsUrl)

  const isConnected = isDebugMode ? debugIsConnected : apiIsConnected
  const sendMessage = isDebugMode ? debugSendMessage : apiSendMessage
  const onMessage = isDebugMode ? debugOnMessage : apiOnMessage

  // WebSocket 监听
  useEffect(() => {
    const handleServerPhaseChange = (data) => {
      if (data?.phase && data.phase !== currentPhase) {
        console.log('[GameRoom] Server phase change:', data)
        goToPhase(data.phase)
      }
    }

    const handleGameUpdate = (data) => {
      console.log('[GameRoom] Game update:', data)
    }

    const unsubPhase = onMessage('PHASE_CHANGE', handleServerPhaseChange)
    const unsubGame = onMessage('GAME_UPDATE', handleGameUpdate)

    return () => {
      unsubPhase?.()
      unsubGame?.()
    }
  }, [onMessage, currentPhase, goToPhase])

  // 事件处理
  const handlePhaseComplete = useCallback(() => goToNext(), [goToNext])
  const handlePhaseSkip = useCallback(() => goToNext(), [goToNext])
  const handlePhaseBack = useCallback(() => goToPrevious(), [goToPrevious])

  const handleAction = useCallback((action, payload) => {
    sendMessage({
      type: 'PLAYER_ACTION',
      action,
      payload,
      timestamp: Date.now(),
    })

    updatePhaseData(currentPhase, {[action]: payload})

    if (action === 'return_home') navigate('/')
    if (action === 'play_again') navigate('/games')
  }, [currentPhase, navigate, sendMessage, updatePhaseData])

  const handleExit = useCallback(() => {
    if (window.confirm('Close the case file? Unsaved progress will be lost.')) {
      sendMessage({type: 'GAME_LEAVE', gameId: id})
      navigate('/games')
    }
  }, [id, navigate, sendMessage])

  const handleDebugPhaseChange = useCallback((phase) => {
    if (isDebugMode) {
      forcePhaseChange(phase)
      goToPhase(phase, false)
    }
  }, [isDebugMode, forcePhaseChange, goToPhase])

  // 渲染当前阶段
  const renderCurrentPhase = useCallback(() => {
    const PhaseComponent = PhaseComponents[currentPhase]

    if (!PhaseComponent) {
      return (
          <div className="h-full flex items-center justify-center text-stone-500 font-serif">
            <p>Unknown case file: {currentPhase}</p>
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
            text="Opening case file..."
            className="bg-stone-950 text-amber-100"
        />
    )
  }

  if (error) {
    return (
        <div className="h-screen w-screen flex items-center justify-center bg-stone-950">
          <div className="text-center border-2 border-stone-800 p-8 bg-stone-900/50">
            <h2 className="text-2xl font-serif text-amber-100 mb-2">Case File Missing</h2>
            <p className="text-stone-500 mb-4 font-serif">The records could not be located.</p>
            <div className="flex gap-3 justify-center">
              <button
                  onClick={() => navigate('/games')}
                  className="px-4 py-2 border border-stone-700 text-stone-400 hover:text-stone-200 hover:border-stone-500 transition-colors font-serif"
              >
                Back to Archives
              </button>
              <button
                  onClick={() => toggleDebugMode()}
                  className="px-4 py-2 border border-amber-700/50 text-amber-500 hover:bg-amber-900/20 transition-colors font-serif"
              >
                Open Simulation
              </button>
            </div>
          </div>
        </div>
    )
  }

  const game = gameData?.data

  return (
      <div className="h-screen w-screen bg-stone-950 flex flex-col overflow-hidden">
        {/* Film Noir 背景 */}
        <NoirBackground/>

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
        />

        {/* 主内容区 - 档案文件夹风格 */}
        <main className="relative z-10 flex-1 px-4 sm:px-6 pb-4 sm:pb-6 pt-8 sm:pt-10 min-h-0">
          <div
              className="h-full bg-stone-900/80 border-2 border-stone-700 shadow-2xl shadow-black/50 overflow-hidden relative">
            {/* 内容区域 */}
            <div className="h-full p-4 sm:p-6">
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
        />

        {/* 调试面板 */}
        <AnimatePresence>
          {showDebugPanel && (
              <DetectiveNotes
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

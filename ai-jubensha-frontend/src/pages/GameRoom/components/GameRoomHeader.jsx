/**
 * @fileoverview GameRoomHeader 组件 - 现代扁平化 + 科技简约风 + 二次元萌系
 * @description 游戏房间顶部导航栏
 *
 * 设计理念：现代扁平化 + 科技简约 + 二次元萌系
 * - 浅灰、淡蓝等低饱和度冷色调为主
 * - 少量亮色点缀（淡紫、淡粉）
 * - 几何图形 + 圆角方形按钮
 * - 兼顾黑白主题
 */

import React, {memo} from 'react'
import {motion} from 'framer-motion'
import {Hexagon, Settings, Sparkles, X} from 'lucide-react'
import {PHASE_CONFIG} from '../phases'
import GhostButton from './GameRoomButton'

// =============================================================================
// 设计Token - 统一美学
// =============================================================================

const DESIGN = {
  colors: {
    light: {
      bg: {
        primary: 'bg-[#F5F7FA]',
        secondary: 'bg-[#EEF1F6]',
        tertiary: 'bg-[#E4E8EE]',
        glass: 'bg-white/80',
      },
      border: {
        default: 'border-[#E0E5EE]',
        hover: 'border-[#C8D0DD]',
        accent: 'border-[#8B9DC8]',
      },
      text: {
        primary: 'text-[#2D3748]',
        secondary: 'text-[#5A6978]',
        muted: 'text-[#8C96A5]',
        accent: 'text-[#7C8CD6]',
      },
      shadow: 'shadow-[0_2px_8px_rgba(45,55,72,0.08)]',
      shadowHover: 'shadow-[0_4px_12px_rgba(45,55,72,0.12)]',
    },
    dark: {
      bg: {
        primary: 'bg-[#1A1D26]',
        secondary: 'bg-[#222631]',
        tertiary: 'bg-[#2A2F3C]',
        glass: 'bg-[#222631]/90',
      },
      border: {
        default: 'border-[#363D4D]',
        hover: 'border-[#4A5568]',
        accent: 'border-[#5E6B8A]',
      },
      text: {
        primary: 'text-[#E8ECF2]',
        secondary: 'text-[#9CA8B8]',
        muted: 'text-[#6B7788]',
        accent: 'text-[#A5B4EC]',
      },
      shadow: 'shadow-[0_2px_8px_rgba(0,0,0,0.2)]',
      shadowHover: 'shadow-[0_4px_12px_rgba(0,0,0,0.3)]',
    },
    accent: {
      primary: 'bg-[#7C8CD6]',
      secondary: 'bg-[#A78BFA]',
      tertiary: 'bg-[#F5A9C9]',
      glow: 'shadow-[0_0_12px_rgba(124,140,214,0.3)]',
    },
  },
  spacing: {
    section: 'gap-4',
    item: 'gap-2',
    padding: 'px-4 py-2.5',
    buttonPadding: 'px-3 py-2',
  },
  radius: {
    sm: 'rounded-md',
    md: 'rounded-lg',
    lg: 'rounded-xl',
  },
}

// =============================================================================
// 主题检测 Hook
// =============================================================================

const useTheme = () => {
  const [isDark, setIsDark] = React.useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return false
  })

  React.useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handler = (e) => setIsDark(e.matches)
    mediaQuery.addEventListener('change', handler)
    return () => mediaQuery.removeEventListener('change', handler)
  }, [])

  return isDark
}

// =============================================================================
// 子组件
// =============================================================================

/**
 * 品牌标识 - 几何徽章 + 萌系点缀
 */
const BrandBadge = memo(({isDark}) => {
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light

  return (
      <motion.div
          className="flex items-center gap-2"
          initial={{opacity: 0, x: -20}}
          animate={{opacity: 1, x: 0}}
          transition={{duration: 0.4, ease: [0.25, 0.1, 0.25, 1]}}
      >
        {/* 几何徽章 - 六边形 */}
        <div className="relative">
          <div
              className={`w-7 h-7 ${colors.bg.tertiary} ${DESIGN.radius.sm} flex items-center justify-center border ${colors.border.default}`}>
            <Hexagon className={`w-3.5 h-3.5 ${colors.text.accent}`} strokeWidth={1.8}/>
        </div>
          {/* 萌系小点缀 - 星星 */}
          <motion.div
              className="absolute -top-0.5 -right-0.5"
              animate={{rotate: [0, 15, -15, 0], scale: [1, 1.1, 1]}}
              transition={{duration: 2, repeat: Infinity, ease: "easeInOut"}}
          >
            <Sparkles className="w-2 h-2 text-[#F5A9C9] fill-[#F5A9C9]"/>
          </motion.div>
        </div>

        {/* 品牌文字 */}
        <div className="hidden sm:flex flex-col">
        <span className={`${colors.text.primary} font-semibold text-sm tracking-tight`}>
          剧本杀
        </span>
        </div>
      </motion.div>
  )
})
BrandBadge.displayName = 'BrandBadge'

/**
 * 阶段进度指示器 - 极简显示（不可点击）
 */
const PhaseProgress = memo(({currentPhase, sequence, isDark}) => {
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light
  const accent = DESIGN.colors.accent
  const currentIndex = sequence.indexOf(currentPhase)

  return (
      <motion.div
          className="hidden lg:flex items-center gap-3"
          initial={{opacity: 0, y: -10}}
          animate={{opacity: 1, y: 0}}
          transition={{duration: 0.4, delay: 0.1}}
      >
        {/* 当前阶段名称 */}
        <div className="flex items-center gap-1.5">
          <div className={`w-0.5 h-3 ${accent.primary} rounded-full`}/>
          <span className={`${colors.text.secondary} text-xs font-medium`}>
          {PHASE_CONFIG[currentPhase]?.title || '准备中'}
        </span>
        </div>

        {/* 分隔符 */}
        <span className={`${colors.text.muted} text-[10px]`}>|</span>

        {/* 阶段列表 - 仅显示 */}
        <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-hide">
          {sequence.map((phase, index) => {
            const config = PHASE_CONFIG[phase]
            const isActive = index === currentIndex
            const isCompleted = index < currentIndex

            return (
                <span
                    key={phase}
                    className={`
                flex-shrink-0 text-[10px] whitespace-nowrap
                transition-all duration-200
                ${isActive
                        ? `${colors.text.accent} font-medium`
                        : isCompleted
                            ? `${colors.text.muted}`
                            : `${colors.text.muted}/50`
                    }
              `}
                >
              {config?.title || phase}
                  {index < sequence.length - 1 && (
                      <span className={`${colors.text.muted}/30 ml-1.5`}>/</span>
                  )}
            </span>
            )
          })}
        </div>
      </motion.div>
  )
})
PhaseProgress.displayName = 'PhaseProgress'

/**
 * 连接状态指示 - 简约圆点
 */
const ConnectionStatus = memo(({isConnected, isDebugMode, isDark}) => {
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light

  const status = isDebugMode ? 'debug' : isConnected ? 'connected' : 'disconnected'

  const config = {
    debug: {
      color: 'bg-[#F5A9C9]',
      label: '调试',
      textColor: colors.text.accent,
    },
    connected: {
      color: 'bg-[#5DD9A8]',
      label: '已连接',
      textColor: 'text-[#5DD9A8]',
    },
    disconnected: {
      color: 'bg-[#F87171]',
      label: '未连接',
      textColor: 'text-[#F87171]',
    },
  }[status]

  return (
      <motion.div
          className="flex items-center gap-1.5 px-2 py-1.5"
          initial={{opacity: 0}}
          animate={{opacity: 1}}
          transition={{duration: 0.3, delay: 0.2}}
      >
        <div className="relative">
          <div className={`w-2 h-2 ${config.color} rounded-full`}/>
          {status !== 'disconnected' && (
              <motion.div
                  className={`absolute inset-0 ${config.color} rounded-full`}
                  animate={{scale: [1, 2], opacity: [0.5, 0]}}
                  transition={{duration: 1.5, repeat: Infinity}}
              />
          )}
        </div>
        <span className={`${config.textColor} text-[10px] font-medium`}>
        {config.label}
      </span>
      </motion.div>
  )
})
ConnectionStatus.displayName = 'ConnectionStatus'

/**
 * 控制按钮 - 统一使用 GhostButton
 */
const ControlButtons = memo(({showDebugPanel: _showDebugPanel, onToggleDebugPanel, onExit}) => {
  return (
      <motion.div
          className="flex items-center gap-1"
          initial={{opacity: 0, x: 20}}
          animate={{opacity: 1, x: 0}}
          transition={{duration: 0.4, delay: 0.25}}
      >
        {/* 调试面板 */}
        <GhostButton onClick={onToggleDebugPanel}>
        <span className="flex items-center gap-1.5">
          <Settings className="w-3.5 h-3.5" strokeWidth={1.8}/>
          <span className="hidden sm:inline">设置</span>
        </span>
        </GhostButton>

        {/* 退出按钮 */}
        <GhostButton onClick={onExit}>
        <span className="flex items-center gap-1.5">
          <X className="w-3.5 h-3.5" strokeWidth={1.8}/>
          <span className="hidden sm:inline">退出</span>
        </span>
        </GhostButton>
      </motion.div>
  )
})
ControlButtons.displayName = 'ControlButtons'

// =============================================================================
// 主组件
// =============================================================================

const GameRoomHeader = memo(({
                               id: _id,
                               currentPhase,
                               sequence,
                               isConnected,
                               isDebugMode,
                               showDebugPanel,
                               onToggleDebugPanel,
                               onExit,
                               onPhaseClick: _onPhaseClick,
                             }) => {
  const isDark = useTheme()
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light

  return (
      <header className="relative z-50 flex-none">
        {/* 背景层 */}
        <div className={`absolute inset-0 ${colors.bg.primary}`}/>

        {/* 顶部微妙渐变 */}
        <div
            className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[#7C8CD6]/20 to-transparent"/>

        {/* 底部细线 */}
        <div className={`absolute bottom-0 left-0 right-0 h-px ${colors.border.default}`}/>

        {/* 主内容区 */}
        <div className="relative px-4 sm:px-5 py-2">
          <div className="flex items-center justify-between gap-4">
            {/* 左侧：品牌 + 阶段进度 */}
            <div className="flex items-center gap-3 sm:gap-4">
              <BrandBadge isDark={isDark}/>

              {/* 分隔线 */}
              <div className="hidden xl:block w-px h-4 bg-[#E0E5EE] dark:bg-[#363D4D]"/>

              <PhaseProgress
                  currentPhase={currentPhase}
                  sequence={sequence}
                  isDark={isDark}
              />
            </div>

            {/* 右侧：连接状态 + 控制 */}
            <div className="flex items-center gap-1 sm:gap-2">
              <ConnectionStatus
                  isConnected={isConnected}
                  isDebugMode={isDebugMode}
                  isDark={isDark}
              />

              {/* 分隔线 */}
              <div className="hidden sm:block w-px h-4 bg-[#E0E5EE] dark:bg-[#363D4D]"/>

              <ControlButtons
                  showDebugPanel={showDebugPanel}
                  onToggleDebugPanel={onToggleDebugPanel}
                  onExit={onExit}
                  isDark={isDark}
              />
            </div>
          </div>
        </div>
      </header>
  )
})

GameRoomHeader.displayName = 'GameRoomHeader'

export default GameRoomHeader
/**
 * @fileoverview GameRoomFooter 组件 - 现代扁平化 + 科技简约风 + 二次元萌系
 * @description 游戏房间底部状态栏
 *
 * 设计理念：现代扁平化 + 科技简约 + 二次元萌系
 * - 浅灰、淡蓝等低饱和度冷色调为主
 * - 少量亮色点缀（淡紫、淡粉）
 * - 几何图形 + 圆角方形按钮
 * - 兼顾黑白主题
 */

import React, {memo} from 'react'
import {motion} from 'framer-motion'
import {Activity, Layers, Wifi, WifiOff} from 'lucide-react'
import {PHASE_CONFIG} from '../phases'

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
    },
    accent: {
      primary: 'bg-[#7C8CD6]',
      secondary: 'bg-[#A78BFA]',
      tertiary: 'bg-[#F5A9C9]',
      success: 'bg-[#5DD9A8]',
      warning: 'bg-[#FCD34D]',
      error: 'bg-[#F87171]',
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
 * 当前阶段指示器 - 极简风格
 */
const PhaseIndicator = memo(({currentPhase, isDark}) => {
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light
  const accent = DESIGN.colors.accent
  const config = PHASE_CONFIG[currentPhase]

  return (
      <motion.div
          className="flex items-center gap-2"
          initial={{opacity: 0, y: 10}}
          animate={{opacity: 1, y: 0}}
          transition={{duration: 0.3, delay: 0.1}}
      >
        {/* 阶段图标 */}
        <div className={`flex items-center justify-center w-5 h-5 ${colors.bg.tertiary} ${DESIGN.radius.sm}`}>
          <Layers className={`w-3 h-3 ${colors.text.accent}`} strokeWidth={1.8}/>
        </div>

        {/* 阶段名称 */}
        <span className={`${colors.text.secondary} text-sm font-medium`}>
        {config?.title}
      </span>
      </motion.div>
  )
})
PhaseIndicator.displayName = 'PhaseIndicator'

/**
 * WebSocket 连接指示器 - 简约风格
 */
const ConnectionIndicator = memo(({isConnected, isDebugMode, isDark}) => {
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light

  const status = isDebugMode ? 'debug' : isConnected ? 'connected' : 'disconnected'

  const config = {
    debug: {
      icon: Activity,
      label: '调试',
      dotColor: 'bg-[#F5A9C9]',
      textColor: 'text-[#F5A9C9]',
    },
    connected: {
      icon: Wifi,
      label: '在线',
      dotColor: 'bg-[#5DD9A8]',
      textColor: 'text-[#5DD9A8]',
    },
    disconnected: {
      icon: WifiOff,
      label: '离线',
      dotColor: 'bg-[#F87171]',
      textColor: 'text-[#F87171]',
    },
  }[status]

  const Icon = config.icon

  return (
      <motion.div
          className="flex items-center gap-1.5"
          initial={{opacity: 0, x: 10}}
          animate={{opacity: 1, x: 0}}
          transition={{duration: 0.3, delay: 0.2}}
      >
        {/* 状态图标 */}
        <Icon className={`w-3.5 h-3.5 ${config.textColor}`} strokeWidth={1.8}/>

        {/* 状态文字 */}
        <span className={`${config.textColor} text-xs font-medium`}>
        {config.label}
      </span>

        {/* 状态点 + 动画 */}
        <div className="relative">
          <div className={`w-1.5 h-1.5 rounded-full ${config.dotColor}`}/>
          {status !== 'disconnected' && (
              <motion.div
                  className={`absolute inset-0 rounded-full ${config.dotColor}`}
                  animate={{scale: [1, 2.5], opacity: [0.6, 0]}}
                  transition={{duration: 1.5, repeat: Infinity, ease: "easeOut"}}
              />
          )}
        </div>
      </motion.div>
  )
})
ConnectionIndicator.displayName = 'ConnectionIndicator'

// =============================================================================
// 主组件
// =============================================================================

const GameRoomFooter = memo(({
                               currentPhase,
                               progress,
                               canGoBack,
                               onBack,
                               gameStatus,
                               isConnected,
                               isDebugMode,
                               onPhaseSelect,
                               sequence = [],
                             }) => {
  const isDark = useTheme()
  const colors = isDark ? DESIGN.colors.dark : DESIGN.colors.light
  const accent = DESIGN.colors.accent

  return (
      <footer className="relative z-50 flex-none">
        {/* 顶部细线 */}
        <div className={`absolute top-0 left-0 right-0 h-px ${colors.border.default}`}/>

        {/* 顶部微妙渐变 */}
        <div
            className="absolute -top-2 left-0 right-0 h-2 bg-gradient-to-b from-black/5 to-transparent dark:from-black/20"/>

        {/* 背景层 */}
        <div className={`absolute inset-0 ${colors.bg.primary}`}/>

        {/* 底部微妙渐变 */}
        <div
            className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[#7C8CD6]/20 to-transparent"/>

        {/* 主内容区 */}
        <div className="relative px-4 sm:px-5 py-2">
          <div className="flex items-center justify-between gap-4">
            {/* 左侧：当前阶段 */}
            <div className="flex items-center">
              <PhaseIndicator
                  currentPhase={currentPhase}
                  isDark={isDark}
              />
            </div>

            {/* 右侧：连接状态 */}
            <motion.div
                className="flex items-center gap-2"
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{duration: 0.3, delay: 0.15}}
            >
              <ConnectionIndicator
                  isConnected={isConnected}
                  isDebugMode={isDebugMode}
                  isDark={isDark}
              />
            </motion.div>
          </div>
        </div>
      </footer>
  )
})

GameRoomFooter.displayName = 'GameRoomFooter'

export default GameRoomFooter
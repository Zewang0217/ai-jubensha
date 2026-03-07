/**
 * @fileoverview PhaseBackgroundDecor 组件 - 阶段页面公共背景装饰
 * @description 提供统一的玻璃态背景装饰效果，包含动态光晕和星星点缀
 * @author zewang
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {PHASE_COLORS} from '../../pages/GameRoom/config/theme'

/**
 * 阶段背景装饰组件
 * @description 提供统一的玻璃态背景装饰效果
 * - 左上角光晕：淡紫色渐变
 * - 右下角光晕：淡粉色渐变
 * - 星星点缀：6个动态闪烁的小圆点
 * @returns {JSX.Element} 背景装饰组件
 */
const PhaseBackgroundDecor = memo(() => (
  <>
    {/* 左上角光晕 */}
    <motion.div
      className="absolute top-0 left-0 w-64 h-64 rounded-full opacity-30 blur-3xl"
      style={{
        background: `radial-gradient(circle, ${PHASE_COLORS.primary}40 0%, transparent 70%)`,
      }}
      animate={{
        scale: [1, 1.1, 1],
        opacity: [0.3, 0.4, 0.3],
      }}
      transition={{duration: 4, repeat: Infinity}}
    />
    
    {/* 右下角光晕 */}
    <motion.div
      className="absolute bottom-0 right-0 w-80 h-80 rounded-full opacity-20 blur-3xl"
      style={{
        background: `radial-gradient(circle, ${PHASE_COLORS.secondary}40 0%, transparent 70%)`,
      }}
      animate={{
        scale: [1, 1.15, 1],
        opacity: [0.2, 0.35, 0.2],
      }}
      transition={{duration: 5, repeat: Infinity, delay: 1}}
    />
    
    {/* 星星点缀 */}
    {[...Array(6)].map((_, i) => (
      <motion.div
        key={i}
        className="absolute w-1 h-1 rounded-full"
        style={{
          backgroundColor: i % 2 === 0 ? PHASE_COLORS.primary : PHASE_COLORS.accent,
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

PhaseBackgroundDecor.displayName = 'PhaseBackgroundDecor'

export default PhaseBackgroundDecor

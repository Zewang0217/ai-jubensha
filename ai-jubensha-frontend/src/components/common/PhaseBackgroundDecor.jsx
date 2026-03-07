/**
 * @fileoverview PhaseBackgroundDecor 组件 - 阶段背景装饰
 * @description 为各个游戏阶段提供统一的背景装饰效果，包含动态光晕和粒子效果
 * @author zewang
 */

import {memo} from 'react'
import {motion} from 'framer-motion'

/**
 * PhaseBackgroundDecor 组件
 * @description 提供游戏阶段统一的背景装饰效果
 * @returns {JSX.Element} 背景装饰组件
 */
const PhaseBackgroundDecor = memo(() => (
    <div className="absolute inset-0 pointer-events-none overflow-hidden">
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
    </div>
))

PhaseBackgroundDecor.displayName = 'PhaseBackgroundDecor'

export default PhaseBackgroundDecor

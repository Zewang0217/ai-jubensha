/**
 * @fileoverview GameRoomButton 组件 - 幽灵按钮样式
 * @description 可复用的幽灵按钮组件
 *
 * 样式规范：
 * - 默认状态：无边框，只有文字
 * - Hover 状态：淡蓝色阴影勾勒出圆角长方形
 * - Hover 时鼠标指针变为点击样式
 */

import React, {memo} from 'react'
import {motion} from 'framer-motion'

// =============================================================================
// 样式常量
// =============================================================================

const HOVER_SHADOW = 'shadow-[0_0_12px_rgba(124,140,214,0.3)]'

/**
 * 幽灵按钮组件
 * @param {Object} props
 * @param {ReactNode} props.children - 按钮内容
 * @param {Function} props.onClick - 点击回调
 * @param {string} props.className - 额外的 className
 * @param {boolean} props.disabled - 是否禁用
 * @param {string} props.type - 按钮类型 'button' | 'submit' | 'reset'
 */
const GhostButton = memo(({
                              children,
                              onClick,
                              className = '',
                              disabled = false,
                              type = 'button',
                          }) => {
    return (
        <motion.button
            type={type}
            onClick={onClick}
            disabled={disabled}
            className={`
        px-3 py-1.5 text-sm font-medium
        text-[#5A6978] dark:text-[#9CA8B8]
        bg-transparent
        rounded-lg
        transition-all duration-200
        cursor-pointer
        hover:text-[#7C8CD6] dark:hover:text-[#A5B4EC]
        disabled:opacity-50 disabled:cursor-not-allowed
        ${className}
      `}
            whileHover={!disabled ? {
                scale: 1.02,
                boxShadow: HOVER_SHADOW,
            } : {}}
            whileTap={!disabled ? {scale: 0.98} : {}}
        >
            {children}
        </motion.button>
    )
})

GhostButton.displayName = 'GhostButton'

export default GhostButton

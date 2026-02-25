/**
 * @fileoverview GhostButton 组件 - 幽灵文字按钮
 * @description 未 hover 时只显示文字，hover 时显示淡蓝色背景的圆角按钮
 */

import {memo} from 'react'
import {motion} from 'framer-motion'

/**
 * 幽灵文字按钮
 * - 默认只显示文字
 * - hover 时显示淡蓝色背景
 */
function GhostButton({
                         children,
                         onClick,
                         disabled = false,
                         className = '',
                         type = 'button',
                     }) {
    return (
        <motion.button
            type={type}
            onClick={onClick}
            disabled={disabled}
            className={`
                text-[var(--color-primary-500)] 
                hover:bg-[var(--color-primary-50)] 
                dark:hover:bg-[var(--color-primary-900)]/30 
                px-4 py-2 
                rounded-lg 
                transition-colors duration-200
                cursor-pointer
                disabled:opacity-50 disabled:cursor-not-allowed
                ${className}
            `}
            whileHover={{scale: disabled ? 1 : 1.02}}
            whileTap={{scale: disabled ? 1 : 0.98}}
        >
            {children}
        </motion.button>
    )
}

export default memo(GhostButton)

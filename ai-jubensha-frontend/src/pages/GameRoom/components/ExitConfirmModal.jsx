/**
 * @fileoverview ExitConfirmModal 组件 - 退出游戏确认弹窗
 * @description 提供退出游戏前的二次确认功能，防止用户误操作
 *
 * 功能特点：
 * - 使用 framer-motion 实现平滑动画效果
 * - 支持点击外部区域关闭
 * - 支持 ESC 键关闭
 * - 遵循项目设计风格（玻璃态效果 + 低饱和度冷色调）
 */

import React, {memo, useCallback, useEffect} from 'react'
import {motion} from 'framer-motion'
import {AlertTriangle, X} from 'lucide-react'

/**
 * 退出确认弹窗组件
 *
 * @param {Object} props - 组件属性
 * @param {boolean} props.isOpen - 控制弹窗显示状态
 * @param {Function} props.onClose - 关闭弹窗的回调函数
 * @param {Function} props.onConfirm - 确认退出的回调函数
 * @returns {JSX.Element|null} 弹窗组件或 null
 */
const ExitConfirmModal = memo(({
  isOpen,
  onClose,
  onConfirm,
}) => {
  // 处理 ESC 键关闭
  useEffect(() => {
    /**
     * 键盘事件处理器
     * @param {KeyboardEvent} event - 键盘事件对象
     */
    const handleKeyDown = (event) => {
      if (event.key === 'Escape' && isOpen) {
        onClose()
      }
    }

    // 添加事件监听
    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown)
      // 防止背景滚动
      document.body.style.overflow = 'hidden'
    }

    // 清理事件监听
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'unset'
    }
  }, [isOpen, onClose])

  /**
   * 处理背景点击（点击外部区域关闭）
   * @param {React.MouseEvent} event - 鼠标事件对象
   */
  const handleBackdropClick = useCallback((event) => {
    // 只有点击背景层才关闭，点击内容区域不关闭
    if (event.target === event.currentTarget) {
      onClose()
    }
  }, [onClose])

  /**
   * 处理继续游戏按钮点击
   */
  const handleContinue = useCallback(() => {
    onClose()
  }, [onClose])

  /**
   * 处理确认退出按钮点击
   */
  const handleConfirm = useCallback(() => {
    onConfirm()
  }, [onConfirm])

  // 弹窗未打开时不渲染
  if (!isOpen) return null

  return (
    <motion.div
      initial={{opacity: 0}}
      animate={{opacity: 1}}
      exit={{opacity: 0}}
      transition={{duration: 0.2}}
      onClick={handleBackdropClick}
      className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/40 dark:bg-black/60 backdrop-blur-sm"
    >
      {/* 弹窗主体 */}
      <motion.div
        initial={{opacity: 0, scale: 0.95, y: 20}}
        animate={{opacity: 1, scale: 1, y: 0}}
        exit={{opacity: 0, scale: 0.95, y: 20}}
        transition={{duration: 0.2, ease: 'easeOut'}}
        className="relative w-full max-w-md bg-white/95 dark:bg-[var(--color-secondary-800)]/95 backdrop-blur-xl border border-[var(--color-secondary-200)] dark:border-[var(--color-secondary-700)] rounded-2xl shadow-2xl shadow-black/10"
      >
        {/* 关闭按钮 */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 hover:bg-[var(--color-secondary-100)] dark:hover:bg-[var(--color-secondary-700)] text-[var(--color-secondary-400)] dark:text-[var(--color-secondary-500)] transition-colors rounded-lg"
          aria-label="关闭"
        >
          <X className="w-5 h-5"/>
        </button>

        {/* 内容区域 */}
        <div className="p-6">
          {/* 图标和标题 */}
          <div className="flex items-start gap-4 mb-4">
            <div className="flex-shrink-0 w-12 h-12 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
              <AlertTriangle className="w-6 h-6 text-amber-600 dark:text-amber-400"/>
            </div>
            <div className="flex-1 pt-1">
              <h3 className="text-lg font-semibold text-[var(--color-secondary-800)] dark:text-[var(--color-secondary-100)]">
                退出游戏
              </h3>
            </div>
          </div>

          {/* 提示文字 */}
          <p className="text-[var(--color-secondary-600)] dark:text-[var(--color-secondary-300)] text-sm leading-relaxed mb-6 pl-16">
            确定要退出游戏吗？退出后游戏将结束，您的游戏进度将无法保存。
          </p>

          {/* 按钮组 */}
          <div className="flex gap-3 pl-16">
            {/* 继续游戏按钮 - 次要样式 */}
            <button
              onClick={handleContinue}
              className="flex-1 px-4 py-2.5 border border-[var(--color-secondary-300)] dark:border-[var(--color-secondary-600)] text-[var(--color-secondary-600)] dark:text-[var(--color-secondary-300)] hover:bg-[var(--color-secondary-100)] dark:hover:bg-[var(--color-secondary-700)] transition-all rounded-lg font-medium text-sm"
            >
              继续游戏
            </button>

            {/* 确认退出按钮 - 警告色主要样式 */}
            <button
              onClick={handleConfirm}
              className="flex-1 px-4 py-2.5 bg-red-500 hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700 text-white transition-all rounded-lg font-medium text-sm shadow-sm"
            >
              确认退出
            </button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  )
})

ExitConfirmModal.displayName = 'ExitConfirmModal'

export default ExitConfirmModal

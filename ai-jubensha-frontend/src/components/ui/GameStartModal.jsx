import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Play, Users, Bot, AlertCircle, X, Clock, Sparkles } from 'lucide-react'
import { createGameFromScript } from '../../services/api/game'

/**
 * 游戏启动弹窗组件
 * 用于选择游戏模式并创建游戏
 */
function GameStartModal({ script, isOpen, onClose, onGameCreated }) {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const [currentTip, setCurrentTip] = useState(0)

    // 游戏创建相关的趣味提示
    const loadingTips = [
        '正在初始化游戏世界...',
        '为AI玩家分配角色...',
        '准备剧本场景和线索...',
        '设置游戏初始状态...',
        '确保所有系统就绪...',
        '游戏即将开始...'
    ]

    // 定时切换提示
    useEffect(() => {
        if (loading) {
            const interval = setInterval(() => {
                setCurrentTip((prev) => (prev + 1) % loadingTips.length)
            }, 2500)
            return () => clearInterval(interval)
        }
    }, [loading, loadingTips.length])

    // 处理关闭弹窗
    const handleClose = () => {
        if (!loading) {
            setError(null)
            onClose()
        }
    }

    // 处理选择游戏模式
    const handleSelectMode = async (realPlayerCount) => {
        if (loading) return

        setLoading(true)
        setError(null)

        // 调试日志：检查 script 对象
        console.log('[GameStartModal] script object:', script)
        console.log('[GameStartModal] script.id:', script?.id)

        // 确保scriptId存在
        const scriptId = script?.id || script?.data?.id
        if (!scriptId) {
            setError('剧本ID不存在，请刷新页面重试')
            setLoading(false)
            return
        }

        try {
            const result = await createGameFromScript(scriptId, realPlayerCount)

            // API 返回后立即调用回调跳转（不等待工作流完成）
            onGameCreated(result.gameId)
        } catch (err) {
            console.error('创建游戏失败:', err)
            setError(err.message || '创建游戏失败，请重试')
            setLoading(false)
        }
    }

    // 难度映射
    const difficultyMap = {
        EASY: { text: '简单', color: 'text-emerald-600 bg-emerald-50' },
        MEDIUM: { text: '中等', color: 'text-amber-600 bg-amber-50' },
        HARD: { text: '困难', color: 'text-rose-600 bg-rose-50' },
    }

    const difficultyInfo = difficultyMap[script?.difficulty] || difficultyMap.MEDIUM

    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.2 }}
                    className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4"
                    onClick={handleClose}
                >
                    <motion.div
                        initial={{ opacity: 0, scale: 0.9, y: 20 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.9, y: 20 }}
                        transition={{ duration: 0.3, ease: [0.25, 0.46, 0.45, 0.94] }}
                        className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden border border-white/60"
                        onClick={(e) => e.stopPropagation()}
                    >
                        {/* 头部 - 剧本信息 */}
                        <div className="relative bg-gradient-to-br from-blue-500 to-blue-700 p-6 text-white">
                            {/* 关闭按钮 */}
                            <button
                                onClick={handleClose}
                                disabled={loading}
                                className="absolute top-4 right-4 w-8 h-8 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors duration-200 disabled:opacity-50"
                            >
                                <X className="w-5 h-5" />
                            </button>

                            {/* 剧本标题和信息 */}
                            <div className="pr-8">
                                <h2 className="text-2xl font-bold mb-2">{script?.name}</h2>
                                <div className="flex items-center gap-4 text-sm text-blue-100">
                                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${difficultyInfo.color.replace('text-', 'bg-white/20 text-')}`}>
                                        {difficultyInfo.text}
                                    </span>
                                    <span className="flex items-center gap-1">
                                        <Users className="w-4 h-4" />
                                        {script?.playerCount} 人
                                    </span>
                                    <span className="flex items-center gap-1">
                                        <Clock className="w-4 h-4" />
                                        {script?.duration} 分钟
                                    </span>
                                </div>
                            </div>

                            {/* 装饰元素 */}
                            <div className="absolute -bottom-6 -right-6 w-24 h-24 bg-white/10 rounded-full blur-2xl" />
                            <div className="absolute top-4 left-1/2 w-16 h-16 bg-white/10 rounded-full blur-xl" />
                        </div>

                        {/* 内容区域 */}
                        <div className="p-6">
                            {loading ? (
                                /* 加载状态 */
                                <div className="flex flex-col items-center py-8">
                                    {/* 动画加载图标 */}
                                    <motion.div
                                        animate={{ rotate: 360 }}
                                        transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
                                        className="w-16 h-16 mb-6"
                                    >
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            width="64"
                                            height="64"
                                            viewBox="0 0 24 24"
                                            fill="none"
                                            stroke="url(#gradient-loading)"
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                        >
                                            <defs>
                                                <linearGradient id="gradient-loading" x1="0%" y1="0%" x2="100%" y2="100%">
                                                    <stop offset="0%" stopColor="#3b82f6" />
                                                    <stop offset="100%" stopColor="#8b5cf6" />
                                                </linearGradient>
                                            </defs>
                                            <circle cx="12" cy="12" r="10" />
                                            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
                                        </svg>
                                    </motion.div>

                                    {/* 加载文字 */}
                                    <h3 className="text-xl font-semibold text-slate-800 mb-4">
                                        正在创建游戏...
                                    </h3>

                                    {/* 动态提示 */}
                                    <motion.div
                                        key={currentTip}
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: -10 }}
                                        transition={{ duration: 0.5 }}
                                        className="text-center text-slate-600 mb-6"
                                    >
                                        {loadingTips[currentTip]}
                                    </motion.div>

                                    {/* 提示信息 */}
                                    <p className="text-sm text-slate-400 text-center">
                                        准备就绪后将自动进入游戏房间
                                    </p>
                                </div>
                            ) : (
                                <>
                                    {/* 标题 */}
                                    <div className="text-center mb-6">
                                        <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-blue-50 mb-3">
                                            <Sparkles className="w-6 h-6 text-blue-500" />
                                        </div>
                                        <h3 className="text-lg font-semibold text-slate-800">
                                            选择游戏模式
                                        </h3>
                                        <p className="text-sm text-slate-500 mt-1">
                                            选择您想要进行的游戏方式
                                        </p>
                                    </div>

                                    {/* 错误提示 */}
                                    {error && (
                                        <motion.div
                                            initial={{ opacity: 0, y: -10 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-3"
                                        >
                                            <AlertCircle className="w-5 h-5 text-rose-500 flex-shrink-0 mt-0.5" />
                                            <div>
                                                <p className="text-sm font-medium text-rose-700">
                                                    创建失败
                                                </p>
                                                <p className="text-sm text-rose-600 mt-1">
                                                    {error}
                                                </p>
                                            </div>
                                        </motion.div>
                                    )}

                                    {/* 模式选择按钮 */}
                                    <div className="space-y-4">
                                        {/* 全AI模式 */}
                                        <motion.button
                                            whileHover={{ scale: 1.02, y: -2 }}
                                            whileTap={{ scale: 0.98 }}
                                            onClick={() => handleSelectMode(0)}
                                            className="w-full group relative p-5 rounded-xl border-2 border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 transition-all duration-300 text-left"
                                        >
                                            <div className="flex items-center gap-4">
                                                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-violet-500 to-violet-700 flex items-center justify-center shadow-lg shadow-violet-500/30 group-hover:shadow-violet-500/50 transition-shadow duration-300">
                                                    <Bot className="w-7 h-7 text-white" />
                                                </div>
                                                <div className="flex-1">
                                                    <h4 className="text-lg font-bold text-slate-800 group-hover:text-blue-700 transition-colors">
                                                        全AI玩家模式
                                                    </h4>
                                                    <p className="text-sm text-slate-500 mt-1">
                                                        所有角色由AI扮演，您可以旁观整场游戏
                                                    </p>
                                                </div>
                                                <div className="w-10 h-10 rounded-full bg-slate-100 group-hover:bg-blue-100 flex items-center justify-center transition-colors duration-300">
                                                    <Play className="w-5 h-5 text-slate-400 group-hover:text-blue-500 transition-colors duration-300" />
                                                </div>
                                            </div>
                                        </motion.button>

                                        {/* 1名真人模式 */}
                                        <motion.button
                                            whileHover={{ scale: 1.02, y: -2 }}
                                            whileTap={{ scale: 0.98 }}
                                            onClick={() => handleSelectMode(1)}
                                            className="w-full group relative p-5 rounded-xl border-2 border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 transition-all duration-300 text-left"
                                        >
                                            <div className="flex items-center gap-4">
                                                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center shadow-lg shadow-blue-500/30 group-hover:shadow-blue-500/50 transition-shadow duration-300">
                                                    <Users className="w-7 h-7 text-white" />
                                                </div>
                                                <div className="flex-1">
                                                    <h4 className="text-lg font-bold text-slate-800 group-hover:text-blue-700 transition-colors">
                                                        1名真人玩家模式
                                                    </h4>
                                                    <p className="text-sm text-slate-500 mt-1">
                                                        您扮演其中一个角色，与AI玩家一起游戏
                                                    </p>
                                                </div>
                                                <div className="w-10 h-10 rounded-full bg-slate-100 group-hover:bg-blue-100 flex items-center justify-center transition-colors duration-300">
                                                    <Play className="w-5 h-5 text-slate-400 group-hover:text-blue-500 transition-colors duration-300" />
                                                </div>
                                            </div>
                                        </motion.button>
                                    </div>

                                    {/* 底部提示 */}
                                    <div className="mt-6 text-center">
                                        <p className="text-xs text-slate-400">
                                            选择模式后将立即创建游戏并进入房间
                                        </p>
                                    </div>
                                </>
                            )}
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}

export default GameStartModal

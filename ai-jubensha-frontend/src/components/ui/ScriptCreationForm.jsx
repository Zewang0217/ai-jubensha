import React, { useState, useEffect } from 'react'
import { motion } from 'framer-motion'

/**
 * 剧本生成表单组件
 * 用于收集用户输入的剧本风格信息
 */
function ScriptCreationForm({ onSubmit, onCancel, isLoading }) {
    // 表单状态
    const [formData, setFormData] = useState({
        style: '',
        theme: '',
        difficulty: 'MEDIUM',
        playerCount: 6,
        description: ''
    })
    
    // 加载状态提示
    const [currentTip, setCurrentTip] = useState(0)
    
    // 剧本杀相关的趣味提示
    const loadingTips = [
        '正在构思精彩的剧情...',
        '为您的剧本设计独特的角色...',
        '正在编织复杂的谜团...',
        '添加一些出人意料的线索...',
        '确保每个角色都有独特的动机...',
        '设计一个令人难忘的结局...',
        '正在检查剧情的连贯性...',
        '为您的剧本添加一些惊喜元素...',
        '确保游戏体验平衡有趣...',
        '正在打磨剧本的细节...'
    ]
    
    // 定时切换提示
    useEffect(() => {
        if (isLoading) {
            const interval = setInterval(() => {
                setCurrentTip(prev => (prev + 1) % loadingTips.length)
            }, 3000)
            return () => clearInterval(interval)
        }
    }, [isLoading, loadingTips.length])

    // 表单输入变化处理
    const handleInputChange = (e) => {
        const { name, value } = e.target
        setFormData(prev => ({
            ...prev,
            [name]: value
        }))
    }

    // 表单提交处理
    const handleSubmit = (e) => {
        e.preventDefault()
        
        // 构建提示词
        const originalPrompt = `创建一个剧本杀剧本，风格：${formData.style}，主题：${formData.theme}，难度：${formData.difficulty}，玩家数量：${formData.playerCount}。${formData.description ? '补充说明：' + formData.description : ''}`
        
        onSubmit(originalPrompt)
    }

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            transition={{ duration: 0.3 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        >
            <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-8 border border-white/60">
                {/* 表单标题 */}
                <h2 className="text-2xl font-bold text-slate-800 mb-6 text-center">
                    AI 剧本生成
                </h2>

                {isLoading ? (
                    /* 加载状态 */
                    <div className="flex flex-col items-center py-8">
                        {/* 动画加载图标 */}
                        <motion.div
                            animate={{ rotate: 360 }}
                            transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                            className="w-16 h-16 mb-6"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="url(#gradient)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <defs>
                                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                                        <stop offset="0%" stopColor="#3b82f6"/>
                                        <stop offset="100%" stopColor="#8b5cf6"/>
                                    </linearGradient>
                                </defs>
                                <circle cx="12" cy="12" r="10"/>
                                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                            </svg>
                        </motion.div>

                        {/* 加载文字 */}
                        <h3 className="text-xl font-semibold text-slate-800 mb-4">
                            剧本生成中...
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
                        <div className="text-sm text-slate-500 text-center">
                            <p>剧本生成可能需要几分钟时间，请耐心等待...</p>
                            <p className="mt-2">我们正在为您创建一个精彩的剧本杀体验！</p>
                        </div>

                        {/* 取消按钮 */}
                        <button
                            type="button"
                            onClick={onCancel}
                            className="mt-8 px-6 py-3 bg-slate-200 text-slate-800 font-semibold rounded-xl hover:bg-slate-300 transition-all duration-300"
                        >
                            取消
                        </button>
                    </div>
                ) : (
                    /* 表单内容 */
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* 剧本风格 */}
                        <div>
                            <label htmlFor="style" className="block text-sm font-medium text-slate-700 mb-2">
                                剧本风格
                            </label>
                            <input
                                type="text"
                                id="style"
                                name="style"
                                value={formData.style}
                                onChange={handleInputChange}
                                placeholder="例如：悬疑、恐怖、喜剧、科幻等"
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                                required
                            />
                        </div>

                        {/* 剧本主题 */}
                        <div>
                            <label htmlFor="theme" className="block text-sm font-medium text-slate-700 mb-2">
                                剧本主题
                            </label>
                            <input
                                type="text"
                                id="theme"
                                name="theme"
                                value={formData.theme}
                                onChange={handleInputChange}
                                placeholder="例如：古老庄园谋杀案、时空穿越谜团等"
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                                required
                            />
                        </div>

                        {/* 难度等级 */}
                        <div>
                            <label htmlFor="difficulty" className="block text-sm font-medium text-slate-700 mb-2">
                                难度等级
                            </label>
                            <select
                                id="difficulty"
                                name="difficulty"
                                value={formData.difficulty}
                                onChange={handleInputChange}
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                            >
                                <option value="EASY">简单</option>
                                <option value="MEDIUM">中等</option>
                                <option value="HARD">困难</option>
                            </select>
                        </div>

                        {/* 玩家数量 */}
                        <div>
                            <label htmlFor="playerCount" className="block text-sm font-medium text-slate-700 mb-2">
                                玩家数量
                            </label>
                            <input
                                type="number"
                                id="playerCount"
                                name="playerCount"
                                value={formData.playerCount}
                                onChange={handleInputChange}
                                min="2"
                                max="10"
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                                required
                            />
                        </div>

                        {/* 补充说明 */}
                        <div>
                            <label htmlFor="description" className="block text-sm font-medium text-slate-700 mb-2">
                                补充说明（可选）
                            </label>
                            <textarea
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                placeholder="添加任何你想要的特殊要求或细节..."
                                rows={3}
                                className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300"
                            ></textarea>
                        </div>

                        {/* 按钮区域 */}
                        <div className="flex space-x-4 pt-4">
                            <button
                                type="button"
                                onClick={onCancel}
                                className="flex-1 px-6 py-3 bg-slate-200 text-slate-800 font-semibold rounded-xl hover:bg-slate-300 transition-all duration-300"
                            >
                                取消
                            </button>
                            <button
                                type="submit"
                                className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-xl hover:shadow-blue-500/40 transition-all duration-300 flex items-center justify-center"
                            >
                                生成剧本
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </motion.div>
    )
}

export default ScriptCreationForm
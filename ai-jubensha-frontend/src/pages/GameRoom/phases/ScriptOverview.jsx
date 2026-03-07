/**
 * @fileoverview ScriptOverview 组件 - 沉浸式剧本展示
 * @description 剧本概览阶段，采用电影感沉浸式设计，带有神秘氛围
 */

import {memo, useEffect, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import PhaseBackgroundDecor from '../../../components/common/PhaseBackgroundDecor'
import {PHASE_COLORS} from '../config/theme'

// =============================================================================
// 设计规范色彩（从 style.md 提取）
// =============================================================================
// 浅色主题：背景 #F5F7FA / #EEF1F6 / #E4E8EE
// 深色主题：背景 #1A1D26 / #222631 / #2A2F3C
// 强调色：主强调 #7C8CD6 / 次强调 #A78BFA / 萌系点缀 #F5A9C9
// 文字：主要 #2D3748 / 次要 #5A6978 / 弱化 #8C96A5 / 强调 #7C8CD6

// =============================================================================
// 加载动画 - 神秘漩涡效果
// =============================================================================

const LoadingSpinner = memo(({text = '加载中'}) => (
    <div className="flex flex-col items-center justify-center space-y-6">
        {/* 神秘漩涡动画 */}
        <div className="relative w-20 h-20">
            {/* 外圈 */}
            <motion.div
                className="absolute inset-0 rounded-full border-2 border-dashed"
                style={{
                    borderColor: `${PHASE_COLORS.primary}30`,
                }}
                animate={{rotate: 360}}
                transition={{duration: 3, repeat: Infinity, ease: 'linear'}}
            />
            {/* 中圈 */}
            <motion.div
                className="absolute inset-2 rounded-full border-2"
                style={{
                    borderColor: `${PHASE_COLORS.primary}50`,
                    borderStyle: 'dashed',
                }}
                animate={{rotate: -360}}
                transition={{duration: 2, repeat: Infinity, ease: 'linear'}}
            />
            {/* 内圈 */}
            <motion.div
                className="absolute inset-4 rounded-full border-2 border-dashed"
                style={{
                    borderColor: `${PHASE_COLORS.primary}80`,
                }}
                animate={{rotate: 360}}
                transition={{duration: 1.5, repeat: Infinity, ease: 'linear'}}
            />
            {/* 中心亮点 */}
            <motion.div
                className="absolute inset-0 flex items-center justify-center"
                animate={{scale: [1, 1.2, 1]}}
                transition={{duration: 1, repeat: Infinity}}
            >
                <div className="w-3 h-3 rounded-full" style={{backgroundColor: PHASE_COLORS.primary}}/>
            </motion.div>
        </div>
        {/* 加载文字 */}
        <div className="flex items-center gap-1">
            <motion.span
                className="text-[#5A6978] dark:text-[#9CA8B8] text-sm"
                animate={{opacity: [0.5, 1, 0.5]}}
                transition={{duration: 1.5, repeat: Infinity}}
            >
                正在
            </motion.span>
            <motion.span
                className="text-sm font-medium"
                style={{color: PHASE_COLORS.primary}}
                animate={{opacity: [0.5, 1, 0.5]}}
                transition={{duration: 1.5, repeat: Infinity, delay: 0.2}}
            >
                {text}
            </motion.span>
        </div>
    </div>
))

LoadingSpinner.displayName = 'LoadingSpinner'

// =============================================================================
// 信息卡片项
// =============================================================================

const InfoCard = memo(({label, value, icon, delay}) => (
    <motion.div
        initial={{opacity: 0, y: 12}}
        animate={{opacity: 1, y: 0}}
        transition={{delay, duration: 0.4}}
        className="group"
    >
        <div
            className="relative p-4 rounded-xl bg-white/50 dark:bg-[#222631]/50 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A] transition-colors duration-300">
            {/* 悬停高光 */}
            <div
                className="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[#7C8CD6]/5 to-transparent"/>

            <div className="relative flex items-center gap-3">
                {/* 图标 */}
                <div 
                    className="w-8 h-8 rounded-lg flex items-center justify-center"
                    style={{
                        backgroundColor: `${PHASE_COLORS.primary}10`,
                        color: PHASE_COLORS.primary
                    }}
                >
                    {icon}
                </div>
                <div className="flex flex-col">
                    <span className="text-[#8C96A5] dark:text-[#6B7788] text-xs uppercase tracking-wider">
                        {label}
                    </span>
                    <span className="text-[#2D3748] dark:text-[#E8ECF2] font-medium">
                        {value}
                    </span>
                </div>
            </div>
        </div>
    </motion.div>
))

InfoCard.displayName = 'InfoCard'

// =============================================================================
// 主组件
// =============================================================================

function ScriptOverview({config: _config, gameData, onComplete, onAction}) {
    const [isGenerating, setIsGenerating] = useState(true)

    // 模拟生成进度
    useEffect(() => {
        if (!isGenerating) return

        const timer = setTimeout(() => {
            setIsGenerating(false)
        }, 2000)

        return () => clearTimeout(timer)
    }, [isGenerating])

    const script = gameData?.script || {
        name: '迷雾山庄',
        description: '一个暴风雨夜，六位陌生人齐聚一堂。当灯光熄灭，死亡降临，每个人都成为嫌疑人。门外的迷雾如同每个人心中的秘密，模糊不清...',
        playerCount: 6,
        duration: 120,
        difficulty: '中等',
        genre: '悬疑推理',
    }

    const handleContinue = () => {
        onAction?.('script_overview_complete', {scriptId: script.id})
        onComplete?.()
    }

    return (
        <div className="h-full relative overflow-hidden">
            {/* 背景装饰 */}
            <div className="absolute inset-0 pointer-events-none">
                <PhaseBackgroundDecor/>
            </div>

            <div className="h-full flex items-center justify-center p-6 relative z-10">
                <AnimatePresence mode="wait">
                    {isGenerating ? (
                        <motion.div
                            key="loading"
                            initial={{opacity: 0}}
                            animate={{opacity: 1}}
                            exit={{opacity: 0, scale: 0.95}}
                            transition={{duration: 0.3}}
                        >
                            <LoadingSpinner text="正在加载剧本..."/>
                        </motion.div>
                    ) : (
                        <motion.div
                            key="content"
                            initial={{opacity: 0, y: 30}}
                            animate={{opacity: 1, y: 0}}
                            transition={{duration: 0.5, ease: 'easeOut'}}
                            className="w-full max-w-5xl"
                        >
                            {/* 主卡片 - 玻璃态效果 */}
                            <div className="relative">
                                {/* 卡片光晕 */}
                                <div
                                    className="absolute -inset-1 rounded-3xl bg-gradient-to-r from-[#7C8CD6]/20 via-[#A78BFA]/20 to-[#F5A9C9]/20 blur-xl opacity-50"/>

                                {/* 卡片本体 */}
                                <div
                                    className="relative bg-white/80 dark:bg-[#222631]/90 backdrop-blur-xl rounded-2xl shadow-2xl shadow-black/5 overflow-hidden border border-[#E0E5EE] dark:border-[#363D4D]">
                                    {/* 顶部装饰线 */}
                                    <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

                                    <div
                                        className="grid lg:grid-cols-5 divide-y lg:divide-y-0 lg:divide-x divide-[#E4E8EE] dark:divide-[#2A2F3C]">

                                        {/* 左侧 - 剧本信息（占3列） */}
                                        <div className="lg:col-span-3 p-8 lg:p-10 flex flex-col">
                                            {/* 标签 */}
                                            <motion.div
                                                initial={{opacity: 0, x: -20}}
                                                animate={{opacity: 1, x: 0}}
                                                transition={{delay: 0.1}}
                                                className="inline-flex items-center gap-2 mb-4"
                                            >
                                                <span
                                                    className="px-3 py-1 rounded-full text-xs font-medium bg-[#7C8CD6]/10 text-[#7C8CD6] dark:bg-[#7C8CD6]/20 dark:text-[#A5B4EC]">
                                                    剧本预览
                                                </span>
                                                <span className="text-[#8C96A5] dark:text-[#6B7788] text-xs">
                                                    SCRIPT PREVIEW
                                                </span>
                                            </motion.div>

                                            {/* 标题 */}
                                            <motion.h1
                                                initial={{opacity: 0, y: 16}}
                                                animate={{opacity: 1, y: 0}}
                                                transition={{delay: 0.15}}
                                                className="text-3xl lg:text-4xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight"
                                            >
                                                {script.name}
                                            </motion.h1>

                                            {/* 装饰线 */}
                                            <motion.div
                                                initial={{scaleX: 0}}
                                                animate={{scaleX: 1}}
                                                transition={{delay: 0.25, duration: 0.6}}
                                                className="w-20 h-1 bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] mt-4 mb-6 rounded-full"
                                            />

                                            {/* 描述 */}
                                            <motion.p
                                                initial={{opacity: 0}}
                                                animate={{opacity: 1}}
                                                transition={{delay: 0.2}}
                                                className="text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed text-lg flex-1"
                                            >
                                                {script.description}
                                            </motion.p>

                                            {/* 标签组 */}
                                            <motion.div
                                                initial={{opacity: 0, y: 12}}
                                                animate={{opacity: 1, y: 0}}
                                                transition={{delay: 0.35}}
                                                className="flex flex-wrap gap-2 mt-6"
                                            >
                                                <span
                                                    className="px-3 py-1.5 rounded-lg text-sm bg-[#F5A9C9]/10 text-[#E879A9] dark:bg-[#F5A9C9]/20">
                                                    {script.genre}
                                                </span>
                                                <span
                                                    className="px-3 py-1.5 rounded-lg text-sm bg-[#7C8CD6]/10 text-[#5E6BCE] dark:bg-[#7C8CD6]/20">
                                                    {script.difficulty}
                                                </span>
                                            </motion.div>
                                        </div>

                                        {/* 右侧 - 信息卡片（占2列） */}
                                        <div
                                            className="lg:col-span-2 p-8 lg:p-10 bg-gradient-to-br from-white/50 to-[#7C8CD6]/5 dark:from-[#222631]/50 dark:to-[#7C8CD6]/5 flex flex-col">
                                            <motion.h2
                                                initial={{opacity: 0}}
                                                animate={{opacity: 1}}
                                                transition={{delay: 0.25}}
                                                className="text-sm font-medium text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider mb-6"
                                            >
                                                剧本信息
                                            </motion.h2>

                                            <div className="grid grid-cols-2 gap-4">
                                                <InfoCard
                                                    label="玩家人数"
                                                    value={`${script.playerCount} 人`}
                                                    delay={0.3}
                                                    icon={
                                                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24"
                                                             stroke="currentColor">
                                                            <path strokeLinecap="round" strokeLinejoin="round"
                                                                  strokeWidth={2}
                                                                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                                                        </svg>
                                                    }
                                                />
                                                <InfoCard
                                                    label="游戏时长"
                                                    value={`${script.duration} 分钟`}
                                                    delay={0.35}
                                                    icon={
                                                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24"
                                                             stroke="currentColor">
                                                            <path strokeLinecap="round" strokeLinejoin="round"
                                                                  strokeWidth={2}
                                                                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                                        </svg>
                                                    }
                                                />
                                                <InfoCard
                                                    label="难度等级"
                                                    value={script.difficulty}
                                                    delay={0.4}
                                                    icon={
                                                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24"
                                                             stroke="currentColor">
                                                            <path strokeLinecap="round" strokeLinejoin="round"
                                                                  strokeWidth={2}
                                                                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                                                        </svg>
                                                    }
                                                />
                                                <InfoCard
                                                    label="剧本类型"
                                                    value={script.genre}
                                                    delay={0.45}
                                                    icon={
                                                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24"
                                                             stroke="currentColor">
                                                            <path strokeLinecap="round" strokeLinejoin="round"
                                                                  strokeWidth={2}
                                                                  d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
                                                        </svg>
                                                    }
                                                />
                                            </div>

                                            {/* 底部区域 - 开始按钮 */}
                                            <div className="mt-auto pt-6">
                                                <div className="flex items-center justify-between">
                                                    <motion.div
                                                        initial={{opacity: 0}}
                                                        animate={{opacity: 1}}
                                                        transition={{delay: 0.5}}
                                                        className="flex items-center gap-2 text-[#8C96A5] dark:text-[#6B7788] text-xs"
                                                    >
                                                        <span
                                                            className="w-2 h-2 rounded-full bg-[#5DD9A8] animate-pulse"/>
                                                        <span>准备就绪</span>
                                                    </motion.div>

                                                    <motion.div
                                                        initial={{opacity: 0, scale: 0.9}}
                                                        animate={{opacity: 1, scale: 1}}
                                                        transition={{delay: 0.45}}
                                                    >
                                                        <GhostButton
                                                            onClick={handleContinue}
                                                            className="group relative px-6 py-2.5 text-sm"
                                                        >
                                                            <span className="flex items-center gap-2">
                                                                <span>开始游戏</span>
                                                                <motion.span
                                                                    animate={{x: [0, 4, 0]}}
                                                                    transition={{duration: 1.5, repeat: Infinity}}
                                                                >
                                                                    →
                                                                </motion.span>
                                                            </span>
                                                        </GhostButton>
                                                    </motion.div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>
        </div>
    )
}

ScriptOverview.displayName = 'ScriptOverview'
ScriptOverview.phaseType = PHASE_TYPE.SCRIPT_OVERVIEW

export default memo(ScriptOverview)

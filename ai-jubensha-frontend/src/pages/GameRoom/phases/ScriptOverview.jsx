/**
 * @fileoverview ScriptOverview 组件 - Film Noir 风格
 * @description 剧本概览阶段，采用复古黑色电影美学
 */

import {memo, useEffect, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 复古打字机效果组件
// =============================================================================

const TypewriterText = memo(({text, _delay = 0, onComplete}) => {
    const [displayText, setDisplayText] = useState('')
    const [currentIndex, setCurrentIndex] = useState(0)

    useEffect(() => {
        if (currentIndex < text.length) {
            const timeout = setTimeout(() => {
                setDisplayText(prev => prev + text[currentIndex])
                setCurrentIndex(prev => prev + 1)
            }, 30 + Math.random() * 20)
            return () => clearTimeout(timeout)
        } else {
            onComplete?.()
        }
    }, [currentIndex, text, onComplete])

    return (
        <span className="font-serif">
      {displayText}
            <span className="animate-pulse">|</span>
    </span>
    )
})

TypewriterText.displayName = 'TypewriterText'

// =============================================================================
// 电影胶片边框组件
// =============================================================================

const FilmStrip = memo(({children}) => (
    <div className="relative">
        {/* 顶部胶片孔 */}
        <div className="absolute -top-3 left-0 right-0 flex justify-between px-2">
            {Array.from({length: 12}).map((_, i) => (
                <div key={i} className="w-2 h-3 bg-stone-800 rounded-sm"/>
            ))}
        </div>
        {/* 内容 */}
        <div className="border-4 border-stone-800 bg-stone-900/90">
            {children}
        </div>
        {/* 底部胶片孔 */}
        <div className="absolute -bottom-3 left-0 right-0 flex justify-between px-2">
            {Array.from({length: 12}).map((_, i) => (
                <div key={i} className="w-2 h-3 bg-stone-800 rounded-sm"/>
            ))}
        </div>
    </div>
))

FilmStrip.displayName = 'FilmStrip'

// =============================================================================
// 复古加载动画 - 电影放映机效果
// =============================================================================

const ProjectorLoader = memo(() => (
    <div className="flex flex-col items-center justify-center space-y-8">
        {/* 放映机光圈 */}
        <div className="relative w-32 h-32">
            <motion.div
                className="absolute inset-0 rounded-full border-4 border-amber-600/30"
                animate={{rotate: 360}}
                transition={{duration: 3, repeat: Infinity, ease: "linear"}}
            >
                {Array.from({length: 8}).map((_, i) => (
                    <div
                        key={i}
                        className="absolute w-2 h-6 bg-amber-600/50 rounded-full"
                        style={{
                            top: '50%',
                            left: '50%',
                            transform: `rotate(${i * 45}deg) translateY(-48px)`,
                            transformOrigin: 'center',
                        }}
                    />
                ))}
            </motion.div>
            <div
                className="absolute inset-4 rounded-full bg-gradient-to-br from-amber-500/20 to-amber-900/40 flex items-center justify-center">
                <span className="text-amber-400 font-serif text-2xl"> noir </span>
            </div>
        </div>

        {/* 加载文字 */}
        <div className="text-center space-y-2">
            <p className="text-amber-400/80 font-serif text-lg tracking-widest uppercase">
                Developing Story
            </p>
            <div className="flex justify-center gap-1">
                {Array.from({length: 5}).map((_, i) => (
                    <motion.div
                        key={i}
                        className="w-2 h-2 bg-amber-600 rounded-full"
                        animate={{opacity: [0.3, 1, 0.3]}}
                        transition={{duration: 1.5, repeat: Infinity, delay: i * 0.2}}
                    />
                ))}
            </div>
        </div>
    </div>
))

ProjectorLoader.displayName = 'ProjectorLoader'

// =============================================================================
// 主要组件
// =============================================================================

function ScriptOverview({_config, gameData, onComplete, onAction}) {
    const [isGenerating, setIsGenerating] = useState(true)
    const [_progress, setProgress] = useState(0)
    const [showContent, setShowContent] = useState(false)
    const [titleTyped, setTitleTyped] = useState(false)

    // 模拟生成进度
    useEffect(() => {
        if (!isGenerating) return

        const timer = setInterval(() => {
            setProgress(prev => {
                if (prev >= 100) {
                    clearInterval(timer)
                    setTimeout(() => {
                        setIsGenerating(false)
                        setTimeout(() => setShowContent(true), 300)
                    }, 500)
                    return 100
                }
                return prev + Math.random() * 8 + 2
            })
        }, 150)

        return () => clearInterval(timer)
    }, [isGenerating])

    const script = gameData?.script || {
        name: 'The Misty Manor Mystery',
        description: 'A stormy night. Six strangers. One murder. The fog outside mirrors the secrets within each guest. When the lights go out and death arrives, everyone becomes a suspect.',
        playerCount: 6,
        duration: 120,
        difficulty: 'Medium',
        genre: 'Murder Mystery',
        year: '1947',
    }

    const handleContinue = () => {
        onAction?.('script_overview_complete', {scriptId: script.id})
        onComplete?.()
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">
            {/* 顶部装饰条 */}
            <div className="h-1 bg-gradient-to-r from-transparent via-amber-700/50 to-transparent"/>

            <AnimatePresence mode="wait">
                {isGenerating ? (
                    <motion.div
                        key="loading"
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        exit={{opacity: 0}}
                        className="flex-1 flex items-center justify-center"
                    >
                        <ProjectorLoader/>
                    </motion.div>
                ) : (
                    <motion.div
                        key="content"
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        transition={{duration: 0.8}}
                        className="flex-1 flex flex-col p-6 overflow-auto"
                    >
                        {/* 档案标签 */}
                        <div className="flex justify-between items-center mb-6">
                            <div className="flex items-center gap-3">
                                <div className="w-3 h-3 bg-red-700 rounded-full animate-pulse"/>
                                <span className="text-stone-500 font-mono text-xs tracking-widest uppercase">
                  Case File #{script.id || '001'}
                </span>
                            </div>
                            <span className="text-amber-600/60 font-serif italic text-sm">
                {script.year || '1947'}
              </span>
                        </div>

                        {/* 主要内容 - 电影胶片边框 */}
                        <FilmStrip>
                            <div className="p-8 space-y-6">
                                {/* 标题 */}
                                <div className="text-center space-y-4">
                                    <motion.h1
                                        initial={{opacity: 0, y: 20}}
                                        animate={{opacity: 1, y: 0}}
                                        transition={{delay: 0.2}}
                                        className="text-4xl md:text-5xl font-black text-amber-100 tracking-tight"
                                        style={{fontFamily: 'serif'}}
                                    >
                                        {showContent && (
                                            <TypewriterText
                                                text={script.name}
                                                onComplete={() => setTitleTyped(true)}
                                            />
                                        )}
                                    </motion.h1>

                                    <motion.div
                                        initial={{scaleX: 0}}
                                        animate={{scaleX: 1}}
                                        transition={{delay: 0.5, duration: 0.8}}
                                        className="h-px bg-gradient-to-r from-transparent via-amber-700/50 to-transparent"
                                    />
                                </div>

                                {/* 描述 */}
                                <motion.p
                                    initial={{opacity: 0}}
                                    animate={{opacity: titleTyped ? 1 : 0}}
                                    transition={{duration: 0.8}}
                                    className="text-stone-300 font-serif text-lg leading-relaxed text-center max-w-2xl mx-auto"
                                >
                                    {script.description}
                                </motion.p>

                                {/* 信息网格 - 侦探档案风格 */}
                                <motion.div
                                    initial={{opacity: 0, y: 20}}
                                    animate={{opacity: titleTyped ? 1 : 0, y: titleTyped ? 0 : 20}}
                                    transition={{delay: 0.3}}
                                    className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8"
                                >
                                    {[
                                        {label: 'Players', value: `${script.playerCount}`, icon: '●'},
                                        {label: 'Duration', value: `${script.duration}m`, icon: '◆'},
                                        {label: 'Difficulty', value: script.difficulty, icon: '▲'},
                                        {label: 'Genre', value: script.genre, icon: '■'},
                                    ].map((item, _i) => (
                                        <div
                                            key={item.label}
                                            className="border border-stone-700/50 bg-stone-800/30 p-4 text-center"
                                        >
                                            <span className="text-amber-600/60 text-xs">{item.icon}</span>
                                            <p className="text-amber-100 font-serif text-xl mt-1">{item.value}</p>
                                            <p className="text-stone-500 text-xs uppercase tracking-wider mt-1">{item.label}</p>
                                        </div>
                                    ))}
                                </motion.div>
                            </div>
                        </FilmStrip>

                        {/* 底部操作 */}
                        <motion.div
                            initial={{opacity: 0}}
                            animate={{opacity: titleTyped ? 1 : 0}}
                            transition={{delay: 0.5}}
                            className="flex justify-center mt-8"
                        >
                            <button
                                onClick={handleContinue}
                                className="group relative px-8 py-4 bg-gradient-to-r from-amber-700 to-amber-600 text-stone-100 font-serif text-lg tracking-wider uppercase overflow-hidden transition-all hover:from-amber-600 hover:to-amber-500"
                            >
                                <span className="relative z-10">Enter The Mystery</span>
                                <motion.div
                                    className="absolute inset-0 bg-white/10"
                                    initial={{x: '-100%'}}
                                    whileHover={{x: '100%'}}
                                    transition={{duration: 0.5}}
                                />
                            </button>
                        </motion.div>

                        {/* 装饰性指纹 */}
                        <div className="absolute bottom-4 right-4 opacity-10">
                            <svg width="120" height="120" viewBox="0 0 100 100" className="text-stone-400">
                                <path
                                    fill="currentColor"
                                    d="M50 5 C30 5 15 20 15 40 C15 55 25 65 30 70 C35 75 35 85 35 95 L40 95 C40 85 40 75 35 68 C30 61 25 52 25 40 C25 25 35 15 50 15 C65 15 75 25 75 40 C75 52 70 61 65 68 C60 75 60 85 60 95 L65 95 C65 85 65 75 70 70 C75 65 85 55 85 40 C85 20 70 5 50 5 Z"
                                />
                            </svg>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* 噪点纹理叠加 */}
            <div
                className="absolute inset-0 pointer-events-none opacity-[0.03]"
                style={{
                    backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E")`,
                }}
            />
        </div>
    )
}

ScriptOverview.displayName = 'ScriptOverview'
ScriptOverview.phaseType = PHASE_TYPE.SCRIPT_OVERVIEW

export default memo(ScriptOverview)
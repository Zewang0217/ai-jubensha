/**
 * @fileoverview ScriptReading 组件 - Film Noir 风格
 * @description 剧本阅读阶段，采用复古黑色电影美学
 */

import {memo, useRef, useState} from 'react'
import {motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 复古分页器组件
// =============================================================================

const PageIndicator = memo(({current, total, onSelect}) => (
    <div className="flex items-center gap-1">
        {Array.from({length: total}).map((_, i) => (
            <button
                key={i}
                onClick={() => onSelect(i)}
                className={`
          w-8 h-1 transition-all duration-300
          ${i === current
                    ? 'bg-amber-600 w-12'
                    : 'bg-stone-700 hover:bg-stone-600'
                }
        `}
            />
        ))}
    </div>
))

PageIndicator.displayName = 'PageIndicator'

// =============================================================================
// 章节卡片 - 档案风格
// =============================================================================

const ChapterTab = memo(({chapter, isActive, onClick, index}) => (
    <button
        onClick={onClick}
        className={`
      w-full text-left p-4 border-l-2 transition-all duration-300
      ${isActive
            ? 'border-amber-600 bg-amber-950/20'
            : 'border-stone-700 hover:border-stone-600 hover:bg-stone-800/20'
        }
    `}
    >
        <div className="flex items-center gap-3">
      <span className={`
        font-mono text-lg
        ${isActive ? 'text-amber-500' : 'text-stone-600'}
      `}>
        {String(index + 1).padStart(2, '0')}
      </span>
            <div className="flex-1 min-w-0">
                <h4 className={`font-serif truncate ${isActive ? 'text-amber-100' : 'text-stone-400'}`}>
                    {chapter.title}
                </h4>
                <p className="text-xs text-stone-600 mt-0.5">
                    {chapter.time}
                </p>
            </div>
        </div>
    </button>
))

ChapterTab.displayName = 'ChapterTab'

// =============================================================================
// 主要组件
// =============================================================================

function ScriptReading({config, gameData, _playerData, onComplete, onSkip, onAction}) {
    const [currentChapter, setCurrentChapter] = useState(0)
    const [isFlipping, setIsFlipping] = useState(false)
    const contentRef = useRef(null)

    const chapters = gameData?.scriptChapters || [
        {
            id: 'ch-001',
            title: 'The Storm',
            time: 'Act I',
            location: 'Manor Hall',
            content: `
        <p class="mb-4">The rain lashed against the windows like angry fists. Inside the manor, six strangers gathered around the fireplace, each carrying secrets heavier than the storm outside.</p>
        <p class="mb-4">Mr. Qin, the master of the house, stood before them with a glass of whiskey in hand. His smile didn't reach his eyes.</p>
        <p class="mb-4">"Tonight, I shall reveal the truth," he announced. "About the inheritance. About the betrayal. About—"</p>
        <p>The lights went out. A scream. Then silence.</p>
      `,
        },
        {
            id: 'ch-002',
            title: 'Who Are You',
            time: 'Background',
            location: 'Various',
            content: `
        <p class="mb-4">You are not here by accident. Every guest was invited for a reason. The victim knew something about each of you—secrets worth killing for.</p>
        <p class="mb-4">Your task is twofold: find the murderer, and keep your own secrets buried. Trust no one. Suspect everyone.</p>
        <p>Remember: in this game, the hunter can become the hunted in the blink of an eye.</p>
      `,
        },
        {
            id: 'ch-003',
            title: 'Evidence Emerges',
            time: 'Crime Scene',
            location: 'The Study',
            content: `
        <p class="mb-4">The study door was locked from the inside. The windows were sealed. Yet Mr. Qin lay dead, an antique letter opener buried in his chest.</p>
        <p class="mb-4">A locked room mystery. The classic scenario. But who had the key? Who had the motive? Who had the opportunity?</p>
        <p>The storm rages on. The killer walks among you.</p>
      `,
        },
        {
            id: 'ch-004',
            title: 'Mission Brief',
            time: 'Instructions',
            location: 'Confidential',
            content: `
        <p class="mb-4 font-bold text-amber-600/80">PRIMARY OBJECTIVE:</p>
        <ul class="list-disc list-inside mb-4 space-y-1 text-stone-300">
          <li>Gather evidence and reconstruct the timeline</li>
          <li>Interrogate other guests and analyze their statements</li>
          <li>Identify the murderer before they strike again</li>
        </ul>
        <p class="mb-4 font-bold text-red-500/80">SECONDARY OBJECTIVE:</p>
        <ul class="list-disc list-inside mb-4 space-y-1 text-stone-300">
          <li>Protect your own secrets</li>
          <li>Determine who can be trusted</li>
          <li>Survive the night</li>
        </ul>
        <p class="text-amber-500/60 italic">"The truth is rarely pure and never simple." — Oscar Wilde</p>
      `,
        },
    ]

    const currentData = chapters[currentChapter]
    const isFirstChapter = currentChapter === 0
    const isLastChapter = currentChapter === chapters.length - 1

    const handleChapterChange = (newIndex) => {
        if (newIndex === currentChapter) return

        setIsFlipping(true)
        setTimeout(() => {
            setCurrentChapter(newIndex)
            setIsFlipping(false)
            contentRef.current?.scrollTo(0, 0)
        }, 300)
    }

    const handleNext = () => {
        if (!isLastChapter) {
            handleChapterChange(currentChapter + 1)
        } else {
            onAction?.('script_reading_complete', {chaptersRead: chapters.length})
            onComplete?.()
        }
    }

    const handlePrevious = () => {
        if (!isFirstChapter) {
            handleChapterChange(currentChapter - 1)
        }
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">
            {/* 顶部信息栏 */}
            <div className="flex items-center justify-between mb-4 px-2 pb-4 border-b border-stone-800">
                <div>
                    <h2 className="text-xl font-serif text-amber-100">
                        Case File: Script
                    </h2>
                    <p className="text-stone-500 text-xs mt-1 font-mono">
                        PAGE {currentChapter + 1} OF {chapters.length}
                    </p>
                </div>

                {config.allowSkip && (
                    <button
                        onClick={onSkip}
                        className="text-stone-600 hover:text-stone-400 text-sm transition-colors"
                    >
                        [ Skip ]
                    </button>
                )}
            </div>

            {/* 主内容区 */}
            <div className="flex-1 flex gap-6 min-h-0">
                {/* 章节导航 */}
                <nav className="w-48 flex-none hidden md:block overflow-y-auto border-r border-stone-800 pr-4">
                    <p className="text-stone-600 text-xs uppercase tracking-widest mb-3 px-4">
                        Contents
                    </p>
                    <div className="space-y-1">
                        {chapters.map((chapter, index) => (
                            <ChapterTab
                                key={chapter.id}
                                chapter={chapter}
                                isActive={index === currentChapter}
                                onClick={() => handleChapterChange(index)}
                                index={index}
                            />
                        ))}
                    </div>
                </nav>

                {/* 阅读区域 */}
                <div className="flex-1 min-w-0 flex flex-col">
                    {/* 章节标题 */}
                    <motion.div
                        className="mb-4 p-4 bg-stone-800/30 border border-stone-700/50"
                        initial={false}
                        animate={{opacity: isFlipping ? 0 : 1}}
                        transition={{duration: 0.3}}
                    >
                        <div className="flex items-center gap-2 text-stone-600 text-xs uppercase tracking-widest mb-2">
                            <span>{currentData.time}</span>
                            <span>•</span>
                            <span>{currentData.location}</span>
                        </div>
                        <h3 className="text-2xl font-serif text-amber-100">
                            {currentData.title}
                        </h3>
                    </motion.div>

                    {/* 内容 */}
                    <div
                        ref={contentRef}
                        className="flex-1 overflow-y-auto pr-2"
                    >
                        <motion.div
                            initial={false}
                            animate={{
                                opacity: isFlipping ? 0 : 1,
                                rotateY: isFlipping ? -10 : 0
                            }}
                            transition={{duration: 0.3}}
                            className="p-6 bg-stone-900/30 border border-stone-800/50 min-h-full"
                        >
                            <div
                                className="prose prose-invert prose-stone max-w-none font-serif text-stone-300 leading-relaxed"
                                dangerouslySetInnerHTML={{__html: currentData.content}}
                            />
                        </motion.div>
                    </div>

                    {/* 导航控制 */}
                    <div className="flex items-center justify-between mt-4 pt-4 border-t border-stone-800">
                        <button
                            onClick={handlePrevious}
                            disabled={isFirstChapter}
                            className={`
                px-4 py-2 font-serif transition-colors
                ${isFirstChapter
                                ? 'text-stone-700 cursor-not-allowed'
                                : 'text-stone-400 hover:text-amber-400'
                            }
              `}
                        >
                            ← Previous
                        </button>

                        <PageIndicator
                            current={currentChapter}
                            total={chapters.length}
                            onSelect={handleChapterChange}
                        />

                        <button
                            onClick={handleNext}
                            className={`
                px-4 py-2 font-serif transition-colors
                ${isLastChapter
                                ? 'text-amber-400 hover:text-amber-300'
                                : 'text-stone-400 hover:text-amber-400'
                            }
              `}
                        >
                            {isLastChapter ? 'Begin Investigation →' : 'Next →'}
                        </button>
                    </div>
                </div>
            </div>

            {/* 页码装饰 */}
            <div className="absolute bottom-4 right-4 text-stone-700 font-mono text-sm">
                - {String(currentChapter + 1).padStart(2, '0')} -
            </div>
        </div>
    )
}

ScriptReading.displayName = 'ScriptReading'
ScriptReading.phaseType = PHASE_TYPE.SCRIPT_READING

export default memo(ScriptReading)
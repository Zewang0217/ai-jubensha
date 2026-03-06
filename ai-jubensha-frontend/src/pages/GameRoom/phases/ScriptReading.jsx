/**
 * @fileoverview ScriptReading 组件 - 透明背景 + 玻璃态卡片
 * @description 剧本阅读阶段，展示角色背景故事、秘密和时间线
 */

import {memo, useMemo, useRef, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {ChevronLeft, ChevronRight, Clock, Eye, Scroll} from 'lucide-react'
import {useQuery} from '@tanstack/react-query'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import {getCharacterById} from '../../../services/api/character'

// =============================================================================
// 动画配置
// =============================================================================

const containerVariants = {
  hidden: {opacity: 0},
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.1,
    },
  },
}

const itemVariants = {
  hidden: {opacity: 0, y: 12},
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.4,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

// =============================================================================
// 背景装饰
// =============================================================================

const BackgroundDecor = memo(() => (
    <>
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
    </>
))
BackgroundDecor.displayName = 'BackgroundDecor'

// =============================================================================
// 章节导航项
// =============================================================================

const ChapterTab = memo(({chapter, isActive, onClick, index}) => (
    <motion.button
        onClick={onClick}
        whileHover={{x: 2}}
        whileTap={{scale: 0.98}}
        className={`
      w-full text-left p-2.5 rounded-lg transition-all duration-300 overflow-hidden
      ${isActive
            ? 'bg-white/80 dark:bg-[#222631]/80 backdrop-blur-md border border-[#7C8CD6]/50 shadow-md'
            : 'bg-white/40 dark:bg-[#222631]/40 backdrop-blur-sm border border-transparent hover:bg-white/60 dark:hover:bg-[#222631]/60'
        }
    `}
    >
      <div className="flex items-center gap-2.5">
      <span className={`
        w-6 h-6 rounded-md flex items-center justify-center text-[10px] font-bold flex-shrink-0
        ${isActive
          ? 'bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] text-white'
          : 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
      }
      `}>
        {String(index + 1).padStart(2, '0')}
      </span>
        <div className="flex-1 min-w-0">
          <h4 className={`font-medium text-sm truncate ${isActive ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
            {chapter.title}
          </h4>
        </div>
      </div>
    </motion.button>
))
ChapterTab.displayName = 'ChapterTab'

// =============================================================================
// 分页指示器
// =============================================================================

const PageIndicator = memo(({current, total, onSelect}) => (
    <div className="flex items-center gap-1.5">
      {Array.from({length: total}).map((_, i) => (
          <motion.button
              key={i}
              onClick={() => onSelect(i)}
              whileHover={{scale: 1.2}}
              whileTap={{scale: 0.9}}
              className={`
          h-2 rounded-full transition-all duration-300
          ${i === current
                  ? 'w-6 bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA]'
                  : 'w-2 bg-[#E0E5EE] dark:bg-[#363D4D] hover:bg-[#C8D0DD] dark:hover:bg-[#4A5568]'
              }
        `}
          />
      ))}
    </div>
))
PageIndicator.displayName = 'PageIndicator'

// =============================================================================
// 主要组件
// =============================================================================

function ScriptReading({_config, _gameData, playerData, onComplete, onAction}) {
  const [currentChapter, setCurrentChapter] = useState(0)
  const [direction, setDirection] = useState(0)
  const contentRef = useRef(null)

  // 获取当前玩家的角色ID
  const characterId = useMemo(() => {
    console.log('[ScriptReading] playerData:', playerData)
    const player = playerData?.data?.find?.(p => p.player?.role === 'REAL' || p.role === 'REAL')
    console.log('[ScriptReading] 找到的玩家:', player)
    const id = player?.characterId || player?.player?.characterId
    console.log('[ScriptReading] 提取的 characterId:', id)
    return id
  }, [playerData])

  // 获取角色详情
  const {data: character, isLoading} = useQuery({
    queryKey: ['character', characterId],
    queryFn: () => getCharacterById(characterId),
    enabled: !!characterId,
    staleTime: 5 * 60 * 1000,
  })

  // 构建章节数据（背景故事、角色秘密、时间线）
  const chapters = useMemo(() => {
    if (!character) return []
    return [
      {
        id: 'background',
        title: '背景故事',
        icon: Scroll,
        content: character.backgroundStory || '暂无背景故事',
      },
      {
        id: 'secret',
        title: '角色秘密',
        icon: Eye,
        content: character.secret || '暂无秘密',
      },
      {
        id: 'timeline',
        title: '时间线',
        icon: Clock,
        content: character.timeline || '暂无时间线',
      },
    ]
  }, [character])

  const currentData = chapters[currentChapter]
  const isFirstChapter = currentChapter === 0
  const isLastChapter = currentChapter === chapters.length - 1

  const handleChapterChange = (newIndex) => {
    if (newIndex === currentChapter) return
    setDirection(newIndex > currentChapter ? 1 : -1)
    setCurrentChapter(newIndex)
    contentRef.current?.scrollTo(0, 0)
  }

  const handleNext = () => {
    if (!isLastChapter) {
      handleChapterChange(currentChapter + 1)
    } else {
      onAction?.('script_reading_complete', {characterId, chaptersRead: chapters.length})
      onComplete?.()
    }
  }

  const handlePrevious = () => {
    if (!isFirstChapter) {
      handleChapterChange(currentChapter - 1)
    }
  }

  const contentVariants = {
    enter: (direction) => ({
      x: direction > 0 ? 20 : -20,
      opacity: 0,
    }),
    center: {
      x: 0,
      opacity: 1,
    },
    exit: (direction) => ({
      x: direction < 0 ? 20 : -20,
      opacity: 0,
    }),
  }

  if (isLoading) {
    return (
        <div className="h-full flex items-center justify-center">
          <motion.div
              animate={{rotate: 360}}
              transition={{duration: 1, repeat: Infinity, ease: 'linear'}}
              className="w-8 h-8 border-2 border-[#7C8CD6] border-t-transparent rounded-full"
          />
        </div>
    )
  }

  const Icon = currentData?.icon || Scroll

  return (
      <div className="h-full relative overflow-hidden">
        <div className="absolute inset-0 pointer-events-none">
          <BackgroundDecor/>
        </div>

        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-8 relative z-10"
        >
          {/* 标题区 */}
          <motion.div variants={itemVariants} className="mb-6">
            <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
              剧本阅读
            </h2>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              角色：{character?.name || '未知角色'} · 第 {currentChapter + 1} / {chapters.length} 部分
            </p>
          </motion.div>

          {/* 主内容区 */}
          <div className="flex-1 flex gap-6 min-h-0">
            {/* 章节导航 - 玻璃态侧边栏 */}
            <motion.nav variants={itemVariants} className="w-52 flex-none hidden md:flex flex-col">
              <div
                  className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
                <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider mb-3 px-1">
                  阅读目录
                </p>
                <div className="flex-1 overflow-y-auto scrollbar-thin space-y-1.5 pr-1">
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
              </div>
            </motion.nav>

            {/* 阅读区域 - 玻璃态主卡片 */}
            <motion.div variants={itemVariants} className="flex-1 min-w-0 flex flex-col relative">
              {/* 卡片光晕 */}
              <div
                  className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

              {/* 主卡片 */}
              <div
                  className="relative flex-1 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden flex flex-col">
                {/* 顶部渐变线 */}
                <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

                {/* 章节标题 */}
                <div className="p-5 border-b border-[#E0E5EE] dark:border-[#363D4D]">
                  <div className="flex items-center gap-3">
                    <div
                        className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] flex items-center justify-center">
                      <Icon className="w-5 h-5 text-white"/>
                  </div>
                  <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                    {currentData?.title}
                  </h3>
                </div>
                </div>

                {/* 内容区域 */}
                <div
                    ref={contentRef}
                    className="flex-1 overflow-y-auto p-5 scrollbar-thin"
                >
                  <AnimatePresence mode="wait" custom={direction}>
                    <motion.div
                        key={currentChapter}
                        custom={direction}
                        variants={contentVariants}
                        initial="enter"
                        animate="center"
                        exit="exit"
                        transition={{duration: 0.3, ease: 'easeInOut'}}
                        className="text-[#2D3748] dark:text-[#E8ECF2] text-sm leading-relaxed whitespace-pre-wrap"
                    >
                      {currentData?.id === 'secret' ? (
                          <div
                              className="p-4 bg-[#7C8CD6]/5 dark:bg-[#7C8CD6]/10 rounded-lg border border-[#7C8CD6]/20">
                            {currentData.content}
                          </div>
                      ) : (
                          currentData?.content
                      )}
                    </motion.div>
                  </AnimatePresence>
                </div>

                {/* 底部导航 */}
                <div className="p-4 border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/40 dark:bg-[#222631]/40">
                  <div className="flex items-center justify-between">
                    <GhostButton
                        onClick={handlePrevious}
                        disabled={isFirstChapter}
                        className={isFirstChapter ? 'opacity-40' : ''}
                    >
                    <span className="flex items-center gap-1">
                      <ChevronLeft className="w-4 h-4"/>
                      上一部分
                    </span>
                    </GhostButton>

                    <PageIndicator
                        current={currentChapter}
                        total={chapters.length}
                        onSelect={handleChapterChange}
                    />

                    <GhostButton onClick={handleNext}>
                    <span className="flex items-center gap-1">
                      {isLastChapter ? '开始调查' : '下一部分'}
                      <motion.span
                          animate={{x: [0, 4, 0]}}
                          transition={{duration: 1.5, repeat: Infinity}}
                      >
                        <ChevronRight className="w-4 h-4"/>
                      </motion.span>
                    </span>
                    </GhostButton>
                  </div>
                </div>
            </div>
            </motion.div>
          </div>
        </motion.div>
      </div>
  )
}

ScriptReading.displayName = 'ScriptReading'
ScriptReading.phaseType = PHASE_TYPE.SCRIPT_READING

export default memo(ScriptReading)

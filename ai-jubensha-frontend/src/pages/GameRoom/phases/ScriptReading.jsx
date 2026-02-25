/**
 * @fileoverview ScriptReading 组件 - 透明背景 + 玻璃态卡片
 * @description 剧本阅读阶段，与 CharacterAssignment 风格保持一致
 */

import {memo, useRef, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {ChevronLeft, ChevronRight, Clock, MapPin} from 'lucide-react'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'

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
// 背景装饰 - 与 CharacterAssignment 风格一致
// =============================================================================

const BackgroundDecor = memo(() => (
    <>
      {/* 左上角装饰 */}
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
      {/* 右下角装饰 */}
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
      {/* 星星点缀 */}
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
// 章节导航项 - 玻璃态设计
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
          <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] mt-0.5 flex items-center gap-1">
            <Clock className="w-2.5 h-2.5 flex-shrink-0"/>
            <span className="truncate">{chapter.time}</span>
          </p>
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

function ScriptReading({_config, gameData, _playerData, onComplete, onAction}) {
  const [currentChapter, setCurrentChapter] = useState(0)
  const [direction, setDirection] = useState(0)
  const contentRef = useRef(null)

  const chapters = gameData?.scriptChapters || [
    {
      id: 'ch-001',
      title: '暴风雨之夜',
      time: '第一幕',
      location: '庄园大厅',
      content: `
        <p class="mb-4 leading-relaxed" style="color: inherit;">雨点像愤怒的拳头敲打着窗户。庄园内，六位陌生人围坐在壁炉旁，每个人心中都藏着比外面风暴更沉重的秘密。</p>
        <p class="mb-4 leading-relaxed" style="color: inherit;">庄园主人秦先生手握威士忌，站在众人面前。他的笑容并未到达眼底。</p>
        <p class="mb-4 leading-relaxed" style="color: inherit;">"今晚，我要揭开真相，"他宣布道，"关于遗产，关于背叛，关于——"</p>
        <p class="leading-relaxed" style="color: inherit;">灯光熄灭。一声尖叫。然后归于寂静。</p>
      `,
    },
    {
      id: 'ch-002',
      title: '你是谁',
      time: '背景介绍',
      location: '各处',
      content: `
        <p class="mb-4 leading-relaxed" style="color: inherit;">你不是偶然来到这里的。每位客人都是被邀请而来的，每个人都有原因。受害者知道关于你们每个人的某些事——值得为之杀人的秘密。</p>
        <p class="mb-4 leading-relaxed" style="color: inherit;">你的任务是双重的：找出凶手，同时保守自己的秘密。不要相信任何人。怀疑每一个人。</p>
        <p class="leading-relaxed" style="color: inherit;">记住：在这个游戏中，猎人和猎物的身份可能在眨眼间互换。</p>
      `,
    },
    {
      id: 'ch-003',
      title: '证据浮现',
      time: '犯罪现场',
      location: '书房',
      content: `
        <p class="mb-4 leading-relaxed" style="color: inherit;">书房门从里面反锁。窗户紧闭。然而秦先生倒在地上，一把古董拆信刀插在他的胸口。</p>
        <p class="mb-4 leading-relaxed" style="color: inherit;">一个密室谜案。经典的场景。但是谁有钥匙？谁有动机？谁有机会？</p>
        <p class="leading-relaxed" style="color: inherit;">风暴仍在肆虐。凶手就在你们之中。</p>
      `,
    },
    {
      id: 'ch-004',
      title: '任务简报',
      time: '指示',
      location: '机密',
      content: `
        <p class="mb-3 font-semibold" style="color: #7C8CD6;">主要目标：</p>
        <ul class="list-disc list-inside mb-4 space-y-2" style="color: inherit;">
          <li>收集证据并重建时间线</li>
          <li>审问其他客人并分析他们的陈述</li>
          <li>在凶手再次下手前指认凶手</li>
        </ul>
        <p class="mb-3 font-semibold" style="color: #F5A9C9;">次要目标：</p>
        <ul class="list-disc list-inside mb-4 space-y-2" style="color: inherit;">
          <li>保护你自己的秘密</li>
          <li>判断谁可以信任</li>
          <li>活过这个夜晚</li>
        </ul>
        <p class="italic text-sm" style="color: #8C96A5;">"真相很少是纯粹的，也绝不简单。" —— 奥斯卡·王尔德</p>
      `,
    },
  ]

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
      onAction?.('script_reading_complete', {chaptersRead: chapters.length})
      onComplete?.()
    }
  }

  const handlePrevious = () => {
    if (!isFirstChapter) {
      handleChapterChange(currentChapter - 1)
    }
  }

  // 内容切换动画
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

  return (
      <div className="h-full relative overflow-hidden">
        {/* 背景装饰 */}
        <div className="absolute inset-0 pointer-events-none">
          <BackgroundDecor/>
        </div>

        {/* 主内容 */}
        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-8 relative z-10"
        >
          {/* 标题区 - 与 CharacterAssignment 风格一致，左对齐 */}
          <motion.div variants={itemVariants} className="mb-6">
            <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
              剧本阅读
            </h2>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              第 {currentChapter + 1} / {chapters.length} 章
            </p>
          </motion.div>

          {/* 主内容区 */}
          <div className="flex-1 flex gap-6 min-h-0">
            {/* 章节导航 - 玻璃态侧边栏 */}
            <motion.nav variants={itemVariants} className="w-52 flex-none hidden md:flex flex-col">
              <div
                  className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
                <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider mb-3 px-1">
                  章节目录
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
                  <div className="flex items-center gap-2 text-[#8C96A5] dark:text-[#6B7788] text-xs mb-2">
                  <span className="px-2 py-0.5 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6] font-medium">
                    {currentData.time}
                  </span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                    <MapPin className="w-3 h-3"/>
                      {currentData.location}
                  </span>
                  </div>
                  <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                    {currentData.title}
                  </h3>
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
                        className="text-[#2D3748] dark:text-[#E8ECF2] text-sm leading-relaxed"
                        dangerouslySetInnerHTML={{__html: currentData.content}}
                    />
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
                      上一章
                    </span>
                    </GhostButton>

                    <PageIndicator
                        current={currentChapter}
                        total={chapters.length}
                        onSelect={handleChapterChange}
                    />

                    <GhostButton onClick={handleNext}>
                    <span className="flex items-center gap-1">
                      {isLastChapter ? '开始调查' : '下一章'}
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
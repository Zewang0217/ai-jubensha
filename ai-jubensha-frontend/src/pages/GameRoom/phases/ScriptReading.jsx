/**
 * @fileoverview ScriptReading 组件 - 透明背景 + 玻璃态卡片
 * @description 剧本阅读阶段，展示角色背景故事、秘密和时间线
 */

import {memo, useEffect, useMemo, useRef, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {ChevronLeft, ChevronRight, Clock, Eye, Scroll, Users} from 'lucide-react'
import {useQuery} from '@tanstack/react-query'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import PhaseBackgroundDecor from '../../../components/common/PhaseBackgroundDecor'
import {getCharacterById, getCharactersByScriptId} from '../../../services/api/character'
import {containerVariants, itemVariants} from '../config/animations'
import {PHASE_COLORS} from '../config/theme'

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
// 角色选择器组件（观察者模式专用）
// =============================================================================

/**
 * 角色选择器组件
 * @description 用于观察者模式下切换查看不同角色的剧本
 * @param {Object} props - 组件属性
 * @param {Array} props.characters - 角色列表
 * @param {number} props.selectedCharacterId - 当前选中的角色ID
 * @param {Function} props.onSelect - 角色选择回调函数
 * @returns {JSX.Element} 角色选择器组件
 */
const CharacterSelector = memo(({characters, selectedCharacterId, onSelect}) => (
    <div className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3">
      {/* 标题 */}
      <div className="flex items-center gap-2 mb-3 px-1">
        <Users className="w-4 h-4 text-[#7C8CD6]"/>
        <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider">
          角色列表
        </p>
      </div>

      {/* 角色列表 */}
      <div className="space-y-1.5 max-h-48 overflow-y-auto scrollbar-thin pr-1">
        {characters?.map((char) => (
            <motion.button
                key={char.id}
                onClick={() => onSelect(char.id)}
                whileHover={{x: 2}}
                whileTap={{scale: 0.98}}
                className={`
          w-full text-left p-2 rounded-lg transition-all duration-300 overflow-hidden
          ${selectedCharacterId === char.id
                    ? 'bg-white/80 dark:bg-[#222631]/80 backdrop-blur-md border border-[#7C8CD6]/50 shadow-md'
                    : 'bg-white/40 dark:bg-[#222631]/40 backdrop-blur-sm border border-transparent hover:bg-white/60 dark:hover:bg-[#222631]/60'
                }
        `}
            >
              <div className="flex items-center gap-2">
            <span className={`
              w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0
              ${selectedCharacterId === char.id
                  ? 'bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] text-white'
                  : 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
              }
            `}>
              {char.name.charAt(0)}
            </span>
                <span className={`font-medium text-sm truncate ${selectedCharacterId === char.id ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
              {char.name}
            </span>
              </div>
            </motion.button>
        ))}
      </div>
    </div>
))
CharacterSelector.displayName = 'CharacterSelector'

// =============================================================================
// 主要组件
// =============================================================================

/**
 * ScriptReading 组件
 * @description 剧本阅读阶段组件，支持真人模式和观察者模式
 * @param {Object} props - 组件属性
 * @param {Object} props._config - 配置信息
 * @param {Object} props._gameData - 游戏数据
 * @param {Object} props.playerData - 玩家数据
 * @param {Function} props.onComplete - 完成回调函数
 * @param {Function} props.onAction - 动作回调函数
 * @param {boolean} props.isObserverMode - 是否为观察者模式
 * @returns {JSX.Element} 剧本阅读组件
 */
function ScriptReading({_config, _gameData, playerData, onComplete, onAction, isObserverMode}) {
  const [currentChapter, setCurrentChapter] = useState(0)
  const [direction, setDirection] = useState(0)
  const [selectedCharacterId, setSelectedCharacterId] = useState(null)
  const contentRef = useRef(null)

  // 获取剧本ID（用于观察者模式）
  const scriptId = useMemo(() => {
    return _gameData?.scriptId || _gameData?.script?.id
  }, [_gameData])

  // 观察者模式：获取所有角色列表
  const {data: allCharacters, isLoading: isLoadingAllCharacters} = useQuery({
    queryKey: ['characters', 'script', scriptId],
    queryFn: () => getCharactersByScriptId(scriptId),
    enabled: isObserverMode && !!scriptId,
    staleTime: 5 * 60 * 1000,
  })

  // 初始化观察者模式下选中的角色
  useEffect(() => {
    if (isObserverMode && allCharacters?.length > 0 && !selectedCharacterId) {
      setSelectedCharacterId(allCharacters[0].id)
    }
  }, [isObserverMode, allCharacters, selectedCharacterId])

  // 获取当前玩家的角色ID（真人模式）
  const characterId = useMemo(() => {
    // 观察者模式：使用选中的角色ID
    if (isObserverMode) {
      console.log('[ScriptReading] 观察者模式，使用选中的角色ID:', selectedCharacterId)
      return selectedCharacterId
    }

    // 真人模式：从玩家数据中获取
    // playerData 格式：{data: [...]} 或直接是数组
    console.log('[ScriptReading] playerData:', playerData)
    console.log('[ScriptReading] playerData 类型:', typeof playerData)
    console.log('[ScriptReading] playerData 是否为数组:', Array.isArray(playerData))
    
    // 获取玩家数组
    const players = playerData?.data || playerData
    console.log('[ScriptReading] players:', players)
    console.log('[ScriptReading] players 是否为数组:', Array.isArray(players))
    console.log('[ScriptReading] players 长度:', players?.length)
    
    if (!Array.isArray(players) || players.length === 0) {
      console.log('[ScriptReading] 玩家数据为空或不是数组')
      return null
    }
    
    // 打印所有玩家的 playerRole
    console.log('[ScriptReading] 所有玩家:', players.map(p => ({ 
      id: p.id, 
      playerRole: p.playerRole, 
      playerRoleType: typeof p.playerRole,
      characterId: p.characterId 
    })))
    
    // 查找真人玩家（playerRole === 'REAL'）
    // 注意：后端返回的是枚举名称字符串，如 'REAL'
    const realPlayer = players.find(p => p.playerRole === 'REAL' || p.playerRole === 'real')
    console.log('[ScriptReading] 找到的真人玩家:', realPlayer)
    
    const id = realPlayer?.characterId
    console.log('[ScriptReading] 提取的 characterId:', id)
    return id
  }, [playerData, isObserverMode, selectedCharacterId])

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

  /**
   * 处理章节切换
   * @param {number} newIndex - 新章节索引
   */
  const handleChapterChange = (newIndex) => {
    if (newIndex === currentChapter) return
    setDirection(newIndex > currentChapter ? 1 : -1)
    setCurrentChapter(newIndex)
    contentRef.current?.scrollTo(0, 0)
  }

  /**
   * 处理角色选择（观察者模式）
   * @param {number} charId - 角色ID
   */
  const handleCharacterSelect = (charId) => {
    if (charId === selectedCharacterId) return
    setSelectedCharacterId(charId)
    setCurrentChapter(0) // 切换角色时重置章节
    contentRef.current?.scrollTo(0, 0)
  }

  /**
   * 处理下一步操作
   */
  const handleNext = () => {
    if (!isLastChapter) {
      handleChapterChange(currentChapter + 1)
    } else {
      // 通知 GameRoom 剧本阅读完成
      onAction?.('script_reading_complete', {characterId, chaptersRead: chapters.length})
      // 不直接调用 onComplete，等待后端广播 PHASE_CHANGE 消息
      // onComplete?.()
    }
  }

  /**
   * 处理上一步操作
   */
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

  // 加载状态判断
  const isLoadingData = isLoading || (isObserverMode && isLoadingAllCharacters)

  // 等待角色ID加载（真人模式下 playerData 可能还在加载）
  const isWaitingForCharacterId = !isObserverMode && !characterId && !isLoading

  // 调试日志
  console.log('[ScriptReading] 状态检查:', {
    isObserverMode,
    characterId,
    isLoading,
    isLoadingData,
    isWaitingForCharacterId,
    playerDataLength: playerData?.length || playerData?.data?.length || 0,
  })

  if (isLoadingData || isWaitingForCharacterId) {
    return (
        <div className="h-full flex flex-col items-center justify-center gap-4">
          <motion.div
              animate={{rotate: 360}}
              transition={{duration: 1, repeat: Infinity, ease: 'linear'}}
              className="w-8 h-8 border-2 border-[#7C8CD6] border-t-transparent rounded-full"
          />
          <p className="text-[#8C96A5] dark:text-[#6B7788] text-sm">
            {isWaitingForCharacterId ? '正在加载角色信息...' : '加载中...'}
          </p>
        </div>
    )
  }

  // 如果没有角色数据，显示提示
  if (!character) {
    return (
        <div className="h-full flex flex-col items-center justify-center gap-4">
          <p className="text-[#8C96A5] dark:text-[#6B7788]">
            暂无角色数据，请稍后重试
          </p>
        </div>
    )
  }

  const Icon = currentData?.icon || Scroll

  return (
      <div className="h-full relative overflow-hidden">
        <div className="absolute inset-0 pointer-events-none">
          <PhaseBackgroundDecor/>
        </div>

        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-8 relative z-10"
        >
          {/* 标题区 */}
          <motion.div variants={itemVariants} className="mb-6">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                剧本阅读
              </h2>
              {/* 观察者模式标识 */}
              {isObserverMode && (
                  <motion.div
                      initial={{opacity: 0, scale: 0.9}}
                      animate={{opacity: 1, scale: 1}}
                      className="flex items-center gap-1.5 px-3 py-1 bg-gradient-to-r from-[#7C8CD6]/10 to-[#A78BFA]/10 border border-[#7C8CD6]/30 rounded-full"
                  >
                    <Eye className="w-3.5 h-3.5 text-[#7C8CD6]"/>
                    <span className="text-xs font-medium text-[#7C8CD6]">观察者模式 - 可查看所有角色剧本</span>
                  </motion.div>
              )}
            </div>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              角色：{character?.name || '未知角色'} · 第 {currentChapter + 1} / {chapters.length} 部分
            </p>
          </motion.div>

          {/* 主内容区 */}
          <div className="flex-1 flex gap-6 min-h-0">
            {/* 左侧面板 - 章节导航或角色选择器 */}
            <motion.nav variants={itemVariants} className="w-52 flex-none hidden md:flex flex-col gap-3">
              {/* 观察者模式：角色选择器 */}
              {isObserverMode && allCharacters && (
                  <CharacterSelector
                      characters={allCharacters}
                      selectedCharacterId={selectedCharacterId}
                      onSelect={handleCharacterSelect}
                  />
              )}

              {/* 章节导航 - 玻璃态侧边栏 */}
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
                      {isLastChapter ? (isObserverMode ? '完成阅读' : '开始调查') : '下一部分'}
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

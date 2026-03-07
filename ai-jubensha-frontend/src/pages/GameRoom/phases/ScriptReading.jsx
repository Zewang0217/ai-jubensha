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
// 时间线解析工具
// =============================================================================

/**
 * 解析时间线文本为结构化数据
 * @param {string} timelineText - 原始时间线文本
 * @returns {Array<{time: string, content: string, isHighlight: boolean}>} 时间线条目数组
 */
const parseTimeline = (timelineText) => {
  if (!timelineText) return []

  const lines = timelineText.split('\n').filter(line => line.trim())

  return lines.map(line => {
    // 首先尝试匹配中文冒号（优先使用中文冒号作为分隔符）
    let match = line.match(/^(.+?)[：](.+)$/)

    // 如果没有中文冒号，尝试匹配英文冒号，但要排除时间格式（如 10:30）
    // 使用负向回顾后发 (?<!\d) 确保冒号前不是数字，或使用 (?=\s) 确保冒号后有内容
    if (!match) {
      // 匹配模式：非数字+冒号+任意内容，或者 数字+冒号+数字+冒号+任意内容（如 10:30:00）
      match = line.match(/^([^:]+?[a-zA-Z\u4e00-\u9fa5])\s*:\s*(.+)$/) ||
          line.match(/^(.+?\d{1,2}:\d{2}(?::\d{2})?)\s*:\s*(.+)$/)
    }

    if (match) {
      const time = match[1].trim()
      const content = match[2].trim()

      // 判断是否为关键时间点（案发相关）
      const isHighlight = /案发|谋杀|实施|计划|死亡|自杀/i.test(content) ||
          /案发|谋杀|实施|计划|死亡|自杀/i.test(time)

      return {time, content, isHighlight}
    }

    // 无法解析的整行作为内容
    return {time: '', content: line.trim(), isHighlight: false}
  }).filter(item => item.content)
}

// =============================================================================
// 时间线组件
// =============================================================================

/**
 * 时间线展示组件
 * @param {Object} props - 组件属性
 * @param {string} props.content - 时间线文本内容
 */
const TimelineViewer = memo(({content}) => {
  const timelineItems = useMemo(() => parseTimeline(content), [content])

  if (timelineItems.length === 0) {
    return <p className="text-[#5A6978] dark:text-[#9CA8B8] text-sm italic">暂无时间线数据</p>
  }

  return (
      <div className="relative max-w-5xl mx-auto">
        {/* 立体时间线中轴线 - 带阴影效果 */}
        <div
            className="absolute left-4 md:left-5 top-3 bottom-3 w-1 bg-gradient-to-b from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9] rounded-full shadow-lg shadow-[#7C8CD6]/20"/>

        <div className="space-y-4">
          {timelineItems.map((item, index) => (
              <motion.div
                  key={index}
                  initial={{opacity: 0, x: -10}}
                  animate={{opacity: 1, x: 0}}
                  transition={{delay: index * 0.05, duration: 0.3}}
                  className="relative pl-10 md:pl-12"
              >
                {/* 立体时间节点 */}
                <div className={`
              absolute left-2 md:left-3 top-2 w-5 h-5 rounded-full flex items-center justify-center
              ${item.isHighlight
                    ? 'bg-gradient-to-br from-[#F5A9C9] to-[#E879A9] shadow-lg shadow-[#F5A9C9]/30 ring-2 ring-white dark:ring-[#222631]'
                    : 'bg-gradient-to-br from-[#7C8CD6] to-[#5E6BCE] shadow-md shadow-[#7C8CD6]/20 ring-2 ring-white dark:ring-[#222631]'
                }
            `}>
                  <div className="w-2 h-2 rounded-full bg-white"/>
                </div>

                {/* 立体卡片内容 */}
                <div className={`
              rounded-xl p-4 border backdrop-blur-sm
              ${item.isHighlight
                    ? 'bg-gradient-to-r from-white via-white to-[#F5A9C9]/5 dark:from-[#222631] dark:via-[#222631] dark:to-[#F5A9C9]/10 border-[#F5A9C9]/30 shadow-md shadow-[#F5A9C9]/5'
                    : 'bg-gradient-to-r from-white via-white to-[#7C8CD6]/5 dark:from-[#222631] dark:via-[#222631] dark:to-[#7C8CD6]/10 border-[#E0E5EE] dark:border-[#363D4D] shadow-sm'
                }
            `}>
                  {/* 时间标签 - 带图标 */}
                  {item.time && (
                      <div className="flex items-center gap-2 mb-2">
                        <div className={`
                    w-6 h-6 rounded-lg flex items-center justify-center
                    ${item.isHighlight
                            ? 'bg-[#F5A9C9]/15'
                            : 'bg-[#7C8CD6]/10'
                        }
                  `}>
                          <Clock className={`w-3.5 h-3.5 ${item.isHighlight ? 'text-[#F5A9C9]' : 'text-[#7C8CD6]'}`}/>
                        </div>
                        <span className={`
                    text-xs font-bold uppercase tracking-wide
                    ${item.isHighlight
                            ? 'text-[#C2417A] dark:text-[#F5A9C9]'
                            : 'text-[#5A69A3] dark:text-[#A5B0E8]'
                        }
                  `}>
                    {item.time}
                  </span>
                        {item.isHighlight && (
                            <span
                                className="px-2 py-0.5 rounded-full bg-[#F5A9C9]/15 text-[#F5A9C9] text-[10px] font-medium">
                      关键
                    </span>
                        )}
                      </div>
                  )}

                  {/* 内容文本 */}
                  <p className={`
                text-sm md:text-[15px] leading-relaxed
                ${item.isHighlight
                      ? 'text-[#2D3748] dark:text-[#E8ECF2] font-medium'
                      : 'text-[#4A5568] dark:text-[#B8C0D0]'
                  }
              `}>
                    {item.content}
                  </p>
                </div>
              </motion.div>
          ))}
        </div>
      </div>
  )
})
TimelineViewer.displayName = 'TimelineViewer'

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

const ChapterTab = memo(({chapter, isActive, onClick, index}) => {
  const Icon = chapter.icon || Scroll

  return (
    <motion.button
        onClick={onClick}
        whileHover={{x: 2}}
        whileTap={{scale: 0.98}}
        className={`
        w-full text-left rounded-lg transition-all duration-300
        ${isActive
            ? 'bg-white dark:bg-[#222631] border border-[#7C8CD6]/30'
            : 'bg-transparent hover:bg-white/50 dark:hover:bg-[#222631]/50 border border-transparent'
        }
      `}
    >
      <div className="p-2.5 flex items-center gap-2.5">
        {/* 序号 */}
        <span className={`
          w-5 h-5 rounded flex items-center justify-center text-[10px] font-medium flex-shrink-0
          ${isActive
            ? 'bg-[#7C8CD6] text-white'
            : 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
        }
        `}>
          {String(index + 1).padStart(2, '0')}
        </span>

        {/* 图标 */}
        <div className={`
          w-7 h-7 rounded-md flex items-center justify-center flex-shrink-0
          ${isActive ? 'bg-[#7C8CD6]/10 dark:bg-[#7C8CD6]/20' : 'bg-transparent'}
        `}>
          <Icon className={`w-3.5 h-3.5 ${isActive ? 'text-[#7C8CD6]' : 'text-[#8C96A5]'}`}/>
        </div>

        {/* 标题 */}
        <div className="flex-1 min-w-0">
          <h4 className={`font-medium text-sm truncate ${isActive ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
            {chapter.title}
          </h4>
        </div>

        {/* 激活指示点 */}
        {isActive && (
            <div className="w-1.5 h-1.5 rounded-full bg-[#7C8CD6] flex-shrink-0"/>
        )}
      </div>
    </motion.button>
  )
})
ChapterTab.displayName = 'ChapterTab'

// =============================================================================
// 分页指示器
// =============================================================================

const PageIndicator = memo(({current, total, onSelect}) => (
    <div className="flex items-center gap-1.5">
      {Array.from({length: total}).map((_, i) => (
          <button
              key={i}
              onClick={() => onSelect(i)}
              className={`
                h-2 rounded-full transition-all duration-200
                ${i === current
                  ? 'w-6 bg-[#7C8CD6]'
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
    <div
        className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-sm border border-[#E0E5EE]/40 dark:border-[#363D4D]/40 rounded-2xl p-4">
      {/* 标题 */}
      <div className="flex items-center gap-2 mb-3 px-1">
        <Users className="w-4 h-4 text-[#A78BFA]"/>
        <p className="text-[#5A6978] dark:text-[#9CA8B8] text-sm font-medium">角色列表</p>
      </div>

      {/* 角色列表 */}
      <div className="space-y-1.5 max-h-48 overflow-y-auto scrollbar-thin">
        {characters?.map((char) => (
            <motion.button
                key={char.id}
                onClick={() => onSelect(char.id)}
                whileHover={{x: 2}}
                whileTap={{scale: 0.98}}
                className={`
                  w-full text-left rounded-xl transition-all duration-300
                  ${selectedCharacterId === char.id
                    ? 'bg-white dark:bg-[#222631] border border-[#A78BFA]/30'
                    : 'bg-transparent hover:bg-white/50 dark:hover:bg-[#222631]/50 border border-transparent'
                }
                `}
            >
              <div className="p-2.5 flex items-center gap-3">
                <span className={`
                  w-8 h-8 rounded-lg flex items-center justify-center text-sm font-medium flex-shrink-0
                  ${selectedCharacterId === char.id
                    ? 'bg-[#A78BFA] text-white'
                    : 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
                }
                `}>
                  {char.name.charAt(0)}
                </span>
                <span
                    className={`font-medium text-sm truncate flex-1 ${selectedCharacterId === char.id ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
                  {char.name}
                </span>
                {selectedCharacterId === char.id && (
                    <div className="w-1.5 h-1.5 rounded-full bg-[#A78BFA] flex-shrink-0"/>
                )}
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
DM评分响应: {"scores": [{"playerId": "1024", "score": 71, "breakdown": {"motive": 16, "method": 15, "clues": 18, "accuracy": 26}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1025", "score": 78, "breakdown": {"motive": 15, "method": 19, "clues": 19, "accuracy": 25}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1026", "score": 94, "breakdown": {"motive": 18, "method": 17, "clues": 17, "accuracy": 33}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1027", "score": 88, "breakdown": {"motive": 17, "method": 19, "clues": 16, "accuracy": 33}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1028", "score": 92, "breakdown": {"motive": 19, "method": 15, "clues": 15, "accuracy": 27}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1029", "score": 87, "breakdown": {"motive": 15, "method": 15, "clues": 19, "accuracy": 34}, "comment": "表现不错，推理逻辑清晰"},{"playerId": "1030", "score": 83, "breakdown": {"motive": 18, "method": 19, "clues": 18, "accuracy": 26}, "comment": "表现不错，推理逻辑清晰"}], "summary": "整体游戏表现良好，大家都很投入，推理过程精彩。", "ending": "案发当晚，侄子张明趁人不备，在叔叔的茶中下了毒。他觊觎遗产已久，得知叔叔要修改遗嘱后，决定先下手为强。真相大白，正义终将到来。"}
17:05:36.835 INFO  o.j.a.a.s.DiscussionServiceImpl - 提取到结局叙述: 案发当晚，侄子张明趁人不备，在叔叔的茶中下了毒。他觊觎遗产已久，得知叔叔要修改遗嘱后，决定先下手为强。真相大白，正义终将到来。
17:05:36.835 INFO  o.j.a.a.s.DiscussionServiceImpl - Judge总结: 【Mock Judge】经过激烈的讨论，各方观点已经充分表达。主要争议集中在凶手的动机和作案手法上。希望大家在投票时能够综合考虑所有线索。讨论内容长度：9字符。
17:05:36.835 INFO  o.j.a.a.s.DiscussionServiceImpl - 讨论已完成，游戏ID: 168
17:05:36.836 INFO  o.j.a.w.service.WebSocketServiceImpl - 广播阶段变化通知到游戏 168: DISCUSSION -> SUMMARY, message=讨论结束，进入真相揭晓阶段
17:05:36.836 INFO  o.j.a.a.s.DiscussionServiceImpl - [讨论结束] 已广播阶段切换消息: DISCUSSION -> SUMMARY
17:05:36.838 INFO  o.j.a.w.service.WebSocketServiceImpl - 广播游戏结束通知到游戏 168: 游戏结束，请查看最终评分和真相揭晓 * @param {Object} props - 组件属性
 * @param {Object} props.config - 配置信息
 * @param {Object} props.gameData - 游戏数据
 * @param {Object} props.playerData - 玩家数据
 * @param {Function} props.onComplete - 完成回调函数
 * @param {Function} props.onAction - 动作回调函数
 * @param {boolean} props.isObserverMode - 是否为观察者模式
 * @returns {JSX.Element} 剧本阅读组件
 */
function ScriptReading({config: _config, gameData, playerData, onComplete: _onComplete, onAction, isObserverMode}) {
  const [currentChapter, setCurrentChapter] = useState(0)
  const [direction, setDirection] = useState(0)
  const [selectedCharacterId, setSelectedCharacterId] = useState(null)
  const contentRef = useRef(null)

  // 获取剧本ID（用于观察者模式）
  const scriptId = useMemo(() => {
    const id = gameData?.scriptId || gameData?.script?.id
    console.log('[ScriptReading] ========== 获取剧本ID ==========')
    console.log('[ScriptReading] gameData:', gameData)
    console.log('[ScriptReading] gameData?.scriptId:', gameData?.scriptId)
    console.log('[ScriptReading] gameData?.script?.id:', gameData?.script?.id)
    console.log('[ScriptReading] 最终 scriptId:', id)
    console.log('[ScriptReading] ==============================')
    return id
  }, [gameData])

  // 观察者模式：获取所有角色列表
  // 优先使用 gameData 中的 characters，必要时从 API 获取
  const gameDataCharacters = useMemo(() => {
    return gameData?.characters || []
  }, [gameData])

  const {data: apiCharacters, isLoading: isLoadingAllCharacters} = useQuery({
    queryKey: ['characters', 'script', scriptId],
    queryFn: () => getCharactersByScriptId(scriptId),
    enabled: isObserverMode && !!scriptId && gameDataCharacters.length === 0,
    staleTime: 5 * 60 * 1000,
  })

  // 合并角色列表：优先使用 gameData 中的数据，否则使用 API 数据
  const allCharacters = useMemo(() => {
    if (gameDataCharacters.length > 0) {
      console.log('[ScriptReading] 使用 gameData.characters:', gameDataCharacters.length, '个角色')
      return gameDataCharacters
    }
    if (apiCharacters) {
      const chars = apiCharacters?.data || apiCharacters || []
      console.log('[ScriptReading] 使用 API 获取的角色:', chars.length, '个角色')
      return chars
    }
    return []
  }, [gameDataCharacters, apiCharacters])

  // 初始化观察者模式下选中的角色
  useEffect(() => {
    if (isObserverMode && allCharacters.length > 0 && !selectedCharacterId) {
      console.log('[ScriptReading] 初始化观察者模式选中的角色:', allCharacters[0].id)
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
    console.log('[ScriptReading] handleNext 被调用, isLastChapter:', isLastChapter, 'isObserverMode:', isObserverMode)
    if (!isLastChapter) {
      handleChapterChange(currentChapter + 1)
    } else {
      console.log('[ScriptReading] 剧本阅读完成，调用 onAction')
      console.log('[ScriptReading] characterId:', characterId, 'chaptersRead:', chapters.length)
      // 只调用 onAction，由 handleAction 统一处理阶段完成逻辑
      // 避免同时调用 onComplete 导致重复触发 handlePhaseComplete
      onAction?.('script_reading_complete', {characterId, chaptersRead: chapters.length})
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
  const isLoadingData = isLoading || (isObserverMode && isLoadingAllCharacters && allCharacters.length === 0)

  // 观察者模式：等待角色选择
  const isWaitingForObserverSelection = isObserverMode && !selectedCharacterId && allCharacters.length > 0

  // 等待角色ID加载（真人模式下 playerData 可能还在加载）
  const isWaitingForCharacterId = !isObserverMode && !characterId && !isLoading

  // 调试日志
  console.log('[ScriptReading] 状态检查:', {
    isObserverMode,
    characterId,
    selectedCharacterId,
    allCharactersCount: allCharacters.length,
    isLoading,
    isLoadingAllCharacters,
    isLoadingData,
    isWaitingForCharacterId,
    isWaitingForObserverSelection,
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

  // 观察者模式：如果没有角色列表，显示提示
  if (isObserverMode && allCharacters.length === 0) {
    return (
        <div className="h-full flex flex-col items-center justify-center gap-4">
          <p className="text-[#8C96A5] dark:text-[#6B7788]">
            暂无角色数据，请稍后重试
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
            className="h-full flex flex-col p-5 md:p-6 relative z-10"
        >
          {/* 标题区 */}
          <motion.div variants={itemVariants} className="mb-4">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                剧本阅读
              </h2>
              {isObserverMode && (
                  <span className="px-3 py-1 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6] text-sm font-medium">
                    观察者模式
                  </span>
              )}
            </div>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              正在阅读 <span
                className="font-medium text-[#2D3748] dark:text-[#E8ECF2]">{character?.name || '未知角色'}</span> 的剧本
              · 第 {currentChapter + 1} / {chapters.length} 部分
            </p>
          </motion.div>

          {/* 主内容区 */}
          <div className="flex-1 flex gap-6 min-h-0">
            {/* 左侧面板 - 章节导航或角色选择器 */}
            <motion.nav variants={itemVariants} className="w-64 flex-none hidden md:flex flex-col gap-3">
              {/* 观察者模式：角色选择器 */}
              {isObserverMode && allCharacters.length > 0 && (
                  <CharacterSelector
                      characters={allCharacters}
                      selectedCharacterId={selectedCharacterId}
                      onSelect={handleCharacterSelect}
                  />
              )}

              {/* 章节导航 - 扁平化设计 */}
              <div
                  className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-sm border border-[#E0E5EE]/40 dark:border-[#363D4D]/40 rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
                {/* 面板头部 - 简化 */}
                <div className="flex items-center gap-2 mb-3 px-1">
                  <Scroll className="w-4 h-4 text-[#7C8CD6]"/>
                  <p className="text-[#5A6978] dark:text-[#9CA8B8] text-sm font-medium">阅读目录</p>
                </div>

                {/* 章节列表 */}
                <div className="flex-1 overflow-y-auto scrollbar-thin space-y-1.5">
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
              <div className="absolute -inset-0.5 rounded-xl bg-[#7C8CD6]/10 blur-lg opacity-40"/>

              {/* 主卡片 */}
              <div
                  className="relative flex-1 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-sm rounded-xl border border-[#E0E5EE]/60 dark:border-[#363D4D]/60 overflow-hidden flex flex-col">
                {/* 顶部装饰条 */}
                <div className="h-1 bg-[#7C8CD6]"/>

                {/* 章节标题 */}
                <div
                    className="p-4 md:p-5 border-b border-[#E0E5EE]/50 dark:border-[#363D4D]/50 bg-white/40 dark:bg-[#222631]/40 flex-shrink-0">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-[#7C8CD6] flex items-center justify-center flex-shrink-0">
                      <Icon className="w-5 h-5 text-white"/>
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg md:text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                        {currentData?.title}
                      </h3>
                      <p className="text-xs text-[#8C96A5] dark:text-[#6B7788] mt-0.5">
                        {currentChapter === 0 && '了解角色的过去与经历'}
                        {currentChapter === 1 && '只有你自己知道的秘密'}
                        {currentChapter === 2 && '案发前后的关键时间点'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* 内容区域 */}
                <div
                    ref={contentRef}
                    className="flex-1 overflow-y-auto px-5 md:px-8 lg:px-12 py-5 scrollbar-thin"
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
                    >
                      {currentData?.id === 'secret' ? (
                          <div className="max-w-5xl mx-auto">
                            <div
                                className="p-5 md:p-6 bg-[#7C8CD6]/5 dark:bg-[#7C8CD6]/10 rounded-xl border border-[#7C8CD6]/20">
                              <div className="flex items-center gap-2 mb-3">
                                <Eye className="w-4 h-4 text-[#7C8CD6]"/>
                                <span className="text-xs font-medium text-[#7C8CD6]">绝密信息</span>
                              </div>
                              <p className="text-base md:text-lg text-[#2D3748] dark:text-[#E8ECF2] leading-relaxed">
                                {currentData.content}
                              </p>
                            </div>
                          </div>
                      ) : currentData?.id === 'timeline' ? (
                          <TimelineViewer content={currentData.content}/>
                      ) : (
                          <div className="max-w-5xl mx-auto">
                            <p className="text-base md:text-lg text-[#2D3748] dark:text-[#E8ECF2] leading-relaxed whitespace-pre-wrap">
                              {currentData?.content}
                            </p>
                          </div>
                      )}
                    </motion.div>
                  </AnimatePresence>
                </div>

                {/* 底部导航 */}
                <div
                    className="p-3 md:p-4 border-t border-[#E0E5EE]/50 dark:border-[#363D4D]/50 bg-white/40 dark:bg-[#222631]/40 flex-shrink-0">
                  <div className="flex items-center justify-between max-w-5xl mx-auto">
                    <GhostButton
                        onClick={handlePrevious}
                        disabled={isFirstChapter}
                        className={`px-3 py-1.5 text-sm ${isFirstChapter ? 'opacity-40 cursor-not-allowed' : ''}`}
                    >
                      <span className="flex items-center gap-1.5">
                        <ChevronLeft className="w-4 h-4"/>
                        上一部分
                      </span>
                    </GhostButton>

                    <PageIndicator
                        current={currentChapter}
                        total={chapters.length}
                        onSelect={handleChapterChange}
                    />

                    <GhostButton
                        onClick={handleNext}
                        className="px-3 py-1.5 text-sm"
                    >
                      <span className="flex items-center gap-1.5">
                        {isLastChapter ? (isObserverMode ? '完成阅读' : '开始调查') : '下一部分'}
                        <ChevronRight className="w-4 h-4"/>
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

/**
 * @fileoverview Investigation 组件 - 透明背景 + 玻璃态卡片
 * @description 搜证阶段，与 CharacterAssignment/ScriptReading 风格保持一致
 */

import {memo, useCallback, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {ChevronRight, Eye, Globe, Lock, Search, Unlock, X} from 'lucide-react'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import ClueCard from '../../../components/ui/ClueCard'
import {publicClue} from '../../../services/api/clue'

// =============================================================================
// 动画配置
// =============================================================================

const containerVariants = {
  hidden: {opacity: 0},
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.06,
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
// 场景卡片
// =============================================================================

/**
 * 场景卡片组件
 * @param {Object} props - 组件属性
 * @param {Object} props.scene - 场景数据
 * @param {boolean} props.isSelected - 是否选中
 * @param {boolean} props.isLocked - 是否锁定
 * @param {Function} props.onClick - 点击回调
 * @param {number} props.clueCount - 线索数量
 * @returns {JSX.Element} 场景卡片元素
 */
const SceneCard = memo(({scene, isSelected, isLocked, onClick, clueCount}) => (
    <motion.button
        onClick={onClick}
        disabled={isLocked}
        whileHover={!isLocked ? {scale: 1.02, y: -2} : {}}
        whileTap={!isLocked ? {scale: 0.98} : {}}
        className={`
      w-full text-left p-3 rounded-xl transition-all duration-300 relative overflow-hidden
      ${isSelected
            ? 'bg-white/80 dark:bg-[#222631]/80 backdrop-blur-md border border-[#7C8CD6]/50 shadow-lg'
            : isLocked
                ? 'bg-white/30 dark:bg-[#222631]/30 backdrop-blur-sm border border-[#E0E5EE]/50 dark:border-[#363D4D]/50 opacity-60'
                : 'bg-white/60 dark:bg-[#222631]/60 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D] hover:bg-white/80 dark:hover:bg-[#222631]/80'
        }
    `}
    >
      {/* 选中指示条 */}
      {isSelected && (
          <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-[#7C8CD6] to-[#A78BFA]"/>
      )}

      <div className="flex items-start gap-3">
        {/* 图标 */}
        <div className={`
        w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0
        ${isLocked
            ? 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
            : isSelected
                ? 'bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] text-white'
                : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#7C8CD6]'
        }
      `}>
          {isLocked ? <Lock className="w-4 h-4"/> : <Search className="w-4 h-4"/>}
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between gap-2">
            <h4 className={`font-medium truncate ${isSelected ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}>
              {scene.name}
            </h4>
            {!isLocked && clueCount > 0 && (
                <span className={`
              text-[10px] px-1.5 py-0.5 rounded-full flex-shrink-0
              ${isSelected
                    ? 'bg-[#7C8CD6]/10 text-[#7C8CD6]'
                    : 'bg-[#E0E5EE] dark:bg-[#363D4D] text-[#8C96A5]'
                }
            `}>
              {clueCount}
            </span>
            )}
          </div>
          <p className="text-xs text-[#8C96A5] dark:text-[#6B7788] mt-1 line-clamp-2">
            {scene.description}
          </p>
        </div>
      </div>
    </motion.button>
))

SceneCard.displayName = 'SceneCard'

// =============================================================================
// 进度条组件
// =============================================================================

/**
 * 进度条组件
 * @param {Object} props - 组件属性
 * @param {number} props.progress - 进度百分比
 * @param {number} props.current - 当前数量
 * @param {number} props.total - 总数量
 * @returns {JSX.Element} 进度条元素
 */
const ProgressBar = memo(({progress, current, total}) => (
    <div className="flex items-center gap-3">
      <div className="flex-1 h-2 bg-[#E0E5EE] dark:bg-[#363D4D] rounded-full overflow-hidden">
        <motion.div
            className="h-full bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] rounded-full"
            initial={{width: 0}}
            animate={{width: `${progress}%`}}
            transition={{duration: 0.5, ease: 'easeOut'}}
        />
      </div>
      <span className="text-xs text-[#8C96A5] dark:text-[#6B7788] font-medium whitespace-nowrap">
      {current}/{total}
    </span>
    </div>
))

ProgressBar.displayName = 'ProgressBar'

// =============================================================================
// 主要组件
// =============================================================================

/**
 * Investigation 组件主函数
 * @param {Object} props - 组件属性
 * @param {Object} props._config - 阶段配置
 * @param {Object} props.gameData - 游戏数据
 * @param {Function} props.onComplete - 完成回调
 * @param {Function} props.onAction - 操作回调
 * @param {boolean} props.isObserverMode - 是否为观察者模式
 * @returns {JSX.Element} 组件元素
 */
function Investigation({_config, gameData, onComplete, onAction, isObserverMode = false}) {
  const [selectedScene, setSelectedScene] = useState(null)
  const [revealedClues, setRevealedClues] = useState(new Set())
  const [publicClues, setPublicClues] = useState(new Set())
  const [limitReached, setLimitReached] = useState(false)
  
  // 公开确认弹窗状态
  const [showPublicModal, setShowPublicModal] = useState(false)
  const [selectedClueForPublic, setSelectedClueForPublic] = useState(null)

  // 搜证次数限制（非观察者模式下有效）
  const totalChances = gameData?.totalChances ?? 3
  const [remainingChances, setRemainingChances] = useState(gameData?.remainingChances ?? 3)

  // 调试日志
  console.log('[Investigation] isObserverMode:', isObserverMode)
  console.log('[Investigation] remainingChances:', remainingChances, 'totalChances:', totalChances)

  const scenes = gameData?.scenes || [
    {
      id: 'scene-001',
      name: '书房',
      description: '尸体被发现的地方，门从里面反锁。',
      isLocked: false,
      clueCount: 3,
      clues: [
        {
          id: 'c1',
          name: '染血的拆信刀',
          description: '凶器。古董银质，刻有受害者姓名首字母。但刀柄上是谁的指纹？',
          type: '凶器'
        },
        {id: 'c2', name: '打翻的茶杯', description: '一杯伯爵红茶被打翻。污渍显示这里发生过搏斗。', type: '物证'},
        {id: 'c3', name: '遗嘱草稿', description: '未签署。改变一切。有人即将失去一切。', type: '文件'},
      ],
    },
    {
      id: 'scene-002',
      name: '客厅',
      description: '客人们等待的地方，气氛紧张。',
      isLocked: false,
      clueCount: 2,
      clues: [
        {id: 'c4', name: '断电记录', description: '精确在晚上8:15。持续整整5分钟。足够杀人了。', type: '时间线'},
        {
          id: 'c5',
          name: '窗户插销',
          description: '从外面被强行打开。但这扇窗朝向悬崖。不可能从这里逃脱。',
          type: '矛盾点'
        },
      ],
    },
    {
      id: 'scene-003',
      name: '厨房',
      description: '毒药在这里准备。',
      isLocked: false,
      clueCount: 2,
      clues: [
        {id: 'c6', name: '空药瓶', description: '医用级砒霜。处方标签被撕掉。需要专业知识才能获取。', type: '毒药'},
        {id: 'c7', name: '皱巴巴的纸条', description: '"8点在书房见我。我们得谈谈。-匿名"', type: '信息'},
      ],
    },
    {
      id: 'scene-004',
      name: '主卧',
      description: '受害者的私人空间，秘密藏在这里。',
      isLocked: false,
      clueCount: 2,
      clues: [
        {id: 'c8', name: '私人日记', description: '最后一篇："我知道谁背叛了我。今晚，所有人都会知道真相。"', type: '证据'},
        {id: 'c9', name: '保险箱', description: '上锁了。密码未知。里面有什么贵重物品？', type: '谜题'},
      ],
    },
    {
      id: 'scene-005',
      name: '地下室',
      description: '储藏室和秘密。需要特殊权限才能进入。',
      isLocked: true,
      clueCount: 0,
      clues: [],
    },
  ]

  const currentScene = scenes.find(s => s.id === selectedScene)
  const totalClues = scenes.reduce((acc, s) => acc + s.clues.length, 0)
  const progress = Math.round((revealedClues.size / totalClues) * 100)

  /**
   * 处理线索揭示操作
   * @param {string} clueId - 线索ID
   * @returns {void}
   */
  const handleRevealClue = useCallback((clueId) => {
    // 观察者模式下不能搜证
    if (isObserverMode) return
    
    // 检查搜证次数是否已达上限
    if (remainingChances <= 0) {
      setLimitReached(true)
      setTimeout(() => setLimitReached(false), 2000)
      return
    }
    
    // 如果已经揭示过，不扣减次数，也不弹窗
    if (revealedClues.has(clueId)) {
      return
    }
    
    // 扣减搜证次数
    setRemainingChances(prev => prev - 1)
    
    // 揭示线索
    setRevealedClues(prev => new Set([...prev, clueId]))
    onAction?.('clue_revealed', {clueId, sceneId: selectedScene})
    
    // 找到线索信息，立即弹出公开确认弹窗
    const clue = scenes.flatMap(s => s.clues).find(c => c.id === clueId)
    if (clue) {
      setSelectedClueForPublic(clue)
      setShowPublicModal(true)
    }
  }, [selectedScene, onAction, isObserverMode, remainingChances, revealedClues, scenes])

  /**
   * 确认公开线索
   * @returns {Promise<void>}
   */
  const confirmPublicClue = useCallback(async () => {
    if (!selectedClueForPublic) return
    
    try {
      // 调用后端 API 公开线索
      await publicClue(selectedClueForPublic.id)
      
      // 更新本地状态
      setPublicClues(prev => new Set([...prev, selectedClueForPublic.id]))
      onAction?.('clue_public', {clueId: selectedClueForPublic.id, sceneId: selectedScene})
      
      console.log('[Investigation] 线索已公开:', selectedClueForPublic.id)
    } catch (error) {
      console.error('[Investigation] 公开线索失败:', error)
    } finally {
      setShowPublicModal(false)
      setSelectedClueForPublic(null)
    }
  }, [selectedClueForPublic, selectedScene, onAction])

  /**
   * 取消公开操作
   * @returns {void}
   */
  const cancelPublic = useCallback(() => {
    setShowPublicModal(false)
    setSelectedClueForPublic(null)
  }, [])

  /**
   * 处理完成调查操作
   * @returns {void}
   */
  const handleComplete = () => {
    onAction?.('investigation_complete', {
      revealedClues: Array.from(revealedClues),
      totalClues,
    })
    onComplete?.()
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
          {/* 观察者模式横幅提示 */}
          {isObserverMode && (
            <motion.div
              variants={itemVariants}
              className="mb-4 p-4 rounded-xl bg-gradient-to-r from-[#7C8CD6]/10 to-[#A78BFA]/10 border border-[#7C8CD6]/20 backdrop-blur-sm"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#7C8CD6]/30 to-[#A78BFA]/30 flex items-center justify-center">
                  <Eye className="w-5 h-5 text-[#7C8CD6]"/>
                </div>
                <div className="flex-1">
                  <h3 className="text-sm font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                    观察者模式已启用
                  </h3>
                  <p className="text-xs text-[#8C96A5] dark:text-[#6B7788] mt-0.5">
                    您可以查看所有线索，但无法进行搜证或公开操作
                  </p>
                </div>
              </div>
            </motion.div>
          )}

          {/* 标题区 - 左对齐 */}
          <motion.div variants={itemVariants} className="mb-6">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                {isObserverMode ? '线索查看' : '现场搜证'}
              </h2>
              {/* 观察者模式徽章 */}
              {isObserverMode && (
                <motion.div
                  initial={{opacity: 0, scale: 0.8}}
                  animate={{opacity: 1, scale: 1}}
                  transition={{delay: 0.2}}
                  className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 border border-[#7C8CD6]/30"
                >
                  <Eye className="w-4 h-4 text-[#7C8CD6]"/>
                  <span className="text-sm font-medium text-[#7C8CD6]">观察者模式</span>
                </motion.div>
              )}
            </div>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              {isObserverMode ? '观察者模式：可查看所有线索，无法进行搜证操作' : '搜集线索，揭开真相'}
            </p>
          </motion.div>

          {/* 进度条 - 仅在非观察者模式下显示 */}
          {!isObserverMode && (
            <motion.div variants={itemVariants} className="mb-6 max-w-md">
              {/* 搜证次数提示 */}
              <div className="flex items-center justify-between mb-2">
                <ProgressBar
                    progress={progress}
                    current={revealedClues.size}
                    total={totalClues}
                />
                <div className="flex items-center gap-2 ml-4">
                  <span className={`text-xs font-medium ${remainingChances <= 0 ? 'text-red-500' : 'text-[#7C8CD6]'}`}>
                    剩余搜证次数: {remainingChances}/{totalChances}
                  </span>
                </div>
              </div>
              {/* 上限提示 */}
              <AnimatePresence>
                {limitReached && (
                  <motion.div
                    initial={{opacity: 0, y: -10}}
                    animate={{opacity: 1, y: 0}}
                    exit={{opacity: 0, y: -10}}
                    className="text-center text-red-500 text-sm font-medium py-2 bg-red-50 dark:bg-red-900/20 rounded-lg"
                  >
                    已达到搜证上限，无法继续搜证
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          )}

          {/* 主内容区 */}
          <div className="flex-1 min-h-0 flex gap-6">            {/* 场景列表 - 玻璃态侧边栏 */}
            <motion.nav variants={itemVariants} className="w-60 flex-none hidden md:flex flex-col">
              <div
                  className="bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-3 flex-1 overflow-hidden flex flex-col">
                <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs font-medium uppercase tracking-wider mb-3 px-1">
                  调查区域
                </p>
                <div className="flex-1 overflow-y-auto scrollbar-thin space-y-2 pr-1">
                  {scenes.map((scene) => (
                      <SceneCard
                          key={scene.id}
                          scene={scene}
                          isSelected={selectedScene === scene.id}
                          isLocked={scene.isLocked}
                          onClick={() => !scene.isLocked && setSelectedScene(scene.id)}
                          clueCount={scene.clueCount}
                      />
                  ))}
                </div>
              </div>
            </motion.nav>

            {/* 线索区域 - 玻璃态主卡片 */}
            <motion.div variants={itemVariants} className="flex-1 min-w-0 flex flex-col relative">
              {/* 卡片光晕 */}
              <div
                  className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

              <div
                  className="relative flex-1 min-h-0 bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden flex flex-col">
                {/* 顶部渐变线 */}
                <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

                <AnimatePresence mode="wait">
                  {currentScene ? (
                      <motion.div
                          key={currentScene.id}
                          initial={{opacity: 0, y: 10}}
                          animate={{opacity: 1, y: 0}}
                          exit={{opacity: 0, y: -10}}
                          transition={{duration: 0.3}}
                          className="flex-1 min-h-0 flex flex-col p-5"
                      >
                        {/* 场景标题 */}
                        <div
                            className="flex items-center justify-between mb-4 pb-4 border-b border-[#E0E5EE] dark:border-[#363D4D]">
                          <div>
                            <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2]">
                              {currentScene.name}
                            </h3>
                            <p className="text-sm text-[#8C96A5] dark:text-[#6B7788] mt-1">
                              {currentScene.description}
                            </p>
                          </div>
                          <div className="text-right">
                        <span className="text-xs text-[#8C96A5] dark:text-[#6B7788]">
                          线索
                        </span>
                            <p className="text-lg font-bold text-[#7C8CD6]">
                              {currentScene.clues.filter(c => revealedClues.has(c.id)).length}/{currentScene.clues.length}
                            </p>
                          </div>
                        </div>

                        {/* 线索卡牌网格 - 竖版扑克牌布局 */}
                        <div className="flex-1 min-h-0 overflow-y-auto scrollbar-thin pr-1">
                          <div className="grid grid-cols-3 sm:grid-cols-4 lg:grid-cols-5 gap-3 pb-2">
                            {currentScene.clues.map((clue, index) => (
                                <ClueCard
                                    key={clue.id}
                                    clue={clue}
                                    isRevealed={isObserverMode || revealedClues.has(clue.id)}
                                    isPublic={publicClues.has(clue.id)}
                                    isObserverMode={isObserverMode}
                                    onReveal={() => handleRevealClue(clue.id)}
                                    index={index}
                                />
                            ))}
                          </div>
                        </div>
                      </motion.div>
                  ) : (
                      <motion.div
                          initial={{opacity: 0}}
                          animate={{opacity: 1}}
                          className="flex-1 flex items-center justify-center"
                      >
                        <div className="text-center">
                          <div
                              className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center">
                            <Search className="w-7 h-7 text-[#7C8CD6]"/>
                          </div>
                          <p className="text-[#8C96A5] dark:text-[#6B7788] text-sm">
                            选择调查区域开始搜证
                          </p>
                        </div>
                      </motion.div>
                  )}
                </AnimatePresence>

                {/* 底部导航 */}
                <div
                    className="p-4 border-t border-[#E0E5EE] dark:border-[#363D4D] bg-white/40 dark:bg-[#222631]/40 flex items-center justify-between">
                  {/* 左侧：当前场景进度 */}
                  {currentScene && (
                      <div className="text-sm">
                    <span className="text-[#8C96A5] dark:text-[#6B7788]">
                      {isObserverMode 
                        ? `共 ${currentScene.clues.length} 条线索` 
                        : `已发现 ${currentScene.clues.filter(c => revealedClues.has(c.id)).length}/${currentScene.clues.length} 条线索`
                      }
                    </span>
                      </div>
                  )}

                  {/* 右侧：操作按钮 */}
                  <div className="flex items-center gap-3">
                    {/* 观察者模式提示 */}
                    {isObserverMode && (
                        <motion.div
                            initial={{opacity: 0, scale: 0.9}}
                            animate={{opacity: 1, scale: 1}}
                            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#7C8CD6]/10 border border-[#7C8CD6]/20"
                        >
                          <Eye className="w-4 h-4 text-[#7C8CD6]"/>
                          <span className="text-xs text-[#7C8CD6] font-medium">观察者模式</span>
                        </motion.div>
                    )}
                    
                    {/* 非观察者模式：显示调查完成状态 */}
                    {!isObserverMode && currentScene && currentScene.clues.every(c => revealedClues.has(c.id)) && (
                        <motion.span
                            initial={{opacity: 0, scale: 0.9}}
                            animate={{opacity: 1, scale: 1}}
                            className="text-xs text-[#5DD9A8] font-medium flex items-center gap-1"
                        >
                          <span className="w-1.5 h-1.5 rounded-full bg-[#5DD9A8]"/>
                          当前区域调查完成
                        </motion.span>
                    )}

                    {/* 完成按钮 */}
                    {(isObserverMode || revealedClues.size === totalClues) ? (
                        <GhostButton onClick={handleComplete}>
                      <span className="flex items-center gap-1 text-[#5DD9A8]">
                        {isObserverMode ? '完成查看' : '完成调查'}
                        <motion.span
                            animate={{x: [0, 4, 0]}}
                            transition={{duration: 1.5, repeat: Infinity}}
                        >
                          <ChevronRight className="w-4 h-4"/>
                        </motion.span>
                      </span>
                        </GhostButton>
                    ) : (
                        <GhostButton onClick={handleComplete}>
                      <span className="flex items-center gap-1">
                        结束调查
                        <motion.span
                            animate={{x: [0, 4, 0]}}
                            transition={{duration: 1.5, repeat: Infinity}}
                        >
                          <ChevronRight className="w-4 h-4"/>
                        </motion.span>
                      </span>
                        </GhostButton>
                    )}
                  </div>
                </div>
            </div>
            </motion.div>
          </div>

          {/* 公开线索确认弹窗 */}
          <AnimatePresence>
            {showPublicModal && selectedClueForPublic && (
                <motion.div
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    exit={{opacity: 0}}
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
                    onClick={cancelPublic}
                >
                  <motion.div
                      initial={{scale: 0.9, opacity: 0}}
                      animate={{scale: 1, opacity: 1}}
                      exit={{scale: 0.9, opacity: 0}}
                      transition={{type: 'spring', damping: 25, stiffness: 300}}
                      className="relative w-full max-w-md mx-4 bg-white dark:bg-[#1A1D26] rounded-2xl shadow-2xl overflow-hidden"
                      onClick={e => e.stopPropagation()}
                  >
                    {/* 顶部渐变条 */}
                    <div className="h-1 bg-gradient-to-r from-[#5DD9A8] via-[#4ECDC4] to-[#45B7AA]"/>

                    {/* 关闭按钮 */}
                    <button
                        onClick={cancelPublic}
                        className="absolute top-4 right-4 w-8 h-8 rounded-full bg-[#EEF1F6] dark:bg-[#2A2F3C] flex items-center justify-center hover:bg-[#E0E5EE] dark:hover:bg-[#363D4D] transition-colors"
                    >
                      <X className="w-4 h-4 text-[#5A6978] dark:text-[#9CA8B8]"/>
                    </button>

                    {/* 内容 */}
                    <div className="p-6">
                      {/* 图标 */}
                      <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-[#5DD9A8]/20 to-[#4ECDC4]/20 flex items-center justify-center">
                        <Globe className="w-8 h-8 text-[#5DD9A8]"/>
                      </div>

                      {/* 标题 */}
                      <h3 className="text-xl font-bold text-center text-[#2D3748] dark:text-[#E8ECF2] mb-2">
                        是否公开该线索？
                      </h3>
                      <p className="text-sm text-center text-[#8C96A5] dark:text-[#6B7788] mb-6">
                        公开后，所有玩家都能看到这条线索
                      </p>

                      {/* 线索信息卡片 */}
                      <div className="bg-[#EEF1F6] dark:bg-[#2A2F3C] rounded-xl p-4 mb-6">
                        <div className="flex items-start gap-3">
                          <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#7C8CD6]/20 to-[#A78BFA]/20 flex items-center justify-center flex-shrink-0">
                            <Eye className="w-5 h-5 text-[#7C8CD6]"/>
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 className="font-bold text-[#2D3748] dark:text-[#E8ECF2] mb-1">
                              {selectedClueForPublic.name}
                            </h4>
                            <p className="text-sm text-[#5A6978] dark:text-[#9CA8B8] line-clamp-3">
                              {selectedClueForPublic.description}
                            </p>
                            <span className="inline-block mt-2 text-[10px] px-2 py-0.5 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6] font-medium">
                              {selectedClueForPublic.type}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* 按钮组 */}
                      <div className="flex gap-3">
                        <button
                            onClick={cancelPublic}
                            className="flex-1 h-12 rounded-xl bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#5A6978] dark:text-[#9CA8B8] font-medium hover:bg-[#E0E5EE] dark:hover:bg-[#363D4D] transition-colors"
                        >
                          取消
                        </button>
                        <button
                            onClick={confirmPublicClue}
                            className="flex-1 h-12 rounded-xl bg-gradient-to-r from-[#5DD9A8] to-[#4ECDC4] text-white font-medium hover:from-[#4ECDC4] hover:to-[#45B7AA] transition-all shadow-lg shadow-[#5DD9A8]/20"
                        >
                          公开线索
                        </button>
                      </div>
                    </div>
                  </motion.div>
                </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </div>
  )
}

Investigation.displayName = 'Investigation'
Investigation.phaseType = PHASE_TYPE.INVESTIGATION

export default memo(Investigation)

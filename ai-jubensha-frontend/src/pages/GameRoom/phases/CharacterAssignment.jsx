/**
 * @fileoverview CharacterAssignment 组件 - 透明背景 + 玻璃态卡片
 * @description 角色分配阶段，支持真人玩家选择角色
 */

import {memo, useState, useCallback, useEffect, useMemo} from 'react'
import {motion, AnimatePresence} from 'framer-motion'
import {ChevronRight, Check, Users, X} from 'lucide-react'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import PhaseBackgroundDecor from '../../../components/common/PhaseBackgroundDecor'
import {selectCharacter} from '../../../services/api'
import {containerVariants, itemVariants} from '../config/animations'
import {PHASE_COLORS} from '../config/theme'

// =============================================================================
// 角色卡片 - 仅展示，点击确认按钮才选择
// =============================================================================

/**
 * 角色卡片组件
 * @param {Object} character - 角色信息对象
 * @param {boolean} isSelected - 是否被选中（仅真人模式有效）
 * @param {Function} onClick - 点击回调函数（仅真人模式有效）
 * @param {boolean} isObserverMode - 是否为观察者模式
 * @returns {JSX.Element} 角色卡片组件
 */
const CharacterCard = memo(({character, isSelected, onClick, isObserverMode}) => {
  /**
   * 处理卡片点击事件
   * @description 观察者模式下不触发任何操作
   */
  const handleClick = useCallback(() => {
    if (isObserverMode) return
    onClick?.(character)
  }, [isObserverMode, onClick, character])

  return (
      <motion.div variants={itemVariants} className="relative">
        {/* 卡片光晕 - 选中时显示（仅真人模式） */}
        {isSelected && !isObserverMode && (
            <div
                className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#5DD9A8]/40 to-[#7C8CD6]/40 blur-lg opacity-70"/>
        )}

        {/* 卡片本体 */}
        <div
            className={`relative backdrop-blur-xl rounded-xl border overflow-hidden transition-all duration-300
                ${isObserverMode 
                  ? 'cursor-default bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D]' 
                  : 'cursor-pointer'
                }
                ${isSelected && !isObserverMode
                ? 'bg-white/90 dark:bg-[#222631]/90 border-[#5DD9A8] dark:border-[#5DD9A8]'
                : !isObserverMode && 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A]'
            }`}
            onClick={handleClick}
        >
          {/* 顶部渐变线 */}
          <div className={`h-1 ${isSelected && !isObserverMode
              ? 'bg-gradient-to-r from-[#5DD9A8] via-[#7C8CD6] to-[#5DD9A8]'
              : 'bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]'
          }`}/>

          {/* 选中标记（仅真人模式） */}
          {isSelected && !isObserverMode && (
              <div className="absolute top-3 right-3 w-6 h-6 rounded-full bg-[#5DD9A8] flex items-center justify-center">
                  <Check className="w-4 h-4 text-white"/>
              </div>
          )}

          <div className="p-5">
            {/* 角色名称区 */}
            <div className="mb-4">
              <div className="flex items-baseline gap-3">
                <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                  {character.name}
                </h3>
                {isSelected && !isObserverMode && (
                    <span className="text-sm text-[#5DD9A8] font-medium">待确认</span>
                )}
              </div>
              <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-2 text-sm leading-relaxed line-clamp-2">
                {character.description}
              </p>
            </div>

            {/* 装饰线 */}
            <div className="w-12 h-0.5 bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] rounded-full mb-4"/>

            {/* 信息区块 */}
            <div className="space-y-3">
              {/* 背景故事 */}
              <div className="relative pl-4 border-l-2 border-[#7C8CD6]/50">
              <span className="text-xs font-semibold text-[#7C8CD6] uppercase tracking-wider">
                背景
              </span>
                <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-1 text-xs leading-relaxed line-clamp-2">
                  {character.backgroundStory || character.background || '暂无背景故事'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </motion.div>
  )
})

CharacterCard.displayName = 'CharacterCard'

// =============================================================================
// 观察者模式提示弹框
// =============================================================================

const ObserverModeDialog = memo(({isOpen, onClose}) => {
  return (
      <AnimatePresence>
        {isOpen && (
            <motion.div
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                exit={{opacity: 0}}
                className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
                onClick={onClose}
            >
              <motion.div
                  initial={{scale: 0.9, opacity: 0}}
                  animate={{scale: 1, opacity: 1}}
                  exit={{scale: 0.9, opacity: 0}}
                  className="relative bg-white dark:bg-[#1a1d24] rounded-2xl p-6 max-w-md mx-4 shadow-2xl border border-[#E0E5EE] dark:border-[#363D4D]"
                  onClick={(e) => e.stopPropagation()}
              >
                {/* 关闭按钮 */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 w-8 h-8 rounded-full bg-[#E0E5EE] dark:bg-[#363D4D] flex items-center justify-center hover:bg-[#D0D5DE] dark:hover:bg-[#464D5D] transition-colors"
                >
                  <X className="w-4 h-4 text-[#5A6978] dark:text-[#9CA8B8]"/>
                </button>

                {/* 内容 */}
                <div className="text-center">
                  <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-[#A78BFA]/20 flex items-center justify-center">
                    <Users className="w-8 h-8 text-[#A78BFA]"/>
                  </div>
                  <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2] mb-2">
                    观察者模式
                  </h3>
                  <p className="text-[#5A6978] dark:text-[#9CA8B8] mb-6">
                    您正在以观察者身份浏览角色信息。
                    <br/>
                    所有角色将由 AI 玩家扮演。
                  </p>
                  <GhostButton onClick={onClose} className="w-full">
                    我知道了，继续浏览
                  </GhostButton>
                </div>
              </motion.div>
            </motion.div>
        )}
      </AnimatePresence>
  )
})

ObserverModeDialog.displayName = 'ObserverModeDialog'

// =============================================================================
// 主要组件
// =============================================================================

function CharacterAssignment({_config, gameData, _playerData, onComplete, onAction, isObserverMode}) {
  const [selectedCharacterId, setSelectedCharacterId] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [showObserverDialog, setShowObserverDialog] = useState(false)
  const [isWaitingForPhaseChange, setIsWaitingForPhaseChange] = useState(false)

  const characters = gameData?.characters || []
  
  // 检查是否已经有真人玩家选择了角色
  const hasRealPlayer = useMemo(() => {
    const players = _playerData?.data || _playerData || []
    const realPlayer = players.find(p => p.playerRole === 'REAL')
    return !!realPlayer
  }, [_playerData])

  // 如果已经有真人玩家，直接跳过角色选择
  useEffect(() => {
    if (hasRealPlayer && !isObserverMode) {
      console.log('[CharacterAssignment] 已检测到真人玩家，跳过角色选择')
      setIsWaitingForPhaseChange(true)
    }
  }, [hasRealPlayer, isObserverMode])

  // 观察者模式：首次进入时显示提示弹框
  useEffect(() => {
    if (isObserverMode) {
      setShowObserverDialog(true)
    }
  }, [isObserverMode])

  // 处理角色卡片点击（仅用于预选，不是最终选择）
  const handleCardClick = useCallback((character) => {
    if (isObserverMode) return
    setSelectedCharacterId(prev => prev === character.id ? null : character.id)
    console.log('[CharacterAssignment] 预选角色:', character.name, character.id)
  }, [isObserverMode])

  // 处理确认选择按钮点击
  const handleConfirmSelection = useCallback(async () => {
    console.log('[CharacterAssignment] handleConfirmSelection 被调用')
    console.log('[CharacterAssignment] isObserverMode:', isObserverMode, 'selectedCharacterId:', selectedCharacterId)
    
    // 观察者模式：直接进入下一阶段
    if (isObserverMode) {
      console.log('[CharacterAssignment] 观察者模式，调用 onAction 和 onComplete')
      onAction?.('character_assignment_complete', {})
      onComplete?.()
      return
    }

    // 真人模式：需要选择角色
    if (!selectedCharacterId) {
      setError('请先点击一个角色进行预选，然后点击确认选择')
      return
    }

    const gameId = gameData?.data?.id || gameData?.id
    if (!gameId) {
      setError('游戏ID不存在')
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      console.log('[CharacterAssignment] 确认选择角色，游戏ID:', gameId, '角色ID:', selectedCharacterId)
      
      const response = await selectCharacter(gameId, {
        characterId: selectedCharacterId,
        nickname: '玩家'
      })

      console.log('[CharacterAssignment] 角色选择响应:', response)

      if (response?.success) {
        onAction?.('character_assignment_complete', {
          characterId: selectedCharacterId,
          playerId: response.playerId,
          playerNickname: response.playerNickname,
        })
        // 真人模式：不直接调用 onComplete，等待后端广播 PHASE_CHANGE 消息
        // 后端工作流会继续执行，广播阶段变化
        console.log('[CharacterAssignment] 角色选择成功，等待后端广播阶段变化...')
        // 显示等待提示
        setError(null)
        setIsSubmitting(false)
        setIsWaitingForPhaseChange(true)
        // 设置等待状态，等待 PHASE_CHANGE 消息
        return
      } else {
        setError(response?.message || '角色选择失败')
      }
    } catch (err) {
      console.error('[CharacterAssignment] 角色选择失败:', err)
      setError(err.message || '角色选择失败，请重试')
    } finally {
      setIsSubmitting(false)
    }
  }, [isObserverMode, selectedCharacterId, gameData, onAction, onComplete])

  return (
      <div className="h-full relative overflow-hidden">
        {/* 背景装饰 - 透明背景 */}
        <div className="absolute inset-0 pointer-events-none">
          <PhaseBackgroundDecor/>
        </div>

        {/* 观察者模式提示弹框 */}
        <ObserverModeDialog 
            isOpen={showObserverDialog} 
            onClose={() => setShowObserverDialog(false)}
        />

        {/* 主内容区域 */}
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
                角色分配
              </h2>
              {isObserverMode && (
                  <span className="px-3 py-1 rounded-full bg-[#A78BFA]/20 text-[#A78BFA] text-sm font-medium">
                    观察者模式
                  </span>
              )}
            </div>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              {isObserverMode 
                  ? '查看所有角色信息，点击确认进入下一阶段'
                  : '点击角色卡片预选，然后点击"确认选择"按钮完成选择'
              }
            </p>
          </motion.div>

          {/* 角色网格 */}
          <div className="flex-1 overflow-y-auto scrollbar-thin pr-2">
            <div className="grid grid-cols-3 gap-4">
              {characters.map((character) => (
                  <CharacterCard
                      key={character.id}
                      character={character}
                      isSelected={selectedCharacterId === character.id}
                      onClick={handleCardClick}
                      isObserverMode={isObserverMode}
                  />
              ))}
            </div>
          </div>

          {/* 错误提示 */}
          {error && (
              <motion.div variants={itemVariants} className="mt-4">
                <div className="px-4 py-2 rounded-lg bg-red-500/20 text-red-400 text-sm">
                  {error}
                </div>
              </motion.div>
          )}

          {/* 等待阶段变化提示 */}
          {isWaitingForPhaseChange && (
              <motion.div variants={itemVariants} className="mt-4">
                <div className="px-4 py-3 rounded-lg bg-[#5DD9A8]/20 text-[#5DD9A8] text-sm flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-[#5DD9A8] border-t-transparent rounded-full animate-spin"/>
                  <span>{hasRealPlayer ? '已选择角色，正在进入剧本阅读阶段...' : '角色选择成功，正在进入剧本阅读阶段...'}</span>
                </div>
              </motion.div>
          )}

          {/* 确认按钮 */}
          <motion.div
              variants={itemVariants}
              className="mt-4 flex items-center justify-between"
          >
            <div className="flex items-center gap-2 text-[#8C96A5] dark:text-[#6B7788] text-sm">
              <Users className="w-4 h-4"/>
              <span>共 {characters.length} 个角色</span>
              {!isObserverMode && selectedCharacterId && (
                  <span className="text-[#5DD9A8]">· 已预选 1 个，点击确认完成选择</span>
              )}
            </div>
            
            <GhostButton
                onClick={handleConfirmSelection}
                disabled={isSubmitting || isWaitingForPhaseChange || (!isObserverMode && !selectedCharacterId)}
                className={`flex items-center gap-2 ${
                    (isObserverMode || selectedCharacterId) && !isWaitingForPhaseChange
                        ? ''
                        : 'opacity-50 cursor-not-allowed'
                }`}
            >
              <span>{isSubmitting || isWaitingForPhaseChange ? '处理中...' : (isObserverMode ? '确认并继续' : '确认选择')}</span>
              {!isSubmitting && !isWaitingForPhaseChange && (
                  <motion.span
                      animate={{x: [0, 4, 0]}}
                      transition={{duration: 1.5, repeat: Infinity}}
                  >
                    <ChevronRight className="w-4 h-4"/>
                  </motion.span>
              )}
            </GhostButton>
          </motion.div>
        </motion.div>
      </div>
  )
}

CharacterAssignment.displayName = 'CharacterAssignment'
CharacterAssignment.phaseType = PHASE_TYPE.CHARACTER_ASSIGNMENT

export default memo(CharacterAssignment)

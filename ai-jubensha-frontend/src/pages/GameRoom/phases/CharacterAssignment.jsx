/**
 * @fileoverview CharacterAssignment 组件 - 透明背景 + 玻璃态卡片
 * @description 角色分配阶段，与 ScriptOverview 风格保持一致
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {ChevronRight} from 'lucide-react'
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
      staggerChildren: 0.08,
      delayChildren: 0.1,
    },
  },
}

const itemVariants = {
  hidden: {opacity: 0, y: 20},
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

// =============================================================================
// 背景装饰 - 与 ScriptOverview 风格一致
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
// 玩家角色卡片 - 玻璃态设计
// =============================================================================

const PlayerCharacterCard = memo(({character}) => {
  return (
      <motion.div variants={itemVariants} className="relative">
        {/* 卡片光晕 */}
        <div
            className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

        {/* 卡片本体 */}
        <div
            className="relative bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden">
          {/* 顶部渐变线 */}
          <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

          <div className="p-6">
            {/* 角色名称区 */}
            <div className="mb-5">
              <div className="flex items-baseline gap-3">
                <h3 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                  {character.name}
                </h3>
                <span className="text-sm text-[#8C96A5] dark:text-[#6B7788] font-medium">你的角色</span>
              </div>
              <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-2 text-sm leading-relaxed">
                {character.description}
              </p>
            </div>

            {/* 装饰线 */}
            <div className="w-12 h-0.5 bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] rounded-full mb-5"/>

            {/* 信息区块 */}
            <div className="space-y-4">
              {/* 背景故事 */}
              <div className="relative pl-4 border-l-2 border-[#7C8CD6]/50">
              <span className="text-xs font-semibold text-[#7C8CD6] uppercase tracking-wider">
                背景故事
              </span>
                <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-1 text-sm leading-relaxed">
                  {character.background}
                </p>
              </div>

              {/* 任务目标 */}
              <div className="relative pl-4 border-l-2 border-[#5DD9A8]/50">
              <span className="text-xs font-semibold text-[#5DD9A8] uppercase tracking-wider">
                任务目标
              </span>
                <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-1 text-sm leading-relaxed">
                  {character.goal}
                </p>
              </div>

              {/* 秘密情报 */}
              <div className="relative pl-4 border-l-2 border-[#F5A9C9]/50">
              <span className="text-xs font-semibold text-[#F5A9C9] uppercase tracking-wider">
                秘密情报
              </span>
                <p className="text-[#5A6978] dark:text-[#9CA8B8] mt-1 text-sm leading-relaxed">
                  {character.secret}
                </p>
              </div>
            </div>
          </div>
        </div>
      </motion.div>
  )
})

PlayerCharacterCard.displayName = 'PlayerCharacterCard'

// =============================================================================
// 其他角色卡片 - 玻璃态设计
// =============================================================================

const OtherCharacterCard = memo(({character, index}) => {
  return (
      <motion.div variants={itemVariants}>
        <div className="relative group">
          {/* 悬停高光 */}
          <div
              className="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[#7C8CD6]/5 to-transparent"/>

          <div
              className="relative bg-white/60 dark:bg-[#222631]/60 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D] rounded-xl p-4 hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A] transition-colors">
            {/* 序号 */}
            <span
                className="inline-flex items-center justify-center w-6 h-6 rounded-lg bg-[#7C8CD6]/10 text-[#7C8CD6] text-xs font-bold mb-3">
            {index + 2}
          </span>

            {/* 角色名 */}
            <h4 className="text-base font-semibold text-[#2D3748] dark:text-[#E8ECF2] mb-1">
              {character.name}
            </h4>

            {/* 描述 */}
            <p className="text-sm text-[#8C96A5] dark:text-[#6B7788] leading-relaxed line-clamp-3">
              {character.description}
            </p>
          </div>
        </div>
      </motion.div>
  )
})

OtherCharacterCard.displayName = 'OtherCharacterCard'

// =============================================================================
// 主要组件
// =============================================================================

function CharacterAssignment({_config, gameData, _playerData, onComplete, onAction}) {
  const characters = gameData?.characters || [
    {
      id: 'char-001',
      name: '林侦探',
      description: '经验丰富的退休警探，敏锐的观察力让你成为这场调查的自然领导者。',
      background: '十年警队生涯，侦破无数案件。但这一桩不同——受害者是你曾经的熟人。',
      goal: '找出真凶，揭开真相。但要小心保守你与受害者的过往联系。',
      secret: '五年前你曾调查过受害者的欺诈案。案件撤销了，但你从未忘记。',
      isPlayer: true,
    },
    {
      id: 'char-002',
      name: '苏医生',
      description: '庄园的私人医生，说话轻声细语，总是在观察着一切。',
      isPlayer: false,
    },
    {
      id: 'char-003',
      name: '陈管家',
      description: '十五年如一日的服务，庄园里没有秘密能逃过你的眼睛。',
      isPlayer: false,
    },
    {
      id: 'char-004',
      name: '赵律师',
      description: '受害者的法律顾问，你知道遗嘱的内容。',
      isPlayer: false,
    },
    {
      id: 'char-005',
      name: '李女仆',
      description: '新来的女仆，但观察力惊人，你看到了别人忽略的细节。',
      isPlayer: false,
    },
    {
      id: 'char-006',
      name: '王司机',
      description: '载着受害者四处奔波的司机，你熟悉他们的日常路线。',
      isPlayer: false,
    },
  ]

  const playerCharacter = characters.find(c => c.isPlayer)
  const otherCharacters = characters.filter(c => !c.isPlayer)

  const handleContinue = () => {
    onAction?.('character_assignment_complete', {
      characterId: playerCharacter?.id,
    })
    onComplete?.()
  }

  return (
      <div className="h-full relative overflow-hidden">
        {/* 背景装饰 - 透明背景 */}
        <div className="absolute inset-0 pointer-events-none">
          <BackgroundDecor/>
        </div>

        {/* 主内容区域 */}
        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-8 relative z-10"
        >
          {/* 标题区 - 左对齐 */}
          <motion.div variants={itemVariants} className="mb-6">
            <h2 className="text-2xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
              角色分配
            </h2>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              熟悉你的身份背景，准备开始探案之旅
            </p>
          </motion.div>

          {/* 内容网格 */}
          <div className="flex-1 grid grid-cols-12 gap-6 min-h-0">
            {/* 左侧：玩家角色 */}
            <div className="col-span-5 h-full overflow-y-auto scrollbar-thin pr-2">
              {playerCharacter && (
                  <PlayerCharacterCard character={playerCharacter}/>
              )}
            </div>

            {/* 右侧：其他玩家 */}
            <div className="col-span-7 h-full flex flex-col">
              <motion.div variants={itemVariants} className="mb-3">
              <span className="text-[#8C96A5] dark:text-[#6B7788] text-sm font-medium">
                其他玩家 · {otherCharacters.length}人
              </span>
              </motion.div>

              <div className="flex-1 overflow-y-auto scrollbar-thin pr-2">
                <div className="grid grid-cols-2 gap-3">
                  {otherCharacters.map((character, index) => (
                      <OtherCharacterCard
                          key={character.id}
                          character={character}
                          index={index}
                      />
                  ))}
                </div>
            </div>

              {/* 确认按钮 */}
              <motion.div
                  variants={itemVariants}
                  className="mt-4 flex justify-end"
              >
                <GhostButton
                    onClick={handleContinue}
                    className="flex items-center gap-2"
                >
                  <span>确认并开始游戏</span>
                  <motion.span
                      animate={{x: [0, 4, 0]}}
                      transition={{duration: 1.5, repeat: Infinity}}
                  >
                    <ChevronRight className="w-4 h-4"/>
                  </motion.span>
                </GhostButton>
              </motion.div>
            </div>
          </div>
        </motion.div>
      </div>
  )
}

CharacterAssignment.displayName = 'CharacterAssignment'
CharacterAssignment.phaseType = PHASE_TYPE.CHARACTER_ASSIGNMENT

export default memo(CharacterAssignment)
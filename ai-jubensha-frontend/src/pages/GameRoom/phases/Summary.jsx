/**
 * @fileoverview Summary 组件 - 游戏结算界面
 * @description 真相揭晓阶段，采用玻璃态卡片设计，与 CharacterAssignment 风格保持一致
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {Crown, Home, RotateCcw, Sparkles, Target, Trophy, Users, XCircle} from 'lucide-react'
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

const scaleInVariants = {
  hidden: {opacity: 0, scale: 0.8},
  visible: {
    opacity: 1,
    scale: 1,
    transition: {
      duration: 0.6,
      ease: [0.25, 0.1, 0.25, 1],
    },
  },
}

// =============================================================================
// 背景装饰 - 与 CharacterAssignment 风格一致
// =============================================================================

const BackgroundDecor = memo(() => (
    <>
      {/* 左上角光晕 */}
      <motion.div
          className="absolute top-0 left-0 w-72 h-72 rounded-full opacity-30 blur-3xl"
          style={{
            background: 'radial-gradient(circle, rgba(124, 140, 214, 0.4) 0%, transparent 70%)',
          }}
          animate={{
            scale: [1, 1.1, 1],
            opacity: [0.3, 0.4, 0.3],
          }}
          transition={{duration: 4, repeat: Infinity}}
      />
      {/* 右下角光晕 */}
      <motion.div
          className="absolute bottom-0 right-0 w-96 h-96 rounded-full opacity-20 blur-3xl"
          style={{
            background: 'radial-gradient(circle, rgba(245, 169, 201, 0.4) 0%, transparent 70%)',
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
// 胜负徽章 - 玻璃态设计
// =============================================================================

const ResultBadge = memo(({isWin}) => (
    <motion.div
        variants={scaleInVariants}
        className="relative"
    >
      {/* 徽章光晕 */}
      <div className={`
      absolute -inset-2 rounded-3xl blur-xl opacity-60
      ${isWin ? 'bg-[#5DD9A8]/40' : 'bg-[#F5A9C9]/40'}
    `}/>

      {/* 徽章本体 */}
      <div className={`
      relative w-24 h-24 rounded-2xl flex items-center justify-center
      bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl
      border border-[#E0E5EE] dark:border-[#363D4D]
      shadow-lg
    `}>
        {/* 内部渐变背景 */}
        <div className={`
        absolute inset-2 rounded-xl
        ${isWin
            ? 'bg-gradient-to-br from-[#5DD9A8]/20 to-[#10b981]/20'
            : 'bg-gradient-to-br from-[#F5A9C9]/20 to-[#E879A9]/20'
        }
      `}/>

        {/* 图标 */}
        <div className="relative">
          {isWin ? (
              <Trophy className="w-12 h-12 text-[#5DD9A8]"/>
          ) : (
              <XCircle className="w-12 h-12 text-[#F5A9C9]"/>
          )}
        </div>
      </div>
    </motion.div>
))

ResultBadge.displayName = 'ResultBadge'

// =============================================================================
// 统计项 - 玻璃态卡片
// =============================================================================

const StatItem = memo(({label, value, icon: Icon, color = 'blue'}) => {
  const colors = {
    blue: 'text-[#7C8CD6] bg-[#7C8CD6]/10 border-[#7C8CD6]/30',
    green: 'text-[#5DD9A8] bg-[#5DD9A8]/10 border-[#5DD9A8]/30',
    pink: 'text-[#F5A9C9] bg-[#F5A9C9]/10 border-[#F5A9C9]/30',
  }

  return (
      <div className="relative group">
        {/* 悬停高光 */}
        <div
            className="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[#7C8CD6]/5 to-transparent"/>

        {/* 卡片本体 */}
        <div
            className="relative flex items-center gap-3 p-3 rounded-xl bg-white/60 dark:bg-[#222631]/60 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A] transition-colors">
          <div className={`w-10 h-10 rounded-lg flex items-center justify-center border ${colors[color]}`}>
            <Icon className="w-5 h-5"/>
          </div>
          <div>
            <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider">{label}</p>
            <p className="text-sm font-semibold text-[#2D3748] dark:text-[#E8ECF2]">{value}</p>
          </div>
        </div>
      </div>
  )
})

StatItem.displayName = 'StatItem'

// =============================================================================
// 玩家排名项 - 玻璃态设计
// =============================================================================

const PlayerRankItem = memo(({player, rank}) => {
  const isTop3 = rank <= 3

  return (
      <motion.div variants={itemVariants} className="relative group">
        {/* 悬停高光 */}
        <div
            className="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[#7C8CD6]/5 to-transparent"/>

        {/* 卡片本体 */}
        <div className={`
        relative flex items-center gap-3 p-3 rounded-xl border transition-all backdrop-blur-sm
        ${player.isCulprit
            ? 'bg-[#F5A9C9]/10 border-[#F5A9C9]/30'
            : player.voteCorrect
                ? 'bg-[#5DD9A8]/10 border-[#5DD9A8]/30'
                : 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A]'
        }
      `}>
          {/* 排名 */}
          <div className={`
          w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm
          ${isTop3
              ? 'bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] text-white'
              : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#8C96A5]'
          }
        `}>
            {rank}
          </div>

          {/* 头像 */}
          <div
              className={`
            w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm
            ${player.isCulprit
                  ? 'bg-[#F5A9C9] text-white'
                  : player.voteCorrect
                      ? 'bg-[#5DD9A8] text-white'
                      : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#8C96A5]'
              }
          `}
          >
            {player.name.charAt(0)}
          </div>

          {/* 信息 */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-[#2D3748] dark:text-[#E8ECF2] truncate">
              {player.name}
            </span>
              {player.isSelf && (
                  <span
                      className="text-[10px] px-1.5 py-0.5 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6] border border-[#7C8CD6]/30">
                我
              </span>
              )}
            </div>
            <span className="text-[10px] text-[#8C96A5] dark:text-[#6B7788]">{player.role}</span>
          </div>

          {/* 标签 */}
          {player.isCulprit && (
              <span className="text-[10px] px-2 py-1 rounded-full bg-[#F5A9C9] text-white font-medium">
            凶手
          </span>
          )}
          {player.voteCorrect && (
              <span className="text-[10px] px-2 py-1 rounded-full bg-[#5DD9A8] text-white font-medium">
            正确
          </span>
          )}
        </div>
      </motion.div>
  )
})

PlayerRankItem.displayName = 'PlayerRankItem'

// =============================================================================
// 真相卡片 - 玻璃态设计
// =============================================================================

const TruthCard = memo(({truth}) => (
    <motion.div variants={itemVariants} className="relative">
      {/* 卡片光晕 */}
      <div
          className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

      {/* 卡片本体 */}
      <div
          className="relative bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden">
        {/* 顶部渐变线 */}
        <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

        <div className="p-5">
          {/* 标题 */}
          <div className="flex items-center gap-2 mb-4">
            <div className="w-8 h-8 rounded-lg bg-[#7C8CD6]/10 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-[#7C8CD6]"/>
            </div>
            <h3 className="text-sm font-semibold text-[#2D3748] dark:text-[#E8ECF2]">
              真相大白
            </h3>
          </div>

          {/* 真相内容 */}
          <div className="relative pl-4 border-l-2 border-[#7C8CD6]/50">
            <p className="text-sm text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed whitespace-pre-line">
              {truth}
            </p>
          </div>
        </div>
      </div>
    </motion.div>
))

TruthCard.displayName = 'TruthCard'

// =============================================================================
// 主要组件
// =============================================================================

function Summary({_config, gameData, onAction}) {
  const result = gameData?.result || {
    isWin: true,
    correctVote: true,
    culprit: {id: 'p2', name: '苏医生', role: '私人医生'},
    truth: `这是一场精心策划的谋杀。苏医生几个月来一直在缓慢地给受害者下毒，同时自己对毒药产生了耐受性。

在那个暴风雨的夜晚，当灯光熄灭时，医生用那把古董拆信刀刺向了受害者——这把武器在这个家族中已经传承了几代。

密室只是个幻觉。只有管家和苏医生知道的秘密通道提供了完美的逃生路线。被破坏的窗户只是个幌子，意在暗示有外来入侵者。

但医生犯了一个错误：厨房垃圾桶里的毒药瓶。一个不小心的时刻，摧毁了这场完美犯罪。`,
    players: [
      {id: 'p1', name: '林侦探', role: '调查员', isSelf: true, voteCorrect: true},
      {id: 'p2', name: '苏医生', role: '私人医生', isCulprit: true},
      {id: 'p3', name: '陈管家', role: '管家'},
      {id: 'p4', name: '赵律师', role: '法律顾问'},
    ],
  }

  const selfPlayer = result.players.find(p => p.isSelf)
  const correctCount = result.players.filter(p => p.voteCorrect).length

  const handleReturnHome = () => {
    onAction?.('return_home')
  }

  const handlePlayAgain = () => {
    onAction?.('play_again')
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
              真相揭晓
            </h2>
            <p className="text-[#8C96A5] dark:text-[#6B7788] mt-1 text-sm">
              案件已尘埃落定，回顾这场推理之旅
            </p>
          </motion.div>

          {/* 内容网格 */}
          <div className="flex-1 grid grid-cols-12 gap-6 min-h-0">
            {/* 左侧：结果展示 + 统计 */}
            <div className="col-span-5 h-full overflow-y-auto scrollbar-thin pr-2">
              <div className="space-y-4">
                {/* 胜负结果卡片 */}
                <motion.div variants={itemVariants} className="relative">
                  {/* 卡片光晕 */}
                  <div
                      className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

                  {/* 卡片本体 */}
                  <div
                      className="relative bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden">
                    {/* 顶部渐变线 */}
                    <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

                    <div className="p-6 text-center">
                      {/* 徽章 */}
                      <div className="flex justify-center mb-4">
                        <ResultBadge isWin={result.isWin}/>
                      </div>

                      {/* 结果文字 */}
                      <h3 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2] mb-2">
                        {result.isWin ? '胜利！' : '失败'}
                      </h3>
                      <p className="text-sm text-[#8C96A5] dark:text-[#6B7788]">
                        {result.isWin
                            ? '你成功指认了凶手，正义得到伸张'
                            : '凶手逃脱了，真相仍隐藏在迷雾中'}
                    </p>
                    </div>
                  </div>
                </motion.div>

                {/* 统计信息 */}
                <div className="space-y-3">
                  <h3 className="text-xs font-medium text-[#8C96A5] uppercase tracking-wider">
                    游戏统计
                  </h3>
                  <StatItem
                      label="凶手"
                      value={result.culprit.name}
                      icon={Target}
                      color="pink"
                  />
                  <StatItem
                      label="正确投票"
                      value={`${correctCount}/${result.players.length} 人`}
                      icon={Users}
                      color="green"
                  />
                  <StatItem
                      label="你的表现"
                      value={selfPlayer?.voteCorrect ? '推理正确' : '推理错误'}
                      icon={Crown}
                      color={selfPlayer?.voteCorrect ? 'green' : 'blue'}
                  />
                </div>
              </div>
            </div>

            {/* 右侧：真相 + 玩家排名 */}
            <div className="col-span-7 h-full flex flex-col">
              {/* 真相卡片 */}
              <div className="mb-4">
                <TruthCard truth={result.truth}/>
              </div>

              {/* 玩家排名 */}
              <motion.div variants={itemVariants} className="flex-1 flex flex-col min-h-0">
                <h3 className="text-xs font-medium text-[#8C96A5] uppercase tracking-wider mb-3">
                  玩家表现
                </h3>
                <div className="flex-1 overflow-y-auto scrollbar-thin pr-2">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {result.players.map((player, index) => (
                        <PlayerRankItem
                            key={player.id}
                            player={player}
                            rank={index + 1}
                        />
                    ))}
                  </div>
                </div>
              </motion.div>

              {/* 底部按钮 */}
              <motion.div
                  variants={itemVariants}
                  className="mt-4 flex justify-end gap-3 pt-4 border-t border-[#E0E5EE] dark:border-[#363D4D]"
              >
                <GhostButton onClick={handleReturnHome}>
                <span className="flex items-center gap-2">
                  <Home className="w-4 h-4"/>
                  返回大厅
                </span>
                </GhostButton>
                <GhostButton onClick={handlePlayAgain}>
                <span className="flex items-center gap-2">
                  <RotateCcw className="w-4 h-4"/>
                  再来一局
                </span>
                </GhostButton>
              </motion.div>
            </div>
          </div>
        </motion.div>
      </div>
  )
}

Summary.displayName = 'Summary'
Summary.phaseType = PHASE_TYPE.SUMMARY

export default memo(Summary)

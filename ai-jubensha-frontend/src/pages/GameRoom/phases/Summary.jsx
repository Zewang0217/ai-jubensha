/**
 * @fileoverview Summary 组件 - 游戏结算界面
 * @description 真相揭晓阶段，采用玻璃态卡片设计，与 CharacterAssignment 风格保持一致
 */

import {memo, useMemo} from 'react'
import {motion} from 'framer-motion'
import {Crown, Home, RotateCcw, Sparkles, Star, Target, Trophy, User, XCircle} from 'lucide-react'
import {PHASE_TYPE} from '../types'
import GhostButton from '../../../components/ui/GhostButton'
import PhaseBackgroundDecor from '../../../components/common/PhaseBackgroundDecor'
import {containerVariants, itemVariants, scaleInVariants} from '../config/animations'

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
      relative w-20 h-20 rounded-2xl flex items-center justify-center
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
              <Trophy className="w-10 h-10 text-[#5DD9A8]"/>
          ) : (
              <XCircle className="w-10 h-10 text-[#F5A9C9]"/>
          )}
        </div>
      </div>
    </motion.div>
))

ResultBadge.displayName = 'ResultBadge'

// =============================================================================
// 玩家评分卡片 - 显示答案+得分+评论
// =============================================================================

const PlayerScoreCard = memo(({player, rank, currentPlayerId, culpritId}) => {
  const isSelf = String(player.playerId) === String(currentPlayerId)
  const isCulprit = String(player.playerId) === String(culpritId)
  const isTop3 = rank <= 3

  return (
      <motion.div variants={itemVariants} className="relative group">
        {/* 悬停高光 */}
        <div
            className="absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[#7C8CD6]/5 to-transparent"/>

        {/* 卡片本体 */}
        <div className={`
        relative flex flex-col p-4 rounded-xl border transition-all backdrop-blur-sm
        ${isCulprit
            ? 'bg-[#F5A9C9]/10 border-[#F5A9C9]/30'
            : isSelf
                ? 'bg-[#5DD9A8]/10 border-[#5DD9A8]/30'
                : 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6] dark:hover:border-[#5E6B8A]'
        }
      `}>
          {/* 第一行：排名、头像、名称、标签 */}
          <div className="flex items-center gap-3 mb-3">
            {/* 排名 */}
            <div className={`
            w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs
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
              w-9 h-9 rounded-lg flex items-center justify-center font-bold text-sm
              ${isCulprit
                  ? 'bg-[#F5A9C9] text-white'
                  : isSelf
                      ? 'bg-[#5DD9A8] text-white'
                      : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#8C96A5]'
              }
          `}
            >
              {player.name?.charAt(0) || '?'}
            </div>

            {/* 信息 */}
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-[#2D3748] dark:text-[#E8ECF2] truncate">
                  {player.name || '未知玩家'}
                </span>
                {isSelf && (
                    <span
                        className="text-[10px] px-1.5 py-0.5 rounded-full bg-[#7C8CD6]/10 text-[#7C8CD6] border border-[#7C8CD6]/30">
                  我
                </span>
                )}
              </div>
              <span className="text-[10px] text-[#8C96A5] dark:text-[#6B7788]">{player.role || '角色'}</span>
            </div>

            {/* 得分 */}
            <div className="flex items-center gap-1.5">
              <Star className="w-4 h-4 text-[#F5A9C9]"/>
              <span className="text-lg font-bold text-[#2D3748] dark:text-[#E8ECF2]">{player.score || 0}</span>
              <span className="text-xs text-[#8C96A5]">分</span>
            </div>

            {/* 标签 */}
            {isCulprit && (
                <span className="text-[10px] px-2 py-1 rounded-full bg-[#F5A9C9] text-white font-medium">
              凶手
            </span>
            )}
          </div>

          {/* 第二行：答案 */}
          {player.answer && (
              <div className="mb-2 p-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40 border border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
                <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] mb-1">投票答案</p>
                <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed line-clamp-2">
                  {player.answer}
                </p>
              </div>
          )}

          {/* 第三行：评论 */}
          {player.comment && (
              <div className="flex items-start gap-2">
                <div className="w-5 h-5 rounded-md bg-[#7C8CD6]/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Sparkles className="w-3 h-3 text-[#7C8CD6]"/>
                </div>
                <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed">
                  {player.comment}
                </p>
              </div>
          )}
        </div>
      </motion.div>
  )
})

PlayerScoreCard.displayName = 'PlayerScoreCard'

// =============================================================================
// 个人表现卡片
// =============================================================================

const PersonalStatsCard = memo(({player}) => {
  if (!player) return null

  return (
      <motion.div variants={itemVariants} className="relative">
        {/* 卡片本体 */}
        <div
            className="relative bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden">
          {/* 顶部渐变线 */}
          <div className="h-1 bg-gradient-to-r from-[#5DD9A8] via-[#7C8CD6] to-[#A78BFA]"/>

          <div className="p-4">
            {/* 标题 */}
            <div className="flex items-center gap-2 mb-4">
              <div className="w-7 h-7 rounded-lg bg-[#5DD9A8]/10 flex items-center justify-center">
                <User className="w-4 h-4 text-[#5DD9A8]"/>
              </div>
              <h3 className="text-sm font-semibold text-[#2D3748] dark:text-[#E8ECF2]">
                你的表现
              </h3>
            </div>

            {/* 得分 */}
            <div className="flex items-center justify-center mb-4">
              <div className="text-center">
                <div className="flex items-center justify-center gap-1 mb-1">
                  <Star className="w-6 h-6 text-[#F5A9C9]"/>
                  <span className="text-3xl font-bold text-[#2D3748] dark:text-[#E8ECF2]">{player.score || 0}</span>
                  <span className="text-sm text-[#8C96A5]">分</span>
                </div>
                <p className="text-xs text-[#8C96A5]">综合评分</p>
              </div>
            </div>

            {/* 评分细分 */}
            {player.breakdown && (
                <div className="grid grid-cols-4 gap-2 mb-4">
                  <div className="text-center p-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40">
                    <p className="text-lg font-bold text-[#7C8CD6]">{player.breakdown.motive || 0}</p>
                    <p className="text-[10px] text-[#8C96A5]">动机</p>
                  </div>
                  <div className="text-center p-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40">
                    <p className="text-lg font-bold text-[#7C8CD6]">{player.breakdown.method || 0}</p>
                    <p className="text-[10px] text-[#8C96A5]">手法</p>
                  </div>
                  <div className="text-center p-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40">
                    <p className="text-lg font-bold text-[#7C8CD6]">{player.breakdown.clues || 0}</p>
                    <p className="text-[10px] text-[#8C96A5]">线索</p>
                  </div>
                  <div className="text-center p-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40">
                    <p className="text-lg font-bold text-[#7C8CD6]">{player.breakdown.accuracy || 0}</p>
                    <p className="text-[10px] text-[#8C96A5]">准确</p>
                  </div>
                </div>
            )}

            {/* 答案 */}
            {player.answer && (
                <div className="mb-3 p-3 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40 border border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
                  <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] mb-1">你的答案</p>
                  <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed">
                    {player.answer}
                  </p>
                </div>
            )}

            {/* 评论 */}
            {player.comment && (
                <div className="flex items-start gap-2 p-3 rounded-lg bg-[#7C8CD6]/5 border border-[#7C8CD6]/20">
                  <Sparkles className="w-4 h-4 text-[#7C8CD6] flex-shrink-0 mt-0.5"/>
                  <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed">
                    {player.comment}
                  </p>
                </div>
            )}
          </div>
        </div>
      </motion.div>
  )
})

PersonalStatsCard.displayName = 'PersonalStatsCard'

// =============================================================================
// 真相卡片 - 玻璃态设计
// =============================================================================

const TruthCard = memo(({truth}) => (
    <motion.div variants={itemVariants} className="relative h-full">
      {/* 卡片光晕 */}
      <div
          className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-[#7C8CD6]/20 to-[#A78BFA]/20 blur-lg opacity-50"/>

      {/* 卡片本体 */}
      <div
          className="relative h-full bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden flex flex-col">
        {/* 顶部渐变线 */}
        <div className="h-1 bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]"/>

        <div className="flex-1 p-5 flex flex-col">
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
          <div className="flex-1 overflow-y-auto scrollbar-thin">
            <div className="relative pl-4 border-l-2 border-[#7C8CD6]/50">
              <p className="text-sm text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed whitespace-pre-line">
                {truth || '真相尚未揭晓...'}
              </p>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
))

TruthCard.displayName = 'TruthCard'

// =============================================================================
// 主要组件
// =============================================================================

function Summary({_config, gameData, currentPlayerId, onAction}) {
  // 从 gameData.result 获取真实数据
  const result = gameData?.result || {}

  // 处理玩家评分数据
  const playerScores = useMemo(() => {
    const scores = result?.scores || []
    const playerAnswers = result?.playerAnswers || {}
    const players = gameData?.players || []

    // 构建 playerId -> player 映射
    const playerMap = new Map()
    players.forEach(p => {
      playerMap.set(String(p.playerId), p)
    })

    // 合并评分数据和玩家信息
    return scores.map(score => {
      const player = playerMap.get(String(score.playerId)) || {}
      return {
        playerId: score.playerId,
        name: player.name || player.characterName || `玩家${score.playerId}`,
        role: player.characterName || '角色',
        score: score.score || 0,
        breakdown: score.breakdown || {},
        comment: score.comment || '',
        answer: playerAnswers[score.playerId] || '',
      }
    }).sort((a, b) => b.score - a.score) // 按分数降序排列
  }, [result?.scores, result?.playerAnswers, gameData?.players])

  // 获取当前玩家的评分
  const selfPlayerScore = useMemo(() => {
    return playerScores.find(p => String(p.playerId) === String(currentPlayerId))
  }, [playerScores, currentPlayerId])

  // 获取凶手ID（需要从游戏数据中获取）
  const culpritId = useMemo(() => {
    // 这里需要根据实际的游戏逻辑获取凶手ID
    // 暂时返回 null，后续可以从 gameData 中获取
    return gameData?.culpritId || null
  }, [gameData])

  // 计算平均分
  const averageScore = useMemo(() => {
    if (playerScores.length === 0) return 0
    const total = playerScores.reduce((sum, p) => sum + (p.score || 0), 0)
    return Math.round(total / playerScores.length)
  }, [playerScores])

  // 判断是否获胜（当前玩家投票正确）
  const isWin = useMemo(() => {
    // 这里需要根据实际的投票结果判断
    // 暂时根据分数判断
    return (selfPlayerScore?.score || 0) >= averageScore
  }, [selfPlayerScore, averageScore])

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
          <PhaseBackgroundDecor/>
        </div>

        {/* 主内容区域 */}
        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="h-full flex flex-col p-6 relative z-10"
        >
          {/* 标题区 */}
          <motion.div variants={itemVariants} className="mb-4 flex items-center justify-between">
            <div>
              <h2 className="text-xl font-bold text-[#2D3748] dark:text-[#E8ECF2] tracking-tight">
                真相揭晓
              </h2>
              <p className="text-[#8C96A5] dark:text-[#6B7788] text-xs">
                案件已尘埃落定，回顾这场推理之旅
              </p>
            </div>
            <div className="flex items-center gap-2">
              <GhostButton onClick={handleReturnHome} size="sm">
                <span className="flex items-center gap-1.5">
                  <Home className="w-3.5 h-3.5"/>
                  返回大厅
                </span>
              </GhostButton>
              <GhostButton onClick={handlePlayAgain} size="sm">
                <span className="flex items-center gap-1.5">
                  <RotateCcw className="w-3.5 h-3.5"/>
                  再来一局
                </span>
              </GhostButton>
            </div>
          </motion.div>

          {/* 内容网格 - 占满剩余空间 */}
          <div className="flex-1 grid grid-cols-12 gap-4 min-h-0">
            {/* 左侧：胜负结果 + 个人表现 */}
            <div className="col-span-4 h-full flex flex-col gap-4 overflow-hidden">
              {/* 胜负结果卡片 */}
              <motion.div variants={itemVariants} className="flex-shrink-0">
                <div
                    className="relative bg-white/80 dark:bg-[#222631]/80 backdrop-blur-xl rounded-xl border border-[#E0E5EE] dark:border-[#363D4D] overflow-hidden">
                  {/* 顶部渐变线 */}
                  <div className={`h-1 ${isWin ? 'bg-gradient-to-r from-[#5DD9A8] to-[#10b981]' : 'bg-gradient-to-r from-[#F5A9C9] to-[#E879A9]'}`}/>

                  <div className="p-4 text-center">
                    {/* 徽章 */}
                    <div className="flex justify-center mb-3">
                      <ResultBadge isWin={isWin}/>
                    </div>

                    {/* 结果文字 */}
                    <h3 className="text-lg font-bold text-[#2D3748] dark:text-[#E8ECF2] mb-1">
                      {isWin ? '胜利！' : '失败'}
                    </h3>
                    <p className="text-xs text-[#8C96A5] dark:text-[#6B7788]">
                      {isWin
                          ? '你的推理得到了验证'
                          : '真相仍隐藏在迷雾中'}
                    </p>
                  </div>
                </div>
              </motion.div>

              {/* 个人表现卡片 */}
              <div className="flex-1 min-h-0 overflow-hidden">
                <PersonalStatsCard player={selfPlayerScore}/>
              </div>
            </div>

            {/* 右侧：真相 + 玩家排名 */}
            <div className="col-span-8 h-full flex flex-col gap-4 overflow-hidden">
              {/* 真相卡片 */}
              <div className="h-40 flex-shrink-0">
                <TruthCard truth={result?.ending}/>
              </div>

              {/* 玩家排名 */}
              <motion.div variants={itemVariants} className="flex-1 flex flex-col min-h-0">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-xs font-medium text-[#8C96A5] uppercase tracking-wider">
                    玩家表现
                  </h3>
                  <div className="flex items-center gap-1 text-xs text-[#8C96A5]">
                    <span>平均分:</span>
                    <span className="font-bold text-[#7C8CD6]">{averageScore}</span>
                  </div>
                </div>
                <div className="flex-1 overflow-y-auto scrollbar-thin pr-1">
                  <div className="grid grid-cols-2 gap-3">
                    {playerScores.map((player, index) => (
                        <PlayerScoreCard
                            key={player.playerId}
                            player={player}
                            rank={index + 1}
                            currentPlayerId={currentPlayerId}
                            culpritId={culpritId}
                        />
                    ))}
                  </div>
                </div>
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

/**
 * @fileoverview 投票结果卡片组件
 * @description 用于观察者模式显示投票结果
 * @author zewang
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {Vote} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * 投票结果卡片组件
 * @description 显示单个玩家的投票信息，包括投票对象和投票理由
 * @param {Object} props - 组件属性
 * @param {Object} props.vote - 投票信息对象
 * @param {string} props.vote.voterName - 投票者名称
 * @param {string} props.vote.voterRole - 投票者角色
 * @param {string} props.vote.targetName - 被投票者名称
 * @param {string} props.vote.voteMessage - 投票理由
 * @param {number} props.index - 索引，用于动画延迟
 * @returns {JSX.Element} 投票结果卡片组件
 */
const VoteResultCard = memo(({vote, index}) => {
  return (
    <motion.div
      initial={{opacity: 0, y: 10}}
      animate={{opacity: 1, y: 0}}
      transition={{delay: index * 0.1}}
      className="p-4 rounded-xl bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D] hover:bg-white/80 dark:hover:bg-[#222631]/80 transition-all"
    >
      {/* 投票者信息 */}
      <div className="flex items-center gap-3 mb-3 pb-3 border-b border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
        <div 
          className="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm flex-shrink-0"
          style={{
            background: `linear-gradient(to bottom right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary})`
          }}
        >
          {vote.voterName.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <h4 className="font-medium text-sm text-[#2D3748] dark:text-[#E8ECF2]">
            {vote.voterName}
          </h4>
          <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788]">
            {vote.voterRole}
          </p>
        </div>
        <div 
          className="flex items-center gap-1.5 px-2 py-1 rounded-lg border"
          style={{
            backgroundColor: `${PHASE_COLORS.accent}10`,
            borderColor: `${PHASE_COLORS.accent}20`
          }}
        >
          <Vote className="w-3 h-3" style={{color: PHASE_COLORS.accent}}/>
          <span className="text-xs font-medium" style={{color: PHASE_COLORS.accent}}>已投票</span>
        </div>
      </div>

      {/* 投票对象 */}
      <div className="mb-3">
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider mb-1.5">
          投票对象
        </p>
        <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-[#EEF1F6]/50 dark:bg-[#2A2F3C]/50 border border-[#E0E5EE] dark:border-[#363D4D]">
          <div 
            className="w-6 h-6 rounded-md flex items-center justify-center text-white font-bold text-xs"
            style={{
              background: `linear-gradient(to bottom right, ${PHASE_COLORS.accent}, #E879A9)`
            }}
          >
            {vote.targetName.charAt(0)}
          </div>
          <span className="text-sm text-[#2D3748] dark:text-[#E8ECF2] font-medium">
            {vote.targetName}
          </span>
        </div>
      </div>

      {/* 投票理由 */}
      <div>
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] uppercase tracking-wider mb-1.5">
          投票理由
        </p>
        <p className="text-xs text-[#5A6978] dark:text-[#9CA8B8] leading-relaxed px-3 py-2 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40 border border-[#E0E5EE]/50 dark:border-[#363D4D]/50">
          {vote.voteMessage || '未提供投票理由'}
        </p>
      </div>
    </motion.div>
  )
})

VoteResultCard.displayName = 'VoteResultCard'

export default VoteResultCard

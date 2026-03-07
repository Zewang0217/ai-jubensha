/**
 * @fileoverview 投票候选人卡片组件
 * @description 用于讨论阶段投票列表的候选人展示
 * @author zewang
 */

import {memo} from 'react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * 投票候选人卡片组件
 * @param {Object} props - 组件属性
 * @param {Object} props.player - 玩家数据
 * @param {string} props.player.playerId - 玩家ID
 * @param {string} props.player.id - 玩家ID（备用字段）
 * @param {string} props.player.name - 玩家名称
 * @param {string} props.player.characterName - 角色名称
 * @param {boolean} props.isSelected - 是否被选中
 * @param {boolean} props.hasVoted - 是否已投票
 * @param {Function} props.onVote - 投票回调函数
 * @param {Function} props.onHover - 悬停回调函数
 * @returns {JSX.Element} 投票候选人卡片组件
 */
const CandidateCard = memo(({player, isSelected, hasVoted, onVote, onHover}) => {
  const playerId = player.playerId || player.id
  
  return (
    <div
      onMouseEnter={() => onHover?.(player)}
      onMouseLeave={() => onHover?.(null)}
      className={`
        w-full p-3 rounded-xl transition-all border flex items-center gap-3 cursor-pointer
        ${isSelected
          ? `bg-[${PHASE_COLORS.accent}]/10 border-[${PHASE_COLORS.accent}]/50`
          : hasVoted
            ? 'bg-white/30 dark:bg-[#222631]/30 border-[#E0E5EE]/50 dark:border-[#363D4D]/50 opacity-50'
            : 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:bg-white/80 dark:hover:bg-[#222631]/80'
        }
      `}
      style={isSelected ? {
        backgroundColor: `${PHASE_COLORS.accent}10`,
        borderColor: `${PHASE_COLORS.accent}50`,
      } : {}}
    >
      {/* 头像 */}
      <div
        className={`
          w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm flex-shrink-0
          ${isSelected
            ? `bg-gradient-to-br from-[${PHASE_COLORS.accent}] to-[#E879A9] text-white`
            : 'bg-[#EEF1F6] dark:bg-[#2A2F3C] text-[#8C96A5]'
          }
        `}
        style={isSelected ? {
          background: `linear-gradient(to bottom right, ${PHASE_COLORS.accent}, #E879A9)`
        } : {}}
      >
        {(player.name || player.characterName || '?').charAt(0)}
      </div>

      {/* 信息 */}
      <div className="flex-1 min-w-0">
        <h4 
          className={`font-medium text-sm ${isSelected ? 'text-[#2D3748] dark:text-[#E8ECF2]' : 'text-[#5A6978] dark:text-[#9CA8B8]'}`}
        >
          {player.name || player.characterName || '未知玩家'}
        </h4>
        <p className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] truncate">
          {player.characterName || player.name || '角色'}
        </p>
      </div>

      {/* 选择状态 */}
      {!hasVoted ? (
        <div
          onClick={() => onVote(playerId)}
          className={`text-xs px-2 py-1 rounded ${isSelected ? 'font-medium' : 'text-[#8C96A5]'}`}
          style={isSelected ? {color: PHASE_COLORS.accent} : {}}
        >
          {isSelected ? '已选择' : '选择'}
        </div>
      ) : (
        isSelected && (
          <div 
            className="w-5 h-5 rounded-full flex items-center justify-center"
            style={{backgroundColor: `${PHASE_COLORS.success}20`}}
          >
            <span style={{color: PHASE_COLORS.success}} className="text-xs">✓</span>
          </div>
        )
      )}
    </div>
  )
})

CandidateCard.displayName = 'CandidateCard'

export default CandidateCard

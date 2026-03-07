/**
 * @fileoverview CandidateCard 组件 - 投票候选人卡片
 * @description 展示可投票的玩家候选人，支持选择和投票操作
 * @author zewang
 */

import {memo} from 'react'
import PropTypes from 'prop-types'
import {motion} from 'framer-motion'
import {User, Vote} from 'lucide-react'

/**
 * CandidateCard 组件 - 投票候选人卡片
 *
 * @param {Object} props - 组件属性
 * @param {Object} props.player - 玩家对象
 * @param {number} props.player.id - 玩家ID
 * @param {number} props.player.playerId - 玩家 playerId
 * @param {string} props.player.name - 玩家名称
 * @param {string} props.player.avatarUrl - 玩家头像URL
 * @param {boolean} props.isSelected - 是否被选中
 * @param {boolean} props.hasVoted - 是否已投票（禁用状态）
 * @param {Function} props.onVote - 投票回调函数，参数为 playerId
 * @param {Function} props.onHover - 悬停回调函数，参数为 player 对象
 */
const CandidateCard = memo(({
  player,
  isSelected,
  hasVoted,
  onVote,
  onHover,
}) => {
  const handleClick = () => {
    if (!hasVoted && onVote) {
      onVote(player.playerId)
    }
  }

  const handleMouseEnter = () => {
    if (onHover) {
      onHover(player)
    }
  }

  const handleMouseLeave = () => {
    if (onHover) {
      onHover(null)
    }
  }

  return (
    <motion.div
      whileHover={!hasVoted ? {scale: 1.02} : {}}
      whileTap={!hasVoted ? {scale: 0.98} : {}}
      onClick={handleClick}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      className={`
        relative p-4 rounded-xl border-2 cursor-pointer transition-all duration-300
        ${isSelected
          ? 'bg-gradient-to-r from-[#7C8CD6]/20 to-[#7C8CD6]/5 border-[#7C8CD6] shadow-lg shadow-[#7C8CD6]/20'
          : 'bg-white/60 dark:bg-[#222631]/60 border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6]/50'
        }
        ${hasVoted ? 'cursor-not-allowed opacity-60' : ''}
      `}
    >
      {/* 选中标记 */}
      {isSelected && (
        <div className="absolute top-2 right-2 w-6 h-6 rounded-full bg-[#7C8CD6] flex items-center justify-center">
          <Vote className="w-3.5 h-3.5 text-white" />
        </div>
      )}

      <div className="flex items-center gap-3">
        {/* 头像 */}
        <div className={`
          w-12 h-12 rounded-full flex items-center justify-center overflow-hidden
          ${isSelected
            ? 'bg-gradient-to-br from-[#7C8CD6] to-[#5A6AB8]'
            : 'bg-gradient-to-br from-[#8C96A5] to-[#6B7280]'
          }
        `}>
          {player.avatarUrl ? (
            <img
              src={player.avatarUrl}
              alt={player.name}
              className="w-full h-full object-cover"
            />
          ) : (
            <User className="w-6 h-6 text-white/80" />
          )}
        </div>

        {/* 玩家信息 */}
        <div className="flex-1 min-w-0">
          <h4 className={`
            font-medium truncate
            ${isSelected
              ? 'text-[#7C8CD6]'
              : 'text-[#2D3748] dark:text-[#E8ECF2]'
            }
          `}>
            {player.name || '未知玩家'}
          </h4>
          <p className="text-xs text-[#8C96A5]">
            {isSelected ? '已选择' : '点击选择'}
          </p>
        </div>
      </div>
    </motion.div>
  )
})

CandidateCard.displayName = 'CandidateCard'

CandidateCard.propTypes = {
  player: PropTypes.shape({
    id: PropTypes.number,
    playerId: PropTypes.number.isRequired,
    name: PropTypes.string,
    avatarUrl: PropTypes.string,
  }).isRequired,
  isSelected: PropTypes.bool,
  hasVoted: PropTypes.bool,
  onVote: PropTypes.func,
  onHover: PropTypes.func,
}

CandidateCard.defaultProps = {
  isSelected: false,
  hasVoted: false,
  onVote: null,
  onHover: null,
}

export default CandidateCard

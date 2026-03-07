/**
 * @fileoverview VoteResultCard 组件 - 投票结果卡片
 * @description 展示单个投票结果的卡片组件
 * @author zewang
 */

import {memo} from 'react'
import PropTypes from 'prop-types'
import {motion} from 'framer-motion'
import {User, Target, FileText} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * VoteResultCard 组件 - 投票结果卡片
 *
 * @param {Object} props - 组件属性
 * @param {Object} props.vote - 投票对象
 * @param {string} props.vote.voterName - 投票者名称
 * @param {string} props.vote.targetName - 被投票者名称
 * @param {string} props.vote.reason - 投票理由
 * @param {number} props.index - 索引用于交错动画
 */
const VoteResultCard = memo(({
  vote,
  index = 0,
}) => {
  return (
    <motion.div
      initial={{opacity: 0, y: 10}}
      animate={{opacity: 1, y: 0}}
      transition={{duration: 0.3, delay: index * 0.05}}
      className="p-3 rounded-xl bg-white/60 dark:bg-[#1A1D26]/60 border border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6]/30 transition-all duration-200"
    >
      {/* 投票者和目标 */}
      <div className="flex items-center gap-2 mb-2">
        <div className="flex items-center gap-1.5">
          <div className="w-6 h-6 rounded-full bg-gradient-to-br from-[#8C96A5] to-[#6B7280] flex items-center justify-center">
            <User className="w-3 h-3 text-white/80" />
          </div>
          <span className="text-sm font-medium text-[#2D3748] dark:text-[#E8ECF2]">
            {vote.voterName || '未知玩家'}
          </span>
        </div>

        <span className="text-[#8C96A5]">→</span>

        <div className="flex items-center gap-1.5">
          <div className="w-6 h-6 rounded-full bg-gradient-to-br from-[#7C8CD6] to-[#5A6AB8] flex items-center justify-center">
            <Target className="w-3 h-3 text-white/80" />
          </div>
          <span className="text-sm font-medium" style={{color: PHASE_COLORS.primary}}>
            {vote.targetName || '未知目标'}
          </span>
        </div>
      </div>

      {/* 投票理由 */}
      {vote.reason && (
        <div className="flex items-start gap-2 pt-2 border-t border-[#E0E5EE] dark:border-[#363D4D]">
          <FileText className="w-3.5 h-3.5 text-[#8C96A5] mt-0.5 flex-shrink-0" />
          <p className="text-xs text-[#8C96A5] line-clamp-2">
            {vote.reason}
          </p>
        </div>
      )}
    </motion.div>
  )
})

VoteResultCard.displayName = 'VoteResultCard'

VoteResultCard.propTypes = {
  vote: PropTypes.shape({
    voterName: PropTypes.string,
    targetName: PropTypes.string,
    reason: PropTypes.string,
  }).isRequired,
  index: PropTypes.number,
}

VoteResultCard.defaultProps = {
  index: 0,
}

export default VoteResultCard

/**
 * @fileoverview ClueListItem 组件 - 线索列表项
 * @description 展示单个线索的列表项，支持悬停查看详情
 * @author zewang
 */

import {memo} from 'react'
import PropTypes from 'prop-types'
import {motion} from 'framer-motion'
import {FileText} from 'lucide-react'

/**
 * ClueListItem 组件 - 线索列表项
 *
 * @param {Object} props - 组件属性
 * @param {Object} props.clue - 线索对象
 * @param {number} props.clue.id - 线索ID
 * @param {string} props.clue.name - 线索名称
 * @param {string} props.clue.description - 线索描述
 * @param {string} props.clue.type - 线索类型
 * @param {number} props.index - 索引用于交错动画
 * @param {Function} props.onHover - 悬停回调函数，参数为 clue 对象
 */
const ClueListItem = memo(({
  clue,
  index = 0,
  onHover,
}) => {
  const handleMouseEnter = () => {
    if (onHover) {
      onHover(clue)
    }
  }

  const handleMouseLeave = () => {
    if (onHover) {
      onHover(null)
    }
  }

  return (
    <motion.div
      initial={{opacity: 0, x: -10}}
      animate={{opacity: 1, x: 0}}
      transition={{duration: 0.2, delay: index * 0.05}}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      className="group p-3 rounded-lg bg-white/40 dark:bg-[#1A1D26]/40 border border-[#E0E5EE] dark:border-[#363D4D] hover:border-[#7C8CD6]/50 hover:bg-white/60 dark:hover:bg-[#1A1D26]/60 transition-all duration-200 cursor-pointer"
    >
      <div className="flex items-start gap-3">
        {/* 线索图标 */}
        <div className="flex-shrink-0 w-8 h-8 rounded-lg bg-gradient-to-br from-[#7C8CD6]/20 to-[#7C8CD6]/5 flex items-center justify-center">
          <FileText className="w-4 h-4 text-[#7C8CD6]" />
        </div>

        {/* 线索信息 */}
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-medium text-[#2D3748] dark:text-[#E8ECF2] truncate group-hover:text-[#7C8CD6] transition-colors">
            {clue.name || '未知道具'}
          </h4>
          <p className="text-xs text-[#8C96A5] line-clamp-2 mt-0.5">
            {clue.description || '暂无描述'}
          </p>
        </div>
      </div>
    </motion.div>
  )
})

ClueListItem.displayName = 'ClueListItem'

ClueListItem.propTypes = {
  clue: PropTypes.shape({
    id: PropTypes.number.isRequired,
    name: PropTypes.string,
    description: PropTypes.string,
    type: PropTypes.string,
  }).isRequired,
  index: PropTypes.number,
  onHover: PropTypes.func,
}

ClueListItem.defaultProps = {
  index: 0,
  onHover: null,
}

export default ClueListItem

/**
 * @fileoverview 线索列表项组件
 * @description 用于讨论阶段左侧公开线索列表的展示
 * @author zewang
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {FileText} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * 线索列表项组件
 * @param {Object} props - 组件属性
 * @param {Object} props.clue - 线索数据
 * @param {string} props.clue.name - 线索名称
 * @param {string} props.clue.type - 线索类型
 * @param {number} props.index - 索引，用于动画延迟
 * @param {Function} props.onHover - 悬停回调函数
 * @returns {JSX.Element} 线索列表项组件
 */
const ClueListItem = memo(({clue, index, onHover}) => (
  <motion.div
    initial={{opacity: 0, x: -10}}
    animate={{opacity: 1, x: 0}}
    transition={{delay: index * 0.05}}
    onMouseEnter={() => onHover?.(clue)}
    onMouseLeave={() => onHover?.(null)}
    className="group p-2.5 rounded-lg bg-white/40 dark:bg-[#222631]/40 backdrop-blur-sm border border-transparent hover:bg-white/60 dark:hover:bg-[#222631]/60 hover:border-[#7C8CD6]/30 transition-all cursor-pointer"
  >
    <div className="flex items-start gap-2.5">
      <div
        className="w-6 h-6 rounded-md bg-gradient-to-br from-[#7C8CD6]/20 to-[#A78BFA]/20 flex items-center justify-center flex-shrink-0"
      >
        <FileText className="w-3 h-3" style={{color: PHASE_COLORS.primary}}/>
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-[#2D3748] dark:text-[#E8ECF2] text-xs font-medium leading-tight line-clamp-2">
          {clue.name}
        </p>
        <span className="text-[10px] text-[#8C96A5] dark:text-[#6B7788] mt-0.5 block">
          {clue.type}
        </span>
      </div>
    </div>
  </motion.div>
))

ClueListItem.displayName = 'ClueListItem'

export default ClueListItem

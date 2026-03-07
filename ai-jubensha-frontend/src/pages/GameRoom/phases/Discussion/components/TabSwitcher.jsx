/**
 * @fileoverview 标签切换器组件
 * @description 用于讨论阶段在讨论和投票标签之间切换
 * @author zewang
 */

import {memo} from 'react'
import {MessageCircle, Vote} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * 标签切换器组件
 * @param {Object} props - 组件属性
 * @param {string} props.activeTab - 当前激活的标签 ('discussion' | 'vote')
 * @param {Function} props.onChange - 标签切换回调函数
 * @param {boolean} props.hasVoted - 是否已投票
 * @returns {JSX.Element} 标签切换器组件
 */
const TabSwitcher = memo(({activeTab, onChange, hasVoted}) => (
  <div
    className="inline-flex p-1 rounded-xl bg-[#EEF1F6]/80 dark:bg-[#222631]/80 backdrop-blur-sm border border-[#E0E5EE] dark:border-[#363D4D]"
  >
    <button
      onClick={() => onChange('discussion')}
      className={`
        px-4 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer
        ${activeTab === 'discussion'
          ? 'bg-white dark:bg-[#2A2F3C] shadow-sm'
          : 'text-[#8C96A5] hover:text-[#5A6978]'
        }
      `}
      style={activeTab === 'discussion' ? {color: PHASE_COLORS.primary} : {}}
    >
      <MessageCircle className="w-3.5 h-3.5"/>
      讨论
    </button>
    <button
      onClick={() => onChange('vote')}
      className={`
        px-4 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer
        ${activeTab === 'vote'
          ? 'bg-white dark:bg-[#2A2F3C] shadow-sm'
          : 'text-[#8C96A5] hover:text-[#5A6978]'
        }
      `}
      style={activeTab === 'vote' ? {color: PHASE_COLORS.accent} : {}}
    >
      <Vote className="w-3.5 h-3.5"/>
      投票
      {hasVoted && (
        <span 
          className="w-1.5 h-1.5 rounded-full"
          style={{backgroundColor: PHASE_COLORS.success}}
        />
      )}
    </button>
  </div>
))

TabSwitcher.displayName = 'TabSwitcher'

export default TabSwitcher

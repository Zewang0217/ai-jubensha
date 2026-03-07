/**
 * @fileoverview TabSwitcher 组件 - 标签切换器
 * @description 用于在讨论阶段切换不同标签页（投票、讨论等）
 * @author zewang
 */

import {memo} from 'react'
import PropTypes from 'prop-types'
import {motion} from 'framer-motion'
import {Vote, MessageCircle} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * TabSwitcher 组件 - 标签切换器
 *
 * @param {Object} props - 组件属性
 * @param {string} props.activeTab - 当前激活的标签页 ('vote' | 'discussion')
 * @param {Function} props.onChange - 切换标签页的回调函数
 * @param {boolean} props.hasVoted - 是否已投票（影响投票标签的显示）
 */
const TabSwitcher = memo(({
  activeTab,
  onChange,
  hasVoted = false,
}) => {
  const tabs = [
    {
      id: 'vote',
      label: '投票',
      icon: Vote,
      badge: hasVoted ? '已投' : null,
    },
    {
      id: 'discussion',
      label: '讨论',
      icon: MessageCircle,
    },
  ]

  return (
    <div className="flex items-center gap-1 p-1 rounded-xl bg-white/60 dark:bg-[#222631]/60 backdrop-blur-md border border-[#E0E5EE] dark:border-[#363D4D]">
      {tabs.map((tab) => {
        const isActive = activeTab === tab.id
        const Icon = tab.icon

        return (
          <button
            key={tab.id}
            onClick={() => onChange && onChange(tab.id)}
            className={`
              relative flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200
              ${isActive
                ? 'text-white'
                : 'text-[#8C96A5] hover:text-[#2D3748] dark:hover:text-[#E8ECF2]'
              }
            `}
          >
            {isActive && (
              <motion.div
                layoutId="activeTab"
                className="absolute inset-0 rounded-lg"
                style={{backgroundColor: PHASE_COLORS.primary}}
                transition={{type: 'spring', bounce: 0.2, duration: 0.6}}
              />
            )}
            <span className="relative z-10 flex items-center gap-2">
              <Icon className="w-4 h-4" />
              {tab.label}
              {tab.badge && (
                <span className={`
                  ml-1 text-[10px] px-1.5 py-0.5 rounded-full
                  ${isActive ? 'bg-white/20' : 'bg-[#7C8CD6]/10 text-[#7C8CD6]'}
                `}>
                  {tab.badge}
                </span>
              )}
            </span>
          </button>
        )
      })}
    </div>
  )
})

TabSwitcher.displayName = 'TabSwitcher'

TabSwitcher.propTypes = {
  activeTab: PropTypes.oneOf(['vote', 'discussion']).isRequired,
  onChange: PropTypes.func.isRequired,
  hasVoted: PropTypes.bool,
}

TabSwitcher.defaultProps = {
  hasVoted: false,
}

export default TabSwitcher

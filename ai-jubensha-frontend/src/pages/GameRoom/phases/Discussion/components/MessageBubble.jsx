/**
 * @fileoverview 消息气泡组件
 * @description 用于讨论阶段聊天消息的展示
 * @author zewang
 */

import {memo} from 'react'
import {Bot, User} from 'lucide-react'
import {PHASE_COLORS} from '../../../config/theme'

/**
 * 消息气泡组件
 * @param {Object} props - 组件属性
 * @param {Object} props.message - 消息数据
 * @param {string} props.message.sender - 发送者名称
 * @param {string} props.message.content - 消息内容
 * @param {string} props.message.time - 消息时间
 * @param {boolean} props.message.isAI - 是否为 AI 消息
 * @param {boolean} props.message.isSystem - 是否为系统消息
 * @param {boolean} props.isSelf - 是否为自己发送的消息
 * @returns {JSX.Element} 消息气泡组件
 */
const MessageBubble = memo(({message, isSelf}) => {
  const isSystem = message.isSystem

  return (
    <div
      className={`flex gap-3 ${isSelf ? 'flex-row-reverse' : ''}`}
    >
      {!isSystem && (
        <div
          className={`
            w-9 h-9 rounded-lg flex-shrink-0 flex items-center justify-center
            ${isSelf
              ? `bg-gradient-to-br from-[${PHASE_COLORS.primary}] to-[${PHASE_COLORS.secondary}] text-white`
              : message.isAI
                ? 'bg-[#EEF1F6] dark:bg-[#2A2F3C]'
                : 'bg-[#F5A9C9]/20'
            }
          `}
          style={isSelf ? {
            background: `linear-gradient(to bottom right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary})`
          } : {}}
        >
          {message.isAI ? (
            <Bot className="w-4 h-4" style={{color: PHASE_COLORS.primary}}/>
          ) : (
            <User className="w-4 h-4" style={{color: PHASE_COLORS.accent}}/>
          )}
        </div>
      )}

      <div className={`max-w-[75%] ${isSelf ? 'text-right' : ''}`}>
        {!isSystem && (
          <p className="text-[#8C96A5] dark:text-[#6B7788] text-[10px] mb-1">
            {message.sender} • {message.time}
          </p>
        )}
        <div
          className={`
            inline-block px-3 py-2 rounded-xl text-xs leading-relaxed
            ${isSystem
              ? `bg-[${PHASE_COLORS.primary}]/10 text-[${PHASE_COLORS.primary}] w-full text-center border border-[${PHASE_COLORS.primary}]/20`
              : isSelf
                ? 'text-white'
                : 'bg-white/80 dark:bg-[#222631]/80 text-[#2D3748] dark:text-[#E8ECF2] border border-[#E0E5EE] dark:border-[#363D4D]'
            }
          `}
          style={isSystem ? {
            backgroundColor: `${PHASE_COLORS.primary}10`,
            color: PHASE_COLORS.primary,
            borderColor: `${PHASE_COLORS.primary}20`,
          } : isSelf ? {
            background: `linear-gradient(to right, ${PHASE_COLORS.primary}, ${PHASE_COLORS.secondary})`
          } : {}}
        >
          {message.content}
        </div>
      </div>
    </div>
  )
})

MessageBubble.displayName = 'MessageBubble'

export default MessageBubble

/**
 * @fileoverview MessageBubble 组件 - 消息气泡
 * @description 展示单条聊天消息，区分自己和他人的消息样式
 * @author zewang
 */

import {memo} from 'react'
import PropTypes from 'prop-types'
import {motion} from 'framer-motion'
import {User, Bot} from 'lucide-react'

/**
 * MessageBubble 组件 - 消息气泡
 *
 * @param {Object} props - 组件属性
 * @param {Object} props.message - 消息对象
 * @param {number} props.message.id - 消息ID
 * @param {string} props.message.content - 消息内容
 * @param {string} props.message.senderName - 发送者名称
 * @param {string} props.message.senderAvatar - 发送者头像URL
 * @param {boolean} props.message.isAI - 是否为AI发送的消息
 * @param {string} props.message.timestamp - 消息时间戳
 * @param {boolean} props.isSelf - 是否为自己发送的消息
 */
const MessageBubble = memo(({
  message,
  isSelf = false,
}) => {
  const isAI = message.isAI || false

  return (
    <motion.div
      initial={{opacity: 0, y: 10}}
      animate={{opacity: 1, y: 0}}
      transition={{duration: 0.2}}
      className={`flex gap-3 ${isSelf ? 'flex-row-reverse' : 'flex-row'}`}
    >
      {/* 头像 */}
      <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center overflow-hidden ${
        isAI
          ? 'bg-gradient-to-br from-purple-400 to-purple-600'
          : isSelf
            ? 'bg-gradient-to-br from-[#7C8CD6] to-[#5A6AB8]'
            : 'bg-gradient-to-br from-[#8C96A5] to-[#6B7280]'
      }`}>
        {message.senderAvatar ? (
          <img
            src={message.senderAvatar}
            alt={message.senderName}
            className="w-full h-full object-cover"
          />
        ) : isAI ? (
          <Bot className="w-4 h-4 text-white/80" />
        ) : (
          <User className="w-4 h-4 text-white/80" />
        )}
      </div>

      {/* 消息内容 */}
      <div className={`flex flex-col ${isSelf ? 'items-end' : 'items-start'} max-w-[70%]`}>
        {/* 发送者名称 */}
        <span className="text-xs text-[#8C96A5] mb-1">
          {message.senderName || (isAI ? 'AI助手' : '未知用户')}
        </span>

        {/* 气泡 */}
        <div className={`px-4 py-2.5 rounded-2xl text-sm ${
          isSelf
            ? 'bg-[#7C8CD6] text-white rounded-tr-sm'
            : isAI
              ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-200 rounded-tl-sm border border-purple-200 dark:border-purple-800'
              : 'bg-white dark:bg-[#2A2F3C] text-[#2D3748] dark:text-[#E8ECF2] rounded-tl-sm border border-[#E0E5EE] dark:border-[#363D4D]'
        }`}>
          {message.content}
        </div>

        {/* 时间戳 */}
        {message.timestamp && (
          <span className="text-[10px] text-[#8C96A5] mt-1">
            {new Date(message.timestamp).toLocaleTimeString('zh-CN', {
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        )}
      </div>
    </motion.div>
  )
})

MessageBubble.displayName = 'MessageBubble'

MessageBubble.propTypes = {
  message: PropTypes.shape({
    id: PropTypes.number.isRequired,
    content: PropTypes.string.isRequired,
    senderName: PropTypes.string,
    senderAvatar: PropTypes.string,
    isAI: PropTypes.bool,
    timestamp: PropTypes.string,
  }).isRequired,
  isSelf: PropTypes.bool,
}

MessageBubble.defaultProps = {
  isSelf: false,
}

export default MessageBubble

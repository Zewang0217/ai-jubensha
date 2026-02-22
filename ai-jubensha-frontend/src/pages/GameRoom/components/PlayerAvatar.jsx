import {motion} from 'framer-motion'

/**
 * 玩家头像组件
 * 显示玩家头像、在线状态等信息
 *
 * @param {Object} player - 玩家数据
 * @param {boolean} isHost - 是否房主
 * @param {boolean} showStatus - 是否显示在线状态
 * @param {string} size - 尺寸大小 (sm, md, lg)
 * @param {boolean} selected - 是否被选中
 * @param {Function} onClick - 点击回调
 */
function PlayerAvatar({
                          player,
                          isHost = false,
                          showStatus = true,
                          size = 'md',
                          selected = false,
                          onClick = null,
                      }) {
    const sizeClasses = {
        sm: 'w-8 h-8 text-sm',
        md: 'w-10 h-10 text-base',
        lg: 'w-12 h-12 text-lg',
    }

    const statusSizeClasses = {
        sm: 'w-2 h-2',
        md: 'w-2.5 h-2.5',
        lg: 'w-3 h-3',
    }

    return (
        <motion.div
            whileHover={onClick ? {scale: 1.05} : {}}
            whileTap={onClick ? {scale: 0.95} : {}}
            onClick={() => onClick?.(player)}
            className={`relative inline-flex ${onClick ? 'cursor-pointer' : ''}`}
        >
            <div
                className={`${sizeClasses[size]} rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold ${
                    selected ? 'ring-2 ring-blue-400 ring-offset-2 ring-offset-slate-900' : ''
                }`}
            >
                {player?.name?.[0] || '?'}
            </div>

            {/* 在线状态 */}
            {showStatus && (
                <div
                    className={`absolute -bottom-0.5 -right-0.5 ${statusSizeClasses[size]} rounded-full bg-emerald-500 border-2 border-slate-900`}
                />
            )}

            {/* 房主标记 */}
            {isHost && (
                <div
                    className="absolute -top-1 -right-1 px-1.5 py-0.5 text-[10px] rounded-full bg-amber-500 text-white font-bold border border-slate-900">
                    房主
                </div>
            )}
        </motion.div>
    )
}

export default PlayerAvatar
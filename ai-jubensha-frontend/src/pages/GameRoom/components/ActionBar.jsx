import {motion} from 'framer-motion'
import {BookOpen, LogOut, MessageCircle, Settings, Users} from 'lucide-react'

/**
 * 游戏操作按钮配置
 */
const ACTION_BUTTONS = [
    {
        id: 'script',
        label: '剧本',
        icon: BookOpen,
        variant: 'default',
    },
    {
        id: 'players',
        label: '玩家',
        icon: Users,
        variant: 'default',
    },
    {
        id: 'chat',
        label: '聊天',
        icon: MessageCircle,
        variant: 'default',
    },
    {
        id: 'settings',
        label: '设置',
        icon: Settings,
        variant: 'default',
    },
    {
        id: 'exit',
        label: '退出游戏',
        icon: LogOut,
        variant: 'danger',
    },
]

/**
 * 操作按钮组件
 * 显示游戏功能操作按钮
 *
 * @param {Function} onAction - 按钮点击回调，接收 actionId 参数
 * @param {Array} actions - 自定义按钮配置（可选，默认使用 ACTION_BUTTONS）
 * @param {boolean} disabled - 是否禁用所有按钮
 */
function ActionBar({onAction, actions = null, disabled = false}) {
    const buttons = actions || ACTION_BUTTONS

    return (
        <div className="flex items-center gap-3">
            {buttons.map((button) => {
                const Icon = button.icon
                const isDanger = button.variant === 'danger'

                return (
                    <motion.button
                        key={button.id}
                        whileHover={!disabled ? {scale: 1.05} : {}}
                        whileTap={!disabled ? {scale: 0.95} : {}}
                        onClick={() => !disabled && onAction?.(button.id)}
                        disabled={disabled}
                        className={`flex items-center gap-2 px-4 py-2 rounded-xl font-medium text-sm transition-all duration-300 ${
                            disabled
                                ? 'opacity-50 cursor-not-allowed'
                                : 'cursor-pointer'
                        } ${
                            isDanger
                                ? 'bg-red-500/20 border border-red-500/30 text-red-400 hover:bg-red-500/30 hover:shadow-lg hover:shadow-red-500/20'
                                : 'bg-slate-800/50 border border-slate-700/50 text-slate-300 hover:bg-slate-700/50 hover:border-blue-500/30 hover:shadow-lg hover:shadow-blue-500/10'
                        }`}
                    >
                        <Icon className="w-4 h-4"/>
                        <span className="hidden sm:inline">{button.label}</span>
                    </motion.button>
                )
            })}
        </div>
    )
}

export default ActionBar
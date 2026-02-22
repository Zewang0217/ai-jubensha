import {AnimatePresence, motion} from 'framer-motion'
import {Eye, FileText, Fingerprint, HelpCircle, Lock, MessageSquare} from 'lucide-react'

/**
 * 线索类型配置
 */
const CLUE_TYPE_CONFIG = {
    physical: {
        label: '物证',
        icon: Fingerprint,
        gradient: 'from-rose-500/20 to-red-500/20',
        border: 'border-rose-500/30',
        text: 'text-rose-400',
        glow: 'shadow-rose-500/20',
    },
    testimony: {
        label: '证词',
        icon: MessageSquare,
        gradient: 'from-amber-500/20 to-yellow-500/20',
        border: 'border-amber-500/30',
        text: 'text-amber-400',
        glow: 'shadow-amber-500/20',
    },
    document: {
        label: '文件',
        icon: FileText,
        gradient: 'from-cyan-500/20 to-blue-500/20',
        border: 'border-cyan-500/30',
        text: 'text-cyan-400',
        glow: 'shadow-cyan-500/20',
    },
    other: {
        label: '其他',
        icon: HelpCircle,
        gradient: 'from-slate-500/20 to-gray-500/20',
        border: 'border-slate-500/30',
        text: 'text-slate-400',
        glow: 'shadow-slate-500/20',
    },
}

/**
 * 线索悬浮详情卡片
 * 采用侦探档案风格，深色背景配金色边框
 *
 * @param {Object} clue - 线索数据
 * @param {boolean} isVisible - 是否显示
 * @param {Object} position - 位置信息 {x, y, placement}
 * @param {boolean} isPrivate - 是否为私人线索
 */
function ClueCard({clue, isVisible, position, isPrivate = false}) {
    if (!clue) return null

    const typeConfig = CLUE_TYPE_CONFIG[clue.type] || CLUE_TYPE_CONFIG.other
    const TypeIcon = typeConfig.icon
    const VisibilityIcon = isPrivate ? Lock : Eye
    const visibilityText = isPrivate ? '私人线索' : '公开线索'
    const visibilityColor = isPrivate ? 'text-purple-400' : 'text-emerald-400'

    // 判断箭头位置：如果卡片在下方，箭头在顶部；如果在上方，箭头在底部
    const isPlacementBottom = position?.placement === 'bottom'

    return (
        <AnimatePresence>
            {isVisible && (
                <motion.div
                    initial={{opacity: 0, scale: 0.9, y: isPlacementBottom ? -10 : 10}}
                    animate={{opacity: 1, scale: 1, y: 0}}
                    exit={{opacity: 0, scale: 0.95, y: isPlacementBottom ? -10 : 10}}
                    transition={{duration: 0.2, ease: 'easeOut'}}
                    style={{
                        position: 'fixed',
                        left: position?.x || 0,
                        top: position?.y || 0,
                        zIndex: 100,
                        pointerEvents: 'none',
                    }}
                    className="w-80"
                >
                    {/* 顶部箭头（当卡片在下方时显示） */}
                    {isPlacementBottom && (
                        <div className="absolute -top-2 left-1/2 -translate-x-1/2">
                            <div
                                className="w-4 h-4 bg-slate-800/95 border-l-2 border-t-2 border-slate-600/50 transform rotate-45"/>
                        </div>
                    )}

                    {/* 卡片主体 */}
                    <div className={`
                        relative overflow-hidden rounded-xl border-2 backdrop-blur-xl
                        ${typeConfig.border} ${typeConfig.glow}
                        bg-gradient-to-br from-slate-900/95 to-slate-800/95
                        shadow-2xl
                    `}>
                        {/* 顶部装饰条 */}
                        <div className={`
                            h-1 bg-gradient-to-r ${typeConfig.gradient}
                        `}/>

                        {/* 角落装饰 */}
                        <div
                            className="absolute top-2 right-2 w-8 h-8 border-t-2 border-r-2 border-white/10 rounded-tr-lg"/>
                        <div
                            className="absolute bottom-2 left-2 w-8 h-8 border-b-2 border-l-2 border-white/10 rounded-bl-lg"/>

                        {/* 内容区 */}
                        <div className="p-5">
                            {/* 头部：类型和可见性 */}
                            <div className="flex items-center justify-between mb-4">
                                <div className={`
                                    flex items-center gap-2 px-3 py-1.5 rounded-full
                                    bg-gradient-to-r ${typeConfig.gradient} ${typeConfig.border} border
                                `}>
                                    <TypeIcon className={`w-4 h-4 ${typeConfig.text}`}/>
                                    <span className={`text-xs font-bold uppercase tracking-wider ${typeConfig.text}`}>
                                        {typeConfig.label}
                                    </span>
                                </div>
                                <div className={`
                                    flex items-center gap-1.5 px-3 py-1.5 rounded-full
                                    bg-slate-800/80 border border-slate-700/50
                                `}>
                                    <VisibilityIcon className={`w-3.5 h-3.5 ${visibilityColor}`}/>
                                    <span className={`text-xs font-medium ${visibilityColor}`}>
                                        {visibilityText}
                                    </span>
                                </div>
                            </div>

                            {/* 线索标题 */}
                            <h3 className="text-lg font-bold text-white mb-3 leading-tight">
                                {clue.name}
                            </h3>

                            {/* 分隔线 */}
                            <div
                                className="h-px bg-gradient-to-r from-transparent via-slate-600/50 to-transparent mb-4"/>

                            {/* 线索描述 */}
                            <p className="text-sm text-slate-300 leading-relaxed mb-4">
                                {clue.description}
                            </p>

                            {/* 发现信息 */}
                            <div
                                className="flex items-center gap-2 text-xs text-slate-500 pt-3 border-t border-slate-700/50">
                                <span>发现者:</span>
                                <span className="text-slate-300 font-medium">{clue.discoveredBy}</span>
                                <span className="mx-2">•</span>
                                <span>{clue.discoveredAt}</span>
                            </div>
                        </div>

                        {/* 底部装饰 */}
                        <div className={`
                            h-0.5 bg-gradient-to-r ${typeConfig.gradient} opacity-50
                        `}/>
                    </div>

                    {/* 底部箭头（当卡片在上方时显示） */}
                    {!isPlacementBottom && (
                        <div className="absolute -bottom-2 left-1/2 -translate-x-1/2">
                            <div
                                className="w-4 h-4 bg-slate-800/95 border-r-2 border-b-2 border-slate-600/50 transform rotate-45"/>
                        </div>
                    )}
                </motion.div>
            )}
        </AnimatePresence>
    )
}

export default ClueCard
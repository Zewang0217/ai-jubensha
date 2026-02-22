import {useRef, useState} from 'react'
import {motion} from 'framer-motion'
import {Eye, FileText, Fingerprint, HelpCircle, Lightbulb, Lock, MessageSquare} from 'lucide-react'
import ClueCard from './ClueCard'

/**
 * 线索类型配置
 */
const CLUE_TYPE_CONFIG = {
    physical: {
        label: '物证',
        icon: Fingerprint,
        bg: 'bg-rose-500/10',
        border: 'border-rose-500/20',
        text: 'text-rose-400',
        glow: 'group-hover:shadow-rose-500/20',
    },
    testimony: {
        label: '证词',
        icon: MessageSquare,
        bg: 'bg-amber-500/10',
        border: 'border-amber-500/20',
        text: 'text-amber-400',
        glow: 'group-hover:shadow-amber-500/20',
    },
    document: {
        label: '文件',
        icon: FileText,
        bg: 'bg-cyan-500/10',
        border: 'border-cyan-500/20',
        text: 'text-cyan-400',
        glow: 'group-hover:shadow-cyan-500/20',
    },
    other: {
        label: '其他',
        icon: HelpCircle,
        bg: 'bg-slate-500/10',
        border: 'border-slate-500/20',
        text: 'text-slate-400',
        glow: 'group-hover:shadow-slate-500/20',
    },
}

/**
 * 线索列表组件
 * 采用侦探档案风格，紧凑的列表布局
 *
 * @param {Array} clues - 线索列表
 * @param {string} title - 列表标题
 * @param {boolean} isPrivate - 是否为私人线索列表
 * @param {string} emptyText - 空状态提示文本
 */
function ClueList({clues = [], title, isPrivate = false, emptyText = '暂无线索'}) {
    const [hoveredClue, setHoveredClue] = useState(null)
    const [cardPosition, setCardPosition] = useState({x: 0, y: 0})
    const containerRef = useRef(null)

    const handleMouseEnter = (clue, event) => {
        const rect = event.currentTarget.getBoundingClientRect()

        // 计算卡片位置：在线索项上方显示，不遮挡鼠标
        const cardWidth = 320 // w-80 = 320px
        const cardHeight = 280 // 预估卡片高度
        const gap = 12 // 与线索项的间距

        let x = rect.left + rect.width / 2 - cardWidth / 2
        let y = rect.top - cardHeight - gap

        // 边界检查：确保不超出视口
        const viewportWidth = window.innerWidth

        // 水平边界检查
        if (x < 16) x = 16
        if (x + cardWidth > viewportWidth - 16) x = viewportWidth - cardWidth - 16

        // 垂直边界检查：如果上方空间不足，显示在下方
        if (y < 16) {
            y = rect.bottom + gap
        }

        setCardPosition({x, y, placement: y < rect.top ? 'top' : 'bottom'})
        setHoveredClue(clue)
    }

    const handleMouseLeave = () => {
        setHoveredClue(null)
    }

    const VisibilityIcon = isPrivate ? Lock : Eye
    const visibilityColor = isPrivate ? 'text-purple-400' : 'text-emerald-400'
    const headerGradient = isPrivate
        ? 'from-purple-500/20 to-pink-500/20'
        : 'from-emerald-500/20 to-teal-500/20'

    return (
        <>
            <div className="h-full flex flex-col rounded-xl bg-slate-800/50 border border-slate-700/50 overflow-hidden">
                {/* 头部 */}
                <div className={`
                    flex-none flex items-center justify-between px-4 py-3
                    bg-gradient-to-r ${headerGradient} border-b border-slate-700/50
                `}>
                    <div className="flex items-center gap-2">
                        <Lightbulb className={`w-4 h-4 ${visibilityColor}`}/>
                        <h3 className="font-bold text-slate-200 text-sm tracking-wide">{title}</h3>
                    </div>
                    <div className="flex items-center gap-2">
                        <VisibilityIcon className={`w-3.5 h-3.5 ${visibilityColor}`}/>
                        <span className={`
                            px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider
                            bg-slate-900/50 ${visibilityColor}
                        `}>
                            {clues.length}
                        </span>
                    </div>
                </div>

                {/* 线索列表 */}
                <div ref={containerRef} className="flex-1 overflow-y-auto p-3 space-y-2">
                    {clues.length > 0 ? (
                        clues.map((clue, index) => {
                            const typeConfig = CLUE_TYPE_CONFIG[clue.type] || CLUE_TYPE_CONFIG.other
                            const TypeIcon = typeConfig.icon

                            return (
                                <motion.div
                                    key={clue.id}
                                    initial={{opacity: 0, x: -20}}
                                    animate={{opacity: 1, x: 0}}
                                    transition={{delay: index * 0.05}}
                                    onMouseEnter={(e) => handleMouseEnter(clue, e)}
                                    onMouseLeave={handleMouseLeave}
                                    className={`
                                        group relative flex items-center gap-3 p-3 rounded-lg
                                        cursor-pointer transition-all duration-200
                                        bg-slate-900/50 ${typeConfig.border} border
                                        hover:bg-slate-800/80 hover:border-opacity-50
                                        hover:shadow-lg ${typeConfig.glow}
                                    `}
                                >
                                    {/* 类型图标 */}
                                    <div className={`
                                        flex-none w-9 h-9 rounded-lg flex items-center justify-center
                                        ${typeConfig.bg} ${typeConfig.border} border
                                        transition-transform duration-200 group-hover:scale-110
                                    `}>
                                        <TypeIcon className={`w-4 h-4 ${typeConfig.text}`}/>
                                    </div>

                                    {/* 线索信息 */}
                                    <div className="flex-1 min-w-0">
                                        <h4 className={`
                                            font-semibold text-sm truncate mb-0.5
                                            text-slate-200 group-hover:text-white
                                            transition-colors duration-200
                                        `}>
                                            {clue.name}
                                        </h4>
                                        <p className="text-xs text-slate-500 truncate group-hover:text-slate-400 transition-colors">
                                            {clue.description}
                                        </p>
                                    </div>

                                    {/* 悬停指示器 */}
                                    <div className={`
                                        flex-none w-1.5 h-1.5 rounded-full
                                        ${typeConfig.bg.replace('/10', '')} opacity-0 group-hover:opacity-100
                                        transition-opacity duration-200
                                    `}/>
                                </motion.div>
                            )
                        })
                    ) : (
                        <div className="h-full flex flex-col items-center justify-center text-slate-600 py-8">
                            <Lightbulb className="w-10 h-10 mb-2 opacity-30"/>
                            <p className="text-sm">{emptyText}</p>
                        </div>
                    )}
                </div>
            </div>

            {/* 悬浮卡片 */}
            <ClueCard
                clue={hoveredClue}
                isVisible={!!hoveredClue}
                position={cardPosition}
                isPrivate={isPrivate}
            />
        </>
    )
}

export default ClueList
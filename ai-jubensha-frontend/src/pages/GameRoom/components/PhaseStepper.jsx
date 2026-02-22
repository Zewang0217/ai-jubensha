import {motion} from 'framer-motion'
import {GAME_PHASE, GAME_PHASE_TEXT} from '../../../utils/constants'
import {MessageSquare, Search, Trophy, UserCircle, Vote} from 'lucide-react'

/**
 * 阶段图标映射
 */
const PHASE_ICONS = {
    [GAME_PHASE.INTRODUCTION]: UserCircle,
    [GAME_PHASE.SEARCH]: Search,
    [GAME_PHASE.DISCUSSION]: MessageSquare,
    [GAME_PHASE.VOTING]: Vote,
    [GAME_PHASE.ENDING]: Trophy,
}

/**
 * 阶段条组件
 * 显示所有游戏阶段及当前阶段状态
 *
 * @param {string} currentPhase - 当前阶段
 * @param {Array} phases - 阶段列表（预留，用于从API获取）
 * @param {Function} onPhaseClick - 阶段点击回调（可选）
 * @param {boolean} interactive - 是否可交互
 */
function PhaseStepper({currentPhase, phases = null, onPhaseClick = null, interactive = false}) {
    // 使用传入的阶段列表或默认全部阶段
    const phaseList = phases || Object.values(GAME_PHASE)

    // 获取当前阶段索引
    const currentIndex = phaseList.indexOf(currentPhase)

    return (
        <div className="w-full">
            <div className="flex items-center justify-between">
                {phaseList.map((phase, index) => {
                    const Icon = PHASE_ICONS[phase]
                    const isActive = phase === currentPhase
                    const isCompleted = index < currentIndex
                    const isPending = index > currentIndex

                    return (
                        <div key={phase} className="flex-1 flex items-center">
                            {/* 阶段节点 */}
                            <motion.button
                                whileHover={interactive ? {scale: 1.05} : {}}
                                whileTap={interactive ? {scale: 0.95} : {}}
                                onClick={() => interactive && onPhaseClick?.(phase)}
                                className={`relative flex flex-col items-center gap-2 group ${
                                    interactive ? 'cursor-pointer' : 'cursor-default'
                                }`}
                            >
                                {/* 图标容器 */}
                                <motion.div
                                    initial={false}
                                    animate={{
                                        backgroundColor: isActive
                                            ? 'rgba(59, 130, 246, 1)'
                                            : isCompleted
                                                ? 'rgba(16, 185, 129, 0.8)'
                                                : 'rgba(30, 41, 59, 0.8)',
                                        borderColor: isActive
                                            ? 'rgba(96, 165, 250, 1)'
                                            : isCompleted
                                                ? 'rgba(16, 185, 129, 0.5)'
                                                : 'rgba(71, 85, 105, 0.5)',
                                        scale: isActive ? 1.1 : 1,
                                    }}
                                    className={`w-10 h-10 rounded-xl flex items-center justify-center border-2 transition-all duration-300 ${
                                        isPending ? 'opacity-50' : 'opacity-100'
                                    }`}
                                >
                                    <Icon
                                        className={`w-5 h-5 transition-colors duration-300 ${
                                            isActive
                                                ? 'text-white'
                                                : isCompleted
                                                    ? 'text-white'
                                                    : 'text-slate-500'
                                        }`}
                                    />
                                </motion.div>

                                {/* 阶段名称 */}
                                <span
                                    className={`text-xs font-medium whitespace-nowrap transition-colors duration-300 ${
                                        isActive
                                            ? 'text-blue-400'
                                            : isCompleted
                                                ? 'text-emerald-400'
                                                : 'text-slate-500'
                                    }`}
                                >
                                    {GAME_PHASE_TEXT[phase]}
                                </span>

                                {/* 活跃指示器 */}
                                {isActive && (
                                    <motion.div
                                        layoutId="activePhaseIndicator"
                                        className="absolute -bottom-1 w-8 h-0.5 bg-blue-400 rounded-full"
                                    />
                                )}
                            </motion.button>

                            {/* 连接线 */}
                            {index < phaseList.length - 1 && (
                                <div className="flex-1 h-0.5 mx-2 relative">
                                    {/* 背景线 */}
                                    <div className="absolute inset-0 bg-slate-700/50 rounded-full"/>
                                    {/* 进度线 */}
                                    <motion.div
                                        initial={false}
                                        animate={{
                                            width: isCompleted ? '100%' : '0%',
                                        }}
                                        transition={{duration: 0.5, ease: 'easeInOut'}}
                                        className="absolute inset-y-0 left-0 bg-gradient-to-r from-emerald-500 to-blue-500 rounded-full"
                                    />
                                </div>
                            )}
                        </div>
                    )
                })}
            </div>
        </div>
    )
}

export default PhaseStepper
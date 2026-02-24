/**
 * @fileoverview GameRoomFooter 组件 - 底部状态栏
 * @description 游戏房间底部状态栏，显示当前阶段、进度、游戏状态等信息
 */

import React, {memo} from 'react'
import {motion} from 'framer-motion'
import {ChevronLeft, Clock} from 'lucide-react'
import {PHASE_CONFIG} from '../phases'

// =============================================================================
// 主组件
// =============================================================================

const GameRoomFooter = memo(({
                                 currentPhase,
                                 progress,
                                 canGoBack,
                                 onBack,
                                 gameStatus,
                             }) => {
    const getStatusColor = (status) => {
        switch (status) {
            case 'playing':
                return 'text-green-400'
            case 'waiting':
                return 'text-amber-400'
            default:
                return 'text-stone-400'
        }
    }

    const getStatusText = (status) => {
        switch (status) {
            case 'playing':
                return 'ACTIVE CASE'
            case 'waiting':
                return 'PENDING'
            default:
                return 'CLOSED'
        }
    }

    return (
        <div
            className="relative z-10 flex-none px-4 sm:px-6 py-3 border-t-2 border-stone-800 bg-stone-900/95 backdrop-blur-xl">
            {/* 装饰线 */}
            <div
                className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-amber-700/30 to-transparent"/>

            <div className="flex items-center justify-between text-xs sm:text-sm">
                <div className="flex items-center gap-4">
                    {/* 当前阶段 */}
                    <div className="flex items-center gap-2">
                        <Clock className="w-3.5 h-3.5 text-stone-500"/>
                        <span className="text-stone-500">Now:</span>
                        <span className="text-amber-200 font-serif">{PHASE_CONFIG[currentPhase]?.title}</span>
                    </div>

                    {/* 返回按钮 */}
                    {canGoBack && (
                        <button
                            onClick={onBack}
                            className="flex items-center gap-1 text-stone-500 hover:text-stone-300 transition-colors"
                        >
                            <ChevronLeft className="w-3.5 h-3.5"/>
                            <span>Review</span>
                        </button>
                    )}
                </div>

                <div className="flex items-center gap-6">
                    {/* 进度 */}
                    <div className="flex items-center gap-2">
                        <span className="text-stone-500">Progress:</span>
                        <div className="w-24 h-1.5 bg-stone-800 rounded-full overflow-hidden">
                            <motion.div
                                className="h-full bg-gradient-to-r from-amber-700 to-amber-500"
                                initial={{width: 0}}
                                animate={{width: `${progress}%`}}
                                transition={{duration: 0.5}}
                            />
                        </div>
                        <span className="text-stone-300 font-mono">{progress}%</span>
                    </div>

                    {/* 状态 */}
                    <div className="flex items-center gap-2">
                        <span className="text-stone-500">Status:</span>
                        <span className={`font-medium ${getStatusColor(gameStatus)}`}>
              {getStatusText(gameStatus)}
            </span>
                    </div>
                </div>
            </div>
        </div>
    )
})

GameRoomFooter.displayName = 'GameRoomFooter'

export default GameRoomFooter

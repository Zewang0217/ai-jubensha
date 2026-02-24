/**
 * @fileoverview GameRoomHeader 组件 - 顶部导航栏
 * @description 游戏房间顶部导航栏，包含事务所招牌、档案标签、案件编号、无线电状态和操作按钮
 */

import React, {memo} from 'react'
import {motion} from 'framer-motion'
import {Bug, FileText, FolderOpen, LogOut, Radio} from 'lucide-react'
import {PHASE_CONFIG} from '../phases'

// =============================================================================
// 子组件
// =============================================================================

/**
 * 侦探事务所招牌
 */
const OfficeSign = memo(() => (
    <div className="flex items-center gap-3">
        <div className="relative">
            {/* 台灯效果 */}
            <div className="absolute -top-8 left-1/2 -translate-x-1/2 w-20 h-20 bg-amber-500/10 rounded-full blur-xl"/>
            <div
                className="w-10 h-10 bg-gradient-to-br from-amber-700 to-amber-900 rounded-lg flex items-center justify-center border border-amber-600/50 shadow-lg shadow-amber-900/20">
                <span className="text-amber-100 font-serif text-lg font-bold">N</span>
            </div>
        </div>
        <div className="hidden sm:block">
            <h1 className="text-amber-100 font-serif text-sm tracking-wider">NOIR</h1>
            <p className="text-stone-500 text-xs tracking-widest">DETECTIVE AGENCY</p>
        </div>
    </div>
))
OfficeSign.displayName = 'OfficeSign'

/**
 * 档案文件夹标签
 */
const CaseFileTab = memo(({currentPhase, sequence}) => {
    const currentIndex = sequence.indexOf(currentPhase)

    return (
        <div className="flex items-center">
            <FolderOpen className="w-4 h-4 text-amber-600/60 mr-2"/>
            <div className="flex items-center gap-1">
                {sequence.map((phase, index) => {
                    const config = PHASE_CONFIG[phase]
                    const isActive = index === currentIndex
                    const isCompleted = index < currentIndex

                    return (
                        <div key={phase} className="flex items-center">
                            <motion.button
                                whileHover={{scale: 1.05}}
                                className={`
                  relative px-3 py-1.5 text-xs font-serif transition-all duration-200
                  ${isActive
                                    ? 'bg-amber-900/40 text-amber-200 border border-amber-700/50'
                                    : isCompleted
                                        ? 'bg-stone-800/50 text-stone-400 border border-stone-700'
                                        : 'bg-stone-900/30 text-stone-600 border border-stone-800'
                                }
                `}
                            >
                                <span className="hidden lg:inline">{config.title}</span>
                                <span className="lg:hidden">{String(index + 1).padStart(2, '0')}</span>
                                {isActive && (
                                    <motion.div
                                        layoutId="activeTab"
                                        className="absolute inset-0 border-2 border-amber-500/30"
                                        transition={{type: 'spring', stiffness: 500, damping: 30}}
                                    />
                                )}
                            </motion.button>
                            {index < sequence.length - 1 && (
                                <div className="w-4 h-px bg-stone-700 mx-0.5"/>
                            )}
                        </div>
                    )
                })}
            </div>
        </div>
    )
})
CaseFileTab.displayName = 'CaseFileTab'

/**
 * 打字机风格的案件编号
 */
const CaseNumber = memo(({id}) => (
    <div className="flex items-center gap-2 px-3 py-1.5 bg-stone-900/50 border border-stone-700">
        <FileText className="w-3.5 h-3.5 text-stone-500"/>
        <div className="flex flex-col">
            <span className="text-[10px] text-stone-500 uppercase tracking-wider">Case No.</span>
            <span className="text-xs text-stone-300 font-mono">#{String(id).padStart(4, '0')}</span>
        </div>
    </div>
))
CaseNumber.displayName = 'CaseNumber'

/**
 * 无线电状态指示器
 */
const RadioStatus = memo(({isConnected, isDebugMode}) => {
    return (
        <div className="flex items-center gap-2 px-3 py-1.5 bg-stone-900/50 border border-stone-700">
            <Radio
                className={`w-3.5 h-3.5 ${isDebugMode ? 'text-amber-500' : isConnected ? 'text-green-500' : 'text-red-500'}`}
            />
            <div className="flex flex-col">
        <span
            className={`text-[10px] uppercase tracking-wider ${isDebugMode ? 'text-amber-500' : isConnected ? 'text-green-400' : 'text-red-400'}`}>
          {isDebugMode ? 'SIMULATION' : isConnected ? 'ONLINE' : 'OFFLINE'}
        </span>
                <div className="flex gap-0.5 mt-0.5">
                    {[1, 2, 3].map((i) => (
                        <motion.div
                            key={i}
                            className={`w-1 h-1 ${isDebugMode ? 'bg-amber-500' : isConnected ? 'bg-green-500' : 'bg-red-500'}`}
                            animate={{opacity: [0.3, 1, 0.3]}}
                            transition={{duration: 1.5, repeat: Infinity, delay: i * 0.2}}
                        />
                    ))}
                </div>
            </div>
        </div>
    )
})
RadioStatus.displayName = 'RadioStatus'

// =============================================================================
// 主组件
// =============================================================================

const GameRoomHeader = memo(({
                                 id,
                                 currentPhase,
                                 sequence,
                                 isConnected,
                                 isDebugMode,
                                 showDebugPanel,
                                 onToggleDebugPanel,
                                 onExit,
                             }) => {
    return (
        <header
            className="relative z-10 flex-none px-4 sm:px-6 py-4 border-b-2 border-stone-800 bg-stone-900/95 backdrop-blur-xl">
            {/* 装饰线 */}
            <div
                className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-amber-700/30 to-transparent"/>

            <div className="flex items-center justify-between gap-4">
                {/* 左侧：事务所招牌 + 档案标签 */}
                <div className="flex items-center gap-6">
                    <OfficeSign/>
                    <div className="hidden md:block">
                        <CaseFileTab currentPhase={currentPhase} sequence={sequence}/>
                    </div>
                </div>

                {/* 右侧：案件编号 + 无线电状态 + 操作 */}
                <div className="flex items-center gap-3">
                    <div className="hidden sm:block">
                        <CaseNumber id={id}/>
                    </div>
                    <RadioStatus isConnected={isConnected} isDebugMode={isDebugMode}/>

                    {/* 调试面板开关 */}
                    <button
                        onClick={onToggleDebugPanel}
                        className={`
              p-2 border transition-colors
              ${showDebugPanel
                            ? 'border-amber-600 bg-amber-900/20 text-amber-400'
                            : 'border-stone-700 text-stone-500 hover:text-stone-300 hover:border-stone-600'
                        }
            `}
                        title="Detective's Notes"
                    >
                        <Bug className="w-4 h-4"/>
                    </button>

                    {/* 退出按钮 */}
                    <button
                        onClick={onExit}
                        className="flex items-center gap-2 px-3 py-2 border border-stone-700 text-stone-400 hover:text-stone-200 hover:border-stone-500 transition-colors"
                    >
                        <LogOut className="w-4 h-4"/>
                        <span className="hidden sm:inline text-sm">Close</span>
                    </button>
                </div>
            </div>

            {/* 移动端阶段标签 */}
            <div className="md:hidden mt-3 overflow-x-auto pb-1">
                <CaseFileTab currentPhase={currentPhase} sequence={sequence}/>
            </div>
        </header>
    )
})

GameRoomHeader.displayName = 'GameRoomHeader'

export default GameRoomHeader

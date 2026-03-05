/**
 * @fileoverview ClueCard 组件 - 竖版扑克牌样式线索卡牌
 * @description 游戏卡牌组件，上下长左右短，支持翻牌动画和公开功能
 */

import {memo} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {Eye, FileText, Globe, Search, Sparkles} from 'lucide-react'

// =============================================================================
// 卡牌背面装饰图案
// =============================================================================

const CardBackPattern = memo(() => (
    <div className="absolute inset-2 rounded-lg overflow-hidden opacity-30">
        {/* 菱形网格图案 */}
        <svg className="w-full h-full" viewBox="0 0 100 140" preserveAspectRatio="none">
            <defs>
                <pattern id="diamond-pattern" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                    <path d="M10 0 L20 10 L10 20 L0 10 Z" fill="none" stroke="currentColor" strokeWidth="0.5"
                          className="text-[#7C8CD6]"/>
                </pattern>
            </defs>
            <rect width="100" height="140" fill="url(#diamond-pattern)"/>
        </svg>
    </div>
))

CardBackPattern.displayName = 'CardBackPattern'

// =============================================================================
// 卡牌角标（类似扑克牌角落的数字/花色）
// =============================================================================

const CardCorner = memo(({type, isTop = true}) => (
    <div className={`absolute ${isTop ? 'top-2 left-2' : 'bottom-2 right-2'} flex flex-col items-center`}>
    <span className={`text-[10px] font-bold ${isTop ? 'text-[#7C8CD6]' : 'text-[#7C8CD6] rotate-180'}`}>
      {type.charAt(0)}
    </span>
        <div className={`w-2 h-2 rounded-full bg-[#7C8CD6]/30 mt-0.5`}/>
    </div>
))

CardCorner.displayName = 'CardCorner'

// =============================================================================
// 线索卡牌组件 - 竖版扑克牌样式
// =============================================================================

const ClueCard = memo(({clue, isRevealed, isPublic, isObserverMode, onReveal, onPublic, index}) => {
    // 卡牌尺寸：宽度固定，高度约为宽度的 1.45 倍（扑克牌比例）
    const cardHeight = 'h-[250px]'

    // 观察者模式下始终显示内容
    const showContent = isObserverMode || isRevealed

    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{delay: index * 0.08, duration: 0.4, ease: [0.25, 0.1, 0.25, 1]}}
            className={`relative w-full ${cardHeight}`}
            style={{perspective: '1000px'}}
        >
            <AnimatePresence mode="wait">
                {!showContent ? (
                    <motion.button
                        key="hidden"
                        onClick={onReveal}
                        initial={{rotateY: -90, opacity: 0}}
                        animate={{rotateY: 0, opacity: 1}}
                        exit={{rotateY: 90, opacity: 0}}
                        transition={{duration: 0.4, ease: 'easeInOut'}}
                        whileHover={{
                            y: -2,
                            transition: {duration: 0.15}
                        }}
                        whileTap={{scale: 0.98}}
                        className="absolute inset-0 w-full rounded-xl overflow-hidden shadow-sm cursor-pointer"
                        style={{transformStyle: 'preserve-3d', backfaceVisibility: 'hidden'}}
                    >
                        {/* 卡牌背面背景 */}
                        <div className="absolute inset-0 bg-gradient-to-br from-[#8B9DC8] via-[#7C8CD6] to-[#A78BFA]"/>

                        {/* 内边框 */}
                        <div className="absolute inset-1 rounded-lg border-2 border-white/20"/>

                        {/* 装饰图案 */}
                        <CardBackPattern/>

                        {/* 中央图标 */}
                        <div className="absolute inset-0 flex items-center justify-center">
                            <div
                                className="w-16 h-16 rounded-full bg-white/20 backdrop-blur-sm border-2 border-white/30 flex items-center justify-center">
                                <Search className="w-7 h-7 text-white"/>
                            </div>
                        </div>

                        {/* 角落装饰 */}
                        <div
                            className="absolute top-3 left-3 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center">
                            <span className="text-white text-xs font-bold">?</span>
                        </div>
                        <div
                            className="absolute bottom-3 right-3 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center rotate-180">
                            <span className="text-white text-xs font-bold">?</span>
                        </div>
                    </motion.button>
                ) : (
                    <motion.div
                        key="revealed"
                        initial={{rotateY: 90, opacity: 0}}
                        animate={{rotateY: 0, opacity: 1}}
                        exit={{rotateY: -90, opacity: 0}}
                        transition={{duration: 0.4, ease: 'easeInOut'}}
                        whileHover={{
                            y: -2,
                            transition: {duration: 0.15}
                        }}
                        className={`absolute inset-0 w-full rounded-xl overflow-hidden flex flex-col shadow-sm ${isObserverMode ? 'cursor-default' : 'cursor-pointer'}`}
                        style={{transformStyle: 'preserve-3d', backfaceVisibility: 'hidden'}}
                    >
                        {/* 卡牌正面背景 */}
                        <div className="absolute inset-0 bg-white dark:bg-[#1A1D26]"/>

                        {/* 顶部渐变条 - 公开线索显示不同颜色 */}
                        <div className={`h-2 ${isPublic 
                            ? 'bg-gradient-to-r from-[#5DD9A8] via-[#4ECDC4] to-[#45B7AA]' 
                            : 'bg-gradient-to-r from-[#7C8CD6] via-[#A78BFA] to-[#F5A9C9]'}`}/>

                        {/* 卡牌内容 */}
                        <div className="relative flex-1 flex flex-col p-3">
                            {/* 顶部：类型标签 + 角标 */}
                            <div className="flex items-start justify-between mb-2">
                                <div className="flex items-center gap-1.5">
                                    <span className={`text-[9px] px-2 py-0.5 rounded-full font-bold uppercase tracking-wider ${
                                        isPublic 
                                            ? 'bg-[#5DD9A8]/10 text-[#5DD9A8]' 
                                            : 'bg-[#7C8CD6]/10 text-[#7C8CD6]'
                                    }`}>
                                        {clue.type}
                                    </span>
                                    {isPublic && (
                                        <span className="text-[8px] px-1.5 py-0.5 rounded-full bg-[#5DD9A8]/20 text-[#5DD9A8] flex items-center gap-0.5">
                                            <Globe className="w-2 h-2"/>
                                            公开
                                        </span>
                                    )}
                                    {isObserverMode && (
                                        <span className="text-[8px] px-1.5 py-0.5 rounded-full bg-[#7C8CD6]/20 text-[#7C8CD6] flex items-center gap-0.5">
                                            <Eye className="w-2 h-2"/>
                                            观察
                                        </span>
                                    )}
                                </div>
                                <div
                                    className="w-5 h-5 rounded-md bg-gradient-to-br from-[#7C8CD6]/20 to-[#A78BFA]/20 flex items-center justify-center">
                                    <FileText className="w-2.5 h-2.5 text-[#7C8CD6]"/>
                                </div>
                            </div>

                            {/* 中央图标区 */}
                            <div className="flex-1 flex items-center justify-center py-2">
                                <div
                                    className="w-12 h-12 rounded-xl bg-gradient-to-br from-[#7C8CD6]/10 to-[#A78BFA]/10 border border-[#7C8CD6]/20 flex items-center justify-center">
                                    <Sparkles className="w-6 h-6 text-[#7C8CD6]"/>
                                </div>
                            </div>

                            {/* 标题 */}
                            <h5 className="text-[#2D3748] dark:text-[#E8ECF2] font-bold text-xs leading-tight mb-1.5 text-center line-clamp-2">
                                {clue.name}
                            </h5>

                            {/* 描述 */}
                            <p className="text-[#5A6978] dark:text-[#9CA8B8] text-[10px] leading-relaxed text-center line-clamp-3">
                                {clue.description}
                            </p>
                        </div>

                        {/* 底部操作区 - 仅在非观察者模式且已揭示时显示 */}
                        {!isObserverMode && isRevealed && !isPublic && onPublic && (
                            <motion.button
                                onClick={(e) => {
                                    e.stopPropagation()
                                    onPublic()
                                }}
                                whileHover={{scale: 1.02}}
                                whileTap={{scale: 0.98}}
                                className="h-8 bg-gradient-to-t from-[#5DD9A8]/20 to-transparent flex items-center justify-center gap-1 text-[#5DD9A8] text-[10px] font-medium hover:from-[#5DD9A8]/30 transition-colors"
                            >
                                <Globe className="w-3 h-3"/>
                                公开线索
                            </motion.button>
                        )}

                        {/* 底部装饰 - 观察者模式或已公开时显示 */}
                        {(isObserverMode || isPublic) && (
                            <div className="h-6 bg-gradient-to-t from-[#EEF1F6]/80 to-transparent dark:from-[#2A2F3C]/80 flex items-center justify-center">
                                <div className="w-8 h-1 rounded-full bg-[#E0E5EE] dark:bg-[#363D4D]"/>
                            </div>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>
        </motion.div>
    )
})

ClueCard.displayName = 'ClueCard'

export default ClueCard

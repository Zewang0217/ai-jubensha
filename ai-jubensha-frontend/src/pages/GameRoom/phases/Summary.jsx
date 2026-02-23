/**
 * @fileoverview Summary 组件 - Film Noir 风格
 * @description 总结阶段，采用复古黑色电影美学
 */

import {memo} from 'react'
import {motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 案件结案印章
// =============================================================================

const CaseClosedStamp = memo(() => (
    <motion.div
        initial={{scale: 3, opacity: 0, rotate: -30}}
        animate={{scale: 1, opacity: 1, rotate: -12}}
        transition={{delay: 0.5, type: 'spring', stiffness: 200}}
        className="absolute top-4 right-4 border-4 border-red-800/60 text-red-800/60 px-6 py-3 font-black text-xl tracking-widest uppercase transform"
    >
        CASE CLOSED
    </motion.div>
))

CaseClosedStamp.displayName = 'CaseClosedStamp'

// =============================================================================
// 侦探报告卡片
// =============================================================================

const DetectiveReport = memo(({title, content, type = 'normal'}) => {
    const styles = {
        normal: 'border-stone-700 bg-stone-800/30',
        success: 'border-green-900/50 bg-green-950/20',
        danger: 'border-red-900/50 bg-red-950/20',
        warning: 'border-amber-900/50 bg-amber-950/20',
    }

    return (
        <div className={`border-2 p-4 ${styles[type]}`}>
            <h4 className="text-stone-500 text-xs uppercase tracking-widest mb-2">
                {title}
            </h4>
            <p className={`font-serif leading-relaxed ${
                type === 'danger' ? 'text-red-200/80' :
                    type === 'success' ? 'text-green-200/80' :
                        'text-stone-300'
            }`}>
                {content}
            </p>
        </div>
    )
})

DetectiveReport.displayName = 'DetectiveReport'

// =============================================================================
// 嫌疑人结果卡片
// =============================================================================

const SuspectResult = memo(({player, isCulprit, isCorrect, isSelf}) => {
    return (
        <div className={`
      flex items-center gap-4 p-4 border-2
      ${isCulprit
            ? 'border-red-900/50 bg-red-950/20'
            : isCorrect
                ? 'border-green-900/50 bg-green-950/10'
                : 'border-stone-800 bg-stone-900/30'
        }
    `}>
            <div className={`
        w-12 h-14 flex items-center justify-center font-serif text-xl border-2 flex-shrink-0
        ${isCulprit
                ? 'border-red-700 bg-red-950/40 text-red-400'
                : isCorrect
                    ? 'border-green-700 bg-green-950/30 text-green-400'
                    : 'border-stone-700 bg-stone-800 text-stone-500'
            }
      `}>
                {player.name.charAt(0)}
            </div>
            <div className="flex-1">
                <div className="flex items-center gap-2">
                    <h4 className={`font-serif ${isCulprit ? 'text-red-300' : 'text-stone-300'}`}>
                        {player.name}
                    </h4>
                    {isSelf && (
                        <span className="text-xs text-stone-500">(You)</span>
                    )}
                </div>
                <p className="text-stone-500 text-sm">{player.role}</p>
            </div>
            <div className="text-right">
                {isCulprit && (
                    <span className="text-red-500 font-bold text-sm">MURDERER</span>
                )}
                {isCorrect && (
                    <span className="text-green-500 text-sm">CORRECT</span>
                )}
            </div>
        </div>
    )
})

SuspectResult.displayName = 'SuspectResult'

// =============================================================================
// 主要组件
// =============================================================================

function Summary({_config, gameData, onAction}) {
    const result = gameData?.result || {
        isWin: true,
        correctVote: true,
        culprit: {id: 'p2', name: 'Dr. Su', role: 'Physician'},
        truth: `
      The murder was meticulously planned. Dr. Su had been slowly poisoning the victim for months, 
      building up a tolerance while the victim grew weaker. On that stormy night, when the lights went out, 
      the doctor struck with the antique letter opener—a weapon that had been in the family for generations.
      
      The locked room was an illusion. The secret passage, known only to the butler and Dr. Su, 
      provided the perfect escape. The forced window was a red herring, meant to suggest an outside intruder.
      
      But the doctor made one mistake: the poison vial in the kitchen trash. A careless moment 
      that unraveled the perfect crime.`,
        players: [
            {id: 'p1', name: 'Detective Lin', role: 'Investigator', isSelf: true, voteCorrect: true},
            {id: 'p2', name: 'Dr. Su', role: 'Physician', isCulprit: true},
            {id: 'p3', name: 'Butler Chen', role: 'Servant'},
            {id: 'p4', name: 'Attorney Zhao', role: 'Legal Counsel'},
        ],
    }

    const selfPlayer = result.players.find(p => p.isSelf)

    const handleReturnHome = () => {
        onAction?.('return_home')
    }

    const handlePlayAgain = () => {
        onAction?.('play_again')
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950 overflow-auto">
            {/* 结案横幅 */}
            <div className="relative p-6 border-b-2 border-stone-800">
                <CaseClosedStamp/>

                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    className="text-center"
                >
                    <h2 className="text-3xl font-serif text-amber-100 mb-2">
                        {result.isWin ? 'Justice Served' : 'The Killer Escaped'}
                    </h2>
                    <p className={`text-lg font-serif ${result.isWin ? 'text-green-400/80' : 'text-red-400/80'}`}>
                        {result.isWin
                            ? 'The murderer has been brought to justice.'
                            : 'The truth remains buried in the shadows.'}
                    </p>
                </motion.div>
            </div>

            {/* 主要内容 */}
            <div className="flex-1 p-6 space-y-6">
                {/* 案件结果 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <DetectiveReport
                        title="Case Resolution"
                        content={`${result.culprit.name}, the ${result.culprit.role}, was convicted of first-degree murder. The evidence was overwhelming.`}
                        type="danger"
                    />
                    <DetectiveReport
                        title="Your Performance"
                        content={selfPlayer?.voteCorrect
                            ? 'Your deduction was correct. You identified the killer and helped bring them to justice.'
                            : 'Your accusation was incorrect. But the truth eventually came to light.'}
                        type={selfPlayer?.voteCorrect ? 'success' : 'warning'}
                    />
                </div>

                {/* 真相大白 */}
                <motion.div
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    transition={{delay: 0.3}}
                    className="border-2 border-amber-700/30 bg-stone-900/50 p-6"
                >
                    <h3 className="text-amber-500/80 text-sm uppercase tracking-widest mb-4">
                        The Truth
                    </h3>
                    <div className="font-serif text-stone-300 leading-relaxed space-y-4 whitespace-pre-line">
                        {result.truth}
                    </div>
                </motion.div>

                {/* 嫌疑人结果 */}
                <div>
                    <h3 className="text-stone-500 text-sm uppercase tracking-widest mb-4">
                        Final Report
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {result.players.map((player) => (
                            <SuspectResult
                                key={player.id}
                                player={player}
                                isCulprit={player.isCulprit}
                                isCorrect={player.voteCorrect}
                                isSelf={player.isSelf}
                            />
                        ))}
                    </div>
                </div>

                {/* 操作按钮 */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.5}}
                    className="flex justify-center gap-4 pt-6 border-t border-stone-800"
                >
                    <button
                        onClick={handleReturnHome}
                        className="px-8 py-3 border-2 border-stone-700 text-stone-400 hover:text-stone-200 hover:border-stone-600 transition-colors font-serif"
                    >
                        Return to Office
                    </button>
                    <button
                        onClick={handlePlayAgain}
                        className="px-8 py-3 bg-amber-900/50 text-amber-400 border border-amber-700/30 hover:bg-amber-900/70 transition-colors font-serif"
                    >
                        New Case
                    </button>
                </motion.div>
            </div>

            {/* 装饰性印章 */}
            <div className="absolute bottom-4 right-4 opacity-5 pointer-events-none">
                <div
                    className="w-32 h-32 border-4 border-stone-400 rounded-full flex items-center justify-center transform rotate-12">
                    <span className="text-stone-400 font-black text-xs tracking-widest">TOP SECRET</span>
                </div>
            </div>
        </div>
    )
}

Summary.displayName = 'Summary'
Summary.phaseType = PHASE_TYPE.SUMMARY

export default memo(Summary)
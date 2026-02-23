/**
 * @fileoverview Investigation 组件 - Film Noir 风格
 * @description 搜证阶段，采用复古黑色电影美学
 */

import {memo, useCallback, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 证据袋组件
// =============================================================================

const EvidenceBag = memo(({clue, isRevealed, onReveal, index}) => {
    return (
        <motion.div
            initial={{opacity: 0, scale: 0.95}}
            animate={{opacity: 1, scale: 1}}
            transition={{delay: index * 0.1}}
            className="relative"
        >
            {!isRevealed ? (
                <button
                    onClick={onReveal}
                    className="w-full aspect-square border-2 border-dashed border-stone-600 hover:border-amber-600/50 bg-stone-800/20 hover:bg-stone-800/40 transition-all flex flex-col items-center justify-center group"
                >
                    <div
                        className="w-12 h-12 rounded-full border-2 border-stone-600 group-hover:border-amber-600/50 flex items-center justify-center mb-2 transition-colors">
                        <span className="text-stone-500 group-hover:text-amber-500/70 text-xl">?</span>
                    </div>
                    <span className="text-stone-600 group-hover:text-stone-400 text-xs uppercase tracking-widest">
            Unexamined
          </span>
                </button>
            ) : (
                <div className="w-full aspect-square border border-amber-700/30 bg-amber-950/10 p-3 overflow-hidden">
                    <div className="flex flex-col h-full">
                        <div className="flex items-center gap-2 mb-2">
                            <div
                                className="w-8 h-8 bg-amber-900/30 border border-amber-700/30 flex items-center justify-center flex-shrink-0">
                                <span className="text-amber-600 text-sm">📋</span>
                            </div>
                            <h5 className="text-amber-100 font-serif text-sm leading-tight line-clamp-2 flex-1">{clue.name}</h5>
                        </div>
                        <p className="text-stone-400 text-xs leading-relaxed line-clamp-3 flex-1">{clue.description}</p>
                        <span
                            className="inline-block mt-auto pt-2 px-2 py-0.5 bg-stone-800 text-stone-500 text-xs border border-stone-700 w-fit">
              {clue.type}
            </span>
                    </div>
                </div>
            )}
        </motion.div>
    )
})

EvidenceBag.displayName = 'EvidenceBag'

// =============================================================================
// 现场照片组件 - 场景选择器
// =============================================================================

const CrimeScenePhoto = memo(({scene, isSelected, isLocked, onClick, clueCount}) => {
    return (
        <motion.button
            whileHover={!isLocked ? {scale: 1.02} : {}}
            whileTap={!isLocked ? {scale: 0.98} : {}}
            onClick={onClick}
            disabled={isLocked}
            className={`
        relative w-full aspect-[4/3] border-2 overflow-hidden transition-all
        ${isSelected
                ? 'border-amber-600 ring-2 ring-amber-600/20'
                : isLocked
                    ? 'border-stone-700 opacity-50'
                    : 'border-stone-600 hover:border-stone-500'
            }
      `}
        >
            {/* 照片背景 - 使用渐变代替图片 */}
            <div className={`
        absolute inset-0
        ${isSelected
                ? 'bg-gradient-to-br from-amber-950/50 to-stone-900'
                : 'bg-gradient-to-br from-stone-800/50 to-stone-900'
            }
      `}/>

            {/* 锁定图标 */}
            {isLocked && (
                <div className="absolute inset-0 flex items-center justify-center bg-stone-950/60">
                    <div className="w-12 h-12 rounded-full border-2 border-stone-600 flex items-center justify-center">
                        <span className="text-stone-600 text-xl">🔒</span>
                    </div>
                </div>
            )}

            {/* 内容 */}
            <div className="relative h-full flex flex-col justify-between p-3">
                <div className="flex justify-between items-start">
          <span className={`
            text-xs font-mono
            ${isSelected ? 'text-amber-500' : 'text-stone-500'}
          `}>
            {isSelected ? '● ACTIVE' : isLocked ? '● LOCKED' : '○ AVAILABLE'}
          </span>
                    {!isLocked && clueCount > 0 && (
                        <span className="px-2 py-0.5 bg-amber-900/50 text-amber-500 text-xs border border-amber-700/30">
              {clueCount} ITEMS
            </span>
                    )}
                </div>

                <div>
                    <h4 className={`font-serif text-lg ${isSelected ? 'text-amber-100' : 'text-stone-300'}`}>
                        {scene.name}
                    </h4>
                    <p className="text-stone-500 text-xs mt-1 line-clamp-2">{scene.description}</p>
                </div>
            </div>

            {/* 照片角标 */}
            <div className="absolute top-2 right-2 w-3 h-3 border-t-2 border-r-2 border-white/20"/>
            <div className="absolute bottom-2 left-2 w-3 h-3 border-b-2 border-l-2 border-white/20"/>
        </motion.button>
    )
})

CrimeScenePhoto.displayName = 'CrimeScenePhoto'

// =============================================================================
// 主要组件
// =============================================================================

function Investigation({_config, gameData, onComplete, onAction}) {
    const [selectedScene, setSelectedScene] = useState(null)
    const [revealedClues, setRevealedClues] = useState(new Set())

    const scenes = gameData?.scenes || [
        {
            id: 'scene-001',
            name: 'The Study',
            description: 'Where the body was found. Locked from the inside.',
            isLocked: false,
            clueCount: 3,
            clues: [
                {
                    id: 'c1',
                    name: 'Bloody Letter Opener',
                    description: 'The murder weapon. Antique silver, monogrammed with the victim\'s initials. But whose fingerprints are on the handle?',
                    type: 'Weapon'
                },
                {
                    id: 'c2',
                    name: 'Spilled Tea',
                    description: 'A cup of Earl Grey, knocked over. The stain pattern suggests a struggle.',
                    type: 'Physical'
                },
                {
                    id: 'c3',
                    name: 'Draft Will',
                    description: 'Unsigned. Changes everything. Someone was about to lose everything.',
                    type: 'Document'
                },
            ],
        },
        {
            id: 'scene-002',
            name: 'Drawing Room',
            description: 'Where the guests waited. Nerves were high.',
            isLocked: false,
            clueCount: 2,
            clues: [
                {
                    id: 'c4',
                    name: 'Power Cut Record',
                    description: 'Precisely 8:15 PM. Lasted exactly 5 minutes. Long enough to kill.',
                    type: 'Timeline'
                },
                {
                    id: 'c5',
                    name: 'Window Latch',
                    description: 'Forced from outside. But this window faces a cliff. Impossible escape.',
                    type: 'Contradiction'
                },
            ],
        },
        {
            id: 'scene-003',
            name: 'Kitchen',
            description: 'Poison was prepared here.',
            isLocked: false,
            clueCount: 2,
            clues: [
                {
                    id: 'c6',
                    name: 'Empty Vial',
                    description: 'Medical-grade arsenic. Prescription label removed. Professional knowledge required.',
                    type: 'Poison'
                },
                {
                    id: 'c7',
                    name: 'Crumpled Note',
                    description: '"Meet me in the study at 8. We need to talk. - Anonymous"',
                    type: 'Message'
                },
            ],
        },
        {
            id: 'scene-004',
            name: 'Master Bedroom',
            description: 'The victim\'s private quarters. Secrets hidden here.',
            isLocked: false,
            clueCount: 2,
            clues: [
                {
                    id: 'c8',
                    name: 'Personal Diary',
                    description: 'Last entry: "I know who betrayed me. Tonight, everyone will know the truth."',
                    type: 'Evidence'
                },
                {
                    id: 'c9',
                    name: 'Safe',
                    description: 'Locked. Combination unknown. Something valuable inside?',
                    type: 'Mystery'
                },
            ],
        },
        {
            id: 'scene-005',
            name: 'Basement',
            description: 'Storage and secrets. Door requires special access.',
            isLocked: true,
            clueCount: 0,
            clues: [],
        },
    ]

    const currentScene = scenes.find(s => s.id === selectedScene)
    const totalClues = scenes.reduce((acc, s) => acc + s.clues.length, 0)
    const progress = Math.round((revealedClues.size / totalClues) * 100)

    const handleRevealClue = useCallback((clueId) => {
        setRevealedClues(prev => new Set([...prev, clueId]))
        onAction?.('clue_revealed', {clueId, sceneId: selectedScene})
    }, [selectedScene, onAction])

    const handleComplete = () => {
        onAction?.('investigation_complete', {
            revealedClues: Array.from(revealedClues),
            totalClues,
        })
        onComplete?.()
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">
            {/* 顶部标题栏 */}
            <div className="flex items-center justify-between mb-4 px-2 pb-4 border-b border-stone-800">
                <div>
                    <h2 className="text-xl font-serif text-amber-100">
                        Crime Scene Investigation
                    </h2>
                    <p className="text-stone-500 text-xs mt-1">
                        {revealedClues.size} OF {totalClues} EVIDENCE COLLECTED
                    </p>
                </div>

                <div className="flex items-center gap-4">
                    {/* 进度条 */}
                    <div className="w-32 h-2 bg-stone-800 rounded-full overflow-hidden">
                        <motion.div
                            className="h-full bg-gradient-to-r from-amber-700 to-amber-500"
                            initial={{width: 0}}
                            animate={{width: `${progress}%`}}
                            transition={{duration: 0.5}}
                        />
                    </div>

                    <button
                        onClick={handleComplete}
                        className="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-stone-300 text-sm transition-colors border border-stone-700"
                    >
                        Close Investigation
                    </button>
                </div>
            </div>

            {/* 主内容区 */}
            <div className="flex-1 flex gap-4 min-h-0">
                {/* 场景列表 */}
                <div className="w-64 flex-none overflow-y-auto pr-2">
                    <p className="text-stone-600 text-xs uppercase tracking-widest mb-3">
                        Crime Scenes
                    </p>
                    <div className="grid grid-cols-1 gap-3">
                        {scenes.map((scene) => (
                            <CrimeScenePhoto
                                key={scene.id}
                                scene={scene}
                                isSelected={selectedScene === scene.id}
                                isLocked={scene.isLocked}
                                onClick={() => !scene.isLocked && setSelectedScene(scene.id)}
                                clueCount={scene.clueCount}
                            />
                        ))}
                    </div>
                </div>

                {/* 证据详情 */}
                <div className="flex-1 min-w-0">
                    <AnimatePresence mode="wait">
                        {currentScene ? (
                            <motion.div
                                key={currentScene.id}
                                initial={{opacity: 0, x: 20}}
                                animate={{opacity: 1, x: 0}}
                                exit={{opacity: 0, x: -20}}
                                transition={{duration: 0.3}}
                                className="h-full flex flex-col"
                            >
                                {/* 场景标题 */}
                                <div className="mb-4 p-4 border border-stone-700/50 bg-stone-800/20">
                                    <h3 className="text-2xl font-serif text-amber-100">{currentScene.name}</h3>
                                    <p className="text-stone-400 text-sm mt-1">{currentScene.description}</p>
                                </div>

                                {/* 证据网格 */}
                                <div className="flex-1 overflow-y-auto">
                                    <p className="text-stone-600 text-xs uppercase tracking-widest mb-3">
                                        Evidence Log
                                    </p>
                                    <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
                                        {currentScene.clues.map((clue, index) => (
                                            <EvidenceBag
                                                key={clue.id}
                                                clue={clue}
                                                isRevealed={revealedClues.has(clue.id)}
                                                onReveal={() => handleRevealClue(clue.id)}
                                                index={index}
                                            />
                                        ))}
                                    </div>
                                </div>
                            </motion.div>
                        ) : (
                            <motion.div
                                initial={{opacity: 0}}
                                animate={{opacity: 1}}
                                className="h-full flex items-center justify-center text-stone-600"
                            >
                                <div className="text-center">
                                    <div
                                        className="w-16 h-16 mx-auto mb-4 border-2 border-stone-700 rounded-full flex items-center justify-center">
                                        <span className="text-2xl">🔍</span>
                                    </div>
                                    <p className="font-serif">Select a crime scene to begin</p>
                                </div>
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            </div>

            {/* 底部胶片条装饰 - 相对定位 */}
            <div
                className="mt-4 h-6 bg-stone-900 border-t border-stone-800 flex justify-between px-4 items-center flex-shrink-0">
                {Array.from({length: 20}).map((_, i) => (
                    <div key={i} className="w-2 h-3 bg-stone-800 rounded-sm"/>
                ))}
            </div>
        </div>
    )
}

Investigation.displayName = 'Investigation'
Investigation.phaseType = PHASE_TYPE.INVESTIGATION

export default memo(Investigation)
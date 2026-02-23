/**
 * @fileoverview CharacterAssignment 组件 - Film Noir 风格
 * @description 角色分配阶段，采用复古黑色电影美学
 */

import {memo, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 机密印章组件
// =============================================================================

const ConfidentialStamp = memo(() => (
    <div className="absolute -top-2 -right-2 transform rotate-12">
        <div
            className="border-4 border-red-700/80 text-red-700/80 px-4 py-2 font-black text-sm tracking-widest uppercase">
            Confidential
        </div>
    </div>
))

ConfidentialStamp.displayName = 'ConfidentialStamp'

// =============================================================================
// 角色档案卡片
// =============================================================================

const CharacterFile = memo(({character, isPlayer, isRevealed, onReveal, index}) => {
    return (
        <motion.div
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            transition={{delay: index * 0.15}}
            className={`relative ${isPlayer ? 'col-span-2' : ''}`}
        >
            <div
                className={`
          relative border-2 p-6 h-full
          ${isPlayer
                    ? 'border-amber-700/50 bg-gradient-to-br from-amber-950/50 to-stone-900/90'
                    : 'border-stone-700/50 bg-stone-900/60'
                }
        `}
            >
                {isPlayer && <ConfidentialStamp/>}

                {/* 档案头部 */}
                <div className="flex items-start gap-4 mb-4">
                    {/* 头像占位 - 使用几何图形代替 */}
                    <div
                        className={`
              w-16 h-20 flex-shrink-0 flex items-center justify-center border-2
              ${isPlayer
                            ? 'border-amber-600/50 bg-amber-950/30'
                            : 'border-stone-600/50 bg-stone-800/30'
                        }
            `}
                    >
            <span className="text-2xl font-serif text-stone-500">
              {character.name.charAt(0)}
            </span>
                    </div>

                    <div className="flex-1 min-w-0">
                        <h3 className={`font-serif text-xl ${isPlayer ? 'text-amber-100' : 'text-stone-300'}`}>
                            {character.name}
                        </h3>
                        <p className="text-stone-500 text-sm mt-1">
                            {isPlayer ? 'YOUR IDENTITY' : 'SUSPECT'}
                        </p>
                        <div className="flex gap-2 mt-2">
              <span className="text-xs bg-stone-800 text-stone-400 px-2 py-0.5 border border-stone-700">
                ID: {character.id?.slice(-4) || '0001'}
              </span>
                        </div>
                    </div>
                </div>

                {/* 档案描述 */}
                <p className="text-stone-400 text-sm leading-relaxed font-serif mb-4">
                    {character.description}
                </p>

                {/* 机密信息 - 仅玩家可见 */}
                {isPlayer && (
                    <div className="border-t border-amber-700/30 pt-4">
                        <AnimatePresence mode="wait">
                            {!isRevealed ? (
                                <motion.button
                                    key="sealed"
                                    initial={{opacity: 0}}
                                    animate={{opacity: 1}}
                                    exit={{opacity: 0}}
                                    onClick={onReveal}
                                    className="w-full py-4 border-2 border-dashed border-amber-700/50 text-amber-600/70 hover:border-amber-600 hover:text-amber-500 transition-colors"
                                >
                  <span className="font-serif tracking-widest uppercase text-sm">
                    ⚠ Break Seal to View Confidential File
                  </span>
                                </motion.button>
                            ) : (
                                <motion.div
                                    key="revealed"
                                    initial={{opacity: 0, height: 0}}
                                    animate={{opacity: 1, height: 'auto'}}
                                    exit={{opacity: 0, height: 0}}
                                    className="space-y-4 bg-stone-950/50 p-4 border border-amber-700/20"
                                >
                                    {/* 背景故事 */}
                                    <div>
                                        <h4 className="text-amber-600/80 text-xs uppercase tracking-widest mb-2">
                                            Background
                                        </h4>
                                        <p className="text-stone-300 text-sm font-serif leading-relaxed">
                                            {character.background}
                                        </p>
                                    </div>

                                    {/* 任务 */}
                                    <div className="border-t border-stone-800 pt-3">
                                        <h4 className="text-amber-600/80 text-xs uppercase tracking-widest mb-2">
                                            Mission
                                        </h4>
                                        <p className="text-stone-300 text-sm font-serif leading-relaxed">
                                            {character.goal}
                                        </p>
                                    </div>

                                    {/* 秘密 - 红色警示 */}
                                    <div className="border-t border-red-900/30 pt-3 bg-red-950/10 p-3">
                                        <h4 className="text-red-500/80 text-xs uppercase tracking-widest mb-2">
                                            ⚠ Secret - Do Not Disclose
                                        </h4>
                                        <p className="text-red-200/70 text-sm font-serif leading-relaxed">
                                            {character.secret}
                                        </p>
                                    </div>

                                    <button
                                        onClick={onReveal}
                                        className="text-stone-500 text-xs hover:text-stone-400 transition-colors"
                                    >
                                        [ Hide File ]
                                    </button>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                )}

                {/* 档案编号 */}
                <div className="absolute bottom-2 right-2 text-stone-700 text-xs font-mono">
                    #{String(index + 1).padStart(3, '0')}
                </div>
            </div>
        </motion.div>
    )
})

CharacterFile.displayName = 'CharacterFile'

// =============================================================================
// 主要组件
// =============================================================================

function CharacterAssignment({_config, gameData, _playerData, onComplete, onAction}) {
    const [isRevealed, setIsRevealed] = useState(false)

    const characters = gameData?.characters || [
        {
            id: 'char-001',
            name: 'Detective Lin',
            description: 'A retired police detective with a keen eye for detail. Your experience makes you the natural leader of this investigation.',
            background: 'Ten years on the force, countless cases solved. But this one feels different. The victim was someone you once knew.',
            goal: 'Find the killer. Uncover the truth. But keep your past connection secret.',
            secret: 'You investigated the victim five years ago for fraud. The case was dropped, but you never forgot.',
            isPlayer: true,
        },
        {
            id: 'char-002',
            name: 'Dr. Su',
            description: 'The manor\'s personal physician. Soft-spoken and always watching.',
            background: 'Five years of service. You know the victim\'s medical history better than anyone.',
            goal: 'Protect your patient\'s privacy. And your own secrets.',
            secret: 'The victim was being poisoned slowly. You knew, but said nothing.',
            isPlayer: false,
        },
        {
            id: 'char-003',
            name: 'Butler Chen',
            description: 'Fifteen years of service. The manor has no secrets from you.',
            background: 'You manage everything. See everything. Know where the bodies are buried—literally.',
            goal: 'Maintain order. Protect the family legacy.',
            secret: 'You know about the secret passages. You were in the study that night.',
            isPlayer: false,
        },
        {
            id: 'char-004',
            name: 'Attorney Zhao',
            description: 'The victim\'s legal counsel. You know what the will says.',
            background: 'Drafted the final will three days ago. Someone stands to gain everything.',
            goal: 'Ensure the will is executed properly.',
            secret: 'The victim changed the will at the last minute. You were the only witness.',
            isPlayer: false,
        },
    ]

    const playerCharacter = characters.find(c => c.isPlayer)
    const otherCharacters = characters.filter(c => !c.isPlayer)

    const handleContinue = () => {
        onAction?.('character_assignment_complete', {
            characterId: playerCharacter?.id,
            revealed: isRevealed,
        })
        onComplete?.()
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">
            {/* 顶部标题 */}
            <div className="flex items-center justify-between mb-6 px-2">
                <div>
                    <h2 className="text-2xl font-serif text-amber-100 tracking-wide">
                        Personnel Files
                    </h2>
                    <p className="text-stone-500 text-sm mt-1">
                        CLASSIFIED - EYES ONLY
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-2 h-2 bg-red-600 rounded-full animate-pulse"/>
                    <span className="text-red-500/80 text-xs uppercase tracking-widest">
            Top Secret
          </span>
                </div>
            </div>

            {/* 角色档案网格 */}
            <div className="flex-1 overflow-auto">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pb-20">
                    {/* 玩家角色 - 优先显示 */}
                    {playerCharacter && (
                        <CharacterFile
                            character={playerCharacter}
                            isPlayer={true}
                            isRevealed={isRevealed}
                            onReveal={() => setIsRevealed(!isRevealed)}
                            index={0}
                        />
                    )}

                    {/* 其他角色 */}
                    {otherCharacters.map((character, index) => (
                        <CharacterFile
                            key={character.id}
                            character={character}
                            isPlayer={false}
                            isRevealed={false}
                            onReveal={() => {
                            }}
                            index={index + 1}
                        />
                    ))}
                </div>
            </div>

            {/* 底部操作栏 */}
            <div
                className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-stone-950 via-stone-950/95 to-transparent">
                <div className="flex justify-between items-center max-w-4xl mx-auto">
                    <p className="text-stone-500 text-sm">
                        {!isRevealed && playerCharacter && (
                            <span className="text-amber-600/80">
                ⚠ Access your confidential file to proceed
              </span>
                        )}
                    </p>

                    <button
                        onClick={handleContinue}
                        disabled={!isRevealed}
                        className={`
              px-6 py-3 font-serif tracking-widest uppercase transition-all
              ${isRevealed
                            ? 'bg-amber-700 text-stone-100 hover:bg-amber-600'
                            : 'bg-stone-800 text-stone-600 cursor-not-allowed'
                        }
            `}
                    >
                        Confirm Identity
                    </button>
                </div>
            </div>

            {/* 背景纹理 */}
            <div
                className="absolute inset-0 pointer-events-none opacity-[0.02]"
                style={{
                    backgroundImage: `repeating-linear-gradient(
            0deg,
            transparent,
            transparent 2px,
            rgba(255,255,255,0.03) 2px,
            rgba(255,255,255,0.03) 4px
          )`,
                }}
            />
        </div>
    )
}

CharacterAssignment.displayName = 'CharacterAssignment'
CharacterAssignment.phaseType = PHASE_TYPE.CHARACTER_ASSIGNMENT

export default memo(CharacterAssignment)
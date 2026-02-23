/**
 * @fileoverview Discussion 组件 - Film Noir 风格
 * @description 讨论投票阶段，采用复古黑色电影美学
 */

import {memo, useCallback, useEffect, useRef, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {PHASE_TYPE} from '../types'

// =============================================================================
// 电报风格消息
// =============================================================================

const TelegraphMessage = memo(({message, isSelf}) => {
    const isSystem = message.isSystem

    return (
        <motion.div
            initial={{opacity: 0, y: 10}}
            animate={{opacity: 1, y: 0}}
            className={`flex gap-3 ${isSelf ? 'flex-row-reverse' : ''}`}
        >
            {!isSystem && (
                <div
                    className={`
            w-10 h-10 flex-shrink-0 flex items-center justify-center font-serif text-lg
            ${isSelf
                        ? 'bg-amber-900/50 text-amber-400 border border-amber-700/50'
                        : message.isAI
                            ? 'bg-stone-800 text-stone-400 border border-stone-700'
                            : 'bg-red-950/30 text-red-400 border border-red-900/30'
                    }
          `}
                >
                    {message.sender.charAt(0)}
                </div>
            )}

            <div className={`max-w-[70%] ${isSelf ? 'text-right' : ''}`}>
                {!isSystem && (
                    <p className="text-stone-600 text-xs mb-1 font-mono">
                        {message.sender.toUpperCase()} • {message.time}
                    </p>
                )}
                <div
                    className={`
            inline-block px-4 py-2 text-sm font-serif
            ${isSystem
                        ? 'bg-stone-800/50 text-amber-500/80 border border-amber-700/30 w-full text-center'
                        : isSelf
                            ? 'bg-amber-900/20 text-amber-100 border border-amber-700/30'
                            : 'bg-stone-800/50 text-stone-300 border border-stone-700'
                    }
          `}
                >
                    {message.content}
                </div>
            </div>
        </motion.div>
    )
})

TelegraphMessage.displayName = 'TelegraphMessage'

// =============================================================================
// 嫌疑人档案卡片
// =============================================================================

const SuspectCard = memo(({player, isSelected, hasVoted, onSelect}) => {
    return (
        <motion.button
            whileHover={!hasVoted ? {scale: 1.02} : {}}
            whileTap={!hasVoted ? {scale: 0.98} : {}}
            onClick={() => !hasVoted && onSelect(player.id)}
            disabled={hasVoted}
            className={`
        w-full p-4 border-2 transition-all text-left
        ${isSelected
                ? 'border-red-700 bg-red-950/20'
                : hasVoted
                    ? 'border-stone-800 bg-stone-900/30 opacity-50'
                    : 'border-stone-700 bg-stone-800/30 hover:border-stone-600'
            }
      `}
        >
            <div className="flex items-center gap-4">
                <div
                    className={`
            w-14 h-16 flex items-center justify-center font-serif text-xl border-2
            ${isSelected
                        ? 'border-red-700 bg-red-950/30 text-red-400'
                        : 'border-stone-700 bg-stone-800 text-stone-500'
                    }
          `}
                >
                    {player.name.charAt(0)}
                </div>
                <div className="flex-1">
                    <h4 className={`font-serif text-lg ${isSelected ? 'text-red-300' : 'text-stone-300'}`}>
                        {player.name}
                    </h4>
                    <p className="text-stone-500 text-sm">{player.role}</p>
                </div>
                {isSelected && (
                    <div className="w-8 h-8 rounded-full border-2 border-red-700 flex items-center justify-center">
                        <span className="text-red-500">✓</span>
                    </div>
                )}
            </div>
        </motion.button>
    )
})

SuspectCard.displayName = 'SuspectCard'

// =============================================================================
// 主要组件
// =============================================================================

function Discussion({_config, gameData, onComplete, onAction}) {
    const [messages, setMessages] = useState([])
    const [inputText, setInputText] = useState('')
    const [selectedTarget, setSelectedTarget] = useState(null)
    const [hasVoted, setHasVoted] = useState(false)
    const [activeTab, setActiveTab] = useState('interrogation')
    const messagesEndRef = useRef(null)

    const players = gameData?.players || [
        {id: 'p1', name: 'Detective Lin', role: 'Investigator', isSelf: true},
        {id: 'p2', name: 'Dr. Su', role: 'Physician', isAI: true},
        {id: 'p3', name: 'Butler Chen', role: 'Servant', isAI: true},
        {id: 'p4', name: 'Attorney Zhao', role: 'Legal Counsel', isAI: true},
    ]

    const otherPlayers = players.filter(p => !p.isSelf)

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({behavior: 'smooth'})
    }, [messages])

    const handleSendMessage = useCallback(() => {
        if (!inputText.trim()) return

        const newMessage = {
            id: Date.now().toString(),
            sender: 'You',
            content: inputText.trim(),
            time: new Date().toLocaleTimeString('en-US', {hour: '2-digit', minute: '2-digit', hour12: false}),
            isAI: false,
        }

        setMessages(prev => [...prev, newMessage])
        setInputText('')
        onAction?.('chat_message', {message: newMessage})

        // 模拟 AI 回复
        setTimeout(() => {
            const aiResponses = [
                "Interesting observation. But can you prove it?",
                "I was in the kitchen when it happened. Ask the butler.",
                "The timeline doesn't add up. Someone is lying.",
                "We need more evidence before we accuse anyone.",
            ]
            const aiMsg = {
                id: `ai-${Date.now()}`,
                sender: otherPlayers[Math.floor(Math.random() * otherPlayers.length)].name,
                content: aiResponses[Math.floor(Math.random() * aiResponses.length)],
                time: new Date().toLocaleTimeString('en-US', {hour: '2-digit', minute: '2-digit', hour12: false}),
                isAI: true,
            }
            setMessages(prev => [...prev, aiMsg])
        }, 2000)
    }, [inputText, onAction, otherPlayers])

    const handleVote = useCallback(() => {
        if (!selectedTarget) return
        setHasVoted(true)
        onAction?.('vote_cast', {targetId: selectedTarget})
    }, [selectedTarget, onAction])

    const handleComplete = () => {
        onAction?.('discussion_complete', {messageCount: messages.length, hasVoted})
        onComplete?.()
    }

    return (
        <div className="h-full flex flex-col bg-gradient-to-b from-stone-950 via-stone-900 to-stone-950">
            {/* 顶部标题栏 */}
            <div className="flex items-center justify-between mb-4 px-2 pb-4 border-b border-stone-800">
                <div>
                    <h2 className="text-xl font-serif text-amber-100">
                        Interrogation Room
                    </h2>
                    <p className="text-stone-500 text-xs mt-1">
                        {hasVoted ? 'VOTE RECORDED' : 'AWAITING YOUR TESTIMONY'}
                    </p>
                </div>

                {/* 标签切换 */}
                <div className="flex bg-stone-800/50 border border-stone-700">
                    <button
                        onClick={() => setActiveTab('interrogation')}
                        className={`
              px-4 py-2 text-sm font-serif transition-colors
              ${activeTab === 'interrogation'
                            ? 'bg-stone-700 text-amber-100'
                            : 'text-stone-400 hover:text-stone-200'
                        }
            `}
                    >
                        Testimony
                    </button>
                    <button
                        onClick={() => setActiveTab('accusation')}
                        className={`
              px-4 py-2 text-sm font-serif transition-colors
              ${activeTab === 'accusation'
                            ? 'bg-red-900/30 text-red-300 border-l border-red-700/30'
                            : 'text-stone-400 hover:text-stone-200 border-l border-stone-700'
                        }
            `}
                    >
                        Accusation {hasVoted && '✓'}
                    </button>
                </div>
            </div>

            {/* 主内容区 */}
            <div className="flex-1 min-h-0 border border-stone-800 bg-stone-900/30">
                <AnimatePresence mode="wait">
                    {activeTab === 'interrogation' ? (
                        <motion.div
                            key="chat"
                            initial={{opacity: 0}}
                            animate={{opacity: 1}}
                            exit={{opacity: 0}}
                            className="h-full flex flex-col"
                        >
                            {/* 消息列表 */}
                            <div className="flex-1 overflow-y-auto p-4 space-y-4">
                                {messages.length === 0 ? (
                                    <div className="h-full flex flex-col items-center justify-center text-stone-600">
                                        <div
                                            className="w-16 h-16 border-2 border-stone-700 rounded-full flex items-center justify-center mb-4">
                                            <span className="text-2xl">📡</span>
                                        </div>
                                        <p className="font-serif">The interrogation begins...</p>
                                        <p className="text-sm mt-2">Share your deductions</p>
                                    </div>
                                ) : (
                                    messages.map((msg) => (
                                        <TelegraphMessage
                                            key={msg.id}
                                            message={msg}
                                            isSelf={!msg.isAI}
                                        />
                                    ))
                                )}
                                <div ref={messagesEndRef}/>
                            </div>

                            {/* 输入框 */}
                            <div className="p-4 border-t border-stone-800 bg-stone-900/50">
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        value={inputText}
                                        onChange={(e) => setInputText(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
                                        placeholder="Transmit your deductions..."
                                        className="flex-1 px-4 py-2 bg-stone-800/50 border border-stone-700 text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-700/50 font-serif"
                                    />
                                    <button
                                        onClick={handleSendMessage}
                                        disabled={!inputText.trim()}
                                        className="px-4 py-2 bg-amber-900/50 text-amber-400 border border-amber-700/30 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-amber-900/70 transition-colors"
                                    >
                                        SEND
                                    </button>
                                </div>
                            </div>
                        </motion.div>
                    ) : (
                        <motion.div
                            key="vote"
                            initial={{opacity: 0}}
                            animate={{opacity: 1}}
                            exit={{opacity: 0}}
                            className="h-full flex flex-col p-4"
                        >
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-serif text-red-300">
                                    Cast Your Accusation
                                </h3>
                                {hasVoted && (
                                    <span
                                        className="px-3 py-1 bg-red-900/20 text-red-400 text-sm border border-red-700/30">
                    VOTE SEALED
                  </span>
                                )}
                            </div>

                            <div className="flex-1 overflow-y-auto space-y-3">
                                {otherPlayers.map((player) => (
                                    <SuspectCard
                                        key={player.id}
                                        player={player}
                                        isSelected={selectedTarget === player.id}
                                        hasVoted={hasVoted}
                                        onSelect={setSelectedTarget}
                                    />
                                ))}
                            </div>

                            {!hasVoted && (
                                <div className="mt-4 pt-4 border-t border-stone-800">
                                    <button
                                        onClick={handleVote}
                                        disabled={!selectedTarget}
                                        className={`
                      w-full py-3 font-serif tracking-widest uppercase transition-all
                      ${selectedTarget
                                            ? 'bg-red-900/50 text-red-200 border border-red-700/50 hover:bg-red-900/70'
                                            : 'bg-stone-800 text-stone-600 cursor-not-allowed'
                                        }
                    `}
                                    >
                                        {selectedTarget ? 'Seal Accusation' : 'Select a Suspect'}
                                    </button>
                                </div>
                            )}
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>

            {/* 底部状态 */}
            <div className="flex justify-between items-center mt-4 pt-4 border-t border-stone-800">
        <span className="text-stone-600 text-sm">
          {messages.length} transmissions
        </span>
                <button
                    onClick={handleComplete}
                    className="px-6 py-2 bg-stone-800 hover:bg-stone-700 text-stone-300 transition-colors border border-stone-700"
                >
                    Close Investigation
                </button>
            </div>
        </div>
    )
}

Discussion.displayName = 'Discussion'
Discussion.phaseType = PHASE_TYPE.DISCUSSION

export default memo(Discussion)
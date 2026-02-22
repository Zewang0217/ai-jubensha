import {motion} from 'framer-motion'
import {Clock, MessageSquare, Send, Users} from 'lucide-react'
import ClueList from './ClueList'
import {privateClues, publicClues} from '../data/mockClues'

/**
 * 讨论阶段组件
 * 四栏布局：公开线索 | 玩家列表 | 聊天区 | 私人线索
 */
function PhaseDiscussion({gameData}) {
    // Mock 玩家数据
    const mockPlayers = [
        {id: 1, name: '侦探A', characterName: '私家侦探', avatar: 'A'},
        {id: 2, name: '管家B', characterName: '宅邸管家', avatar: 'B'},
        {id: 3, name: '女仆C', characterName: '女仆长', avatar: 'C'},
        {id: 4, name: '律师D', characterName: '家族律师', avatar: 'D'},
    ]

    const players = gameData?.players || mockPlayers

    return (
        <div className="h-full flex flex-col p-5">
            {/* 阶段标题 */}
            <motion.div
                initial={{opacity: 0, y: -10}}
                animate={{opacity: 1, y: 0}}
                className="flex-none text-center mb-4"
            >
                <h2 className="text-2xl font-bold text-white mb-1">讨论阶段</h2>
                <p className="text-slate-400 text-sm">分享推理，查看线索，找出真相</p>
            </motion.div>

            {/* 主内容区 - 四栏布局 */}
            <div className="flex-1 min-h-0 flex gap-4">
                {/* 左侧：公开线索 */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="w-56 flex-none"
                >
                    <ClueList
                        clues={publicClues}
                        title="公开线索"
                        isPrivate={false}
                        emptyText="暂无公开线索"
                    />
                </motion.div>

                {/* 左中：在线玩家 */}
                <motion.div
                    initial={{opacity: 0, x: -10}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.15}}
                    className="w-44 flex-none rounded-xl bg-slate-800/50 border border-slate-700/50 flex flex-col overflow-hidden"
                >
                    <div
                        className="flex-none flex items-center gap-2 px-4 py-3 bg-gradient-to-r from-purple-500/20 to-pink-500/20 border-b border-slate-700/50">
                        <Users className="w-4 h-4 text-purple-400"/>
                        <h3 className="font-bold text-white text-sm">在线玩家</h3>
                    </div>

                    <div className="flex-1 overflow-y-auto p-3 space-y-2">
                        {players.map((player, index) => (
                            <motion.div
                                key={player.id}
                                initial={{opacity: 0, x: -10}}
                                animate={{opacity: 1, x: 0}}
                                transition={{delay: 0.2 + index * 0.05}}
                                className="flex items-center gap-3 p-2.5 rounded-lg bg-slate-900/50 border border-slate-700/30 hover:border-purple-500/30 hover:bg-slate-800/50 transition-all duration-200"
                            >
                                <div
                                    className="w-9 h-9 rounded-lg bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold shrink-0">
                                    {player.avatar || player.name?.[0] || '?'}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-semibold text-white truncate">{player.name}</p>
                                    <p className="text-xs text-slate-400 truncate">{player.characterName || '未知'}</p>
                                </div>
                                <div
                                    className="w-2 h-2 rounded-full bg-emerald-400 shrink-0 shadow-lg shadow-emerald-400/30"/>
                            </motion.div>
                        ))}
                    </div>
                </motion.div>

                {/* 中间：聊天区 */}
                <motion.div
                    initial={{opacity: 0, y: 10}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.2}}
                    className="flex-1 rounded-xl bg-slate-800/50 border border-slate-700/50 flex flex-col overflow-hidden"
                >
                    <div
                        className="flex-none flex items-center gap-2 px-4 py-3 bg-gradient-to-r from-blue-500/20 to-purple-500/20 border-b border-slate-700/50">
                        <MessageSquare className="w-4 h-4 text-blue-400"/>
                        <h3 className="font-bold text-white text-sm">讨论区</h3>
                    </div>

                    {/* 消息列表 */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-3">
                        <div className="flex gap-3">
                            <div
                                className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white text-sm font-bold shrink-0">
                                A
                            </div>
                            <div className="flex-1">
                                <div className="flex items-baseline gap-2 mb-1">
                                    <span className="text-sm font-bold text-white">侦探A</span>
                                    <span className="text-xs text-slate-500">22:35</span>
                                </div>
                                <div
                                    className="bg-slate-700/60 rounded-lg rounded-tl-none px-3 py-2.5 border border-slate-600/30">
                                    <p className="text-sm text-slate-200 leading-relaxed">书房里的匕首很可疑，上面的血迹还没完全干透。</p>
                                </div>
                            </div>
                        </div>

                        <div className="flex gap-3">
                            <div
                                className="w-9 h-9 rounded-lg bg-gradient-to-br from-amber-500 to-orange-500 flex items-center justify-center text-white text-sm font-bold shrink-0">
                                B
                            </div>
                            <div className="flex-1">
                                <div className="flex items-baseline gap-2 mb-1">
                                    <span className="text-sm font-bold text-white">管家B</span>
                                    <span className="text-xs text-slate-500">22:36</span>
                                </div>
                                <div
                                    className="bg-slate-700/60 rounded-lg rounded-tl-none px-3 py-2.5 border border-slate-600/30">
                                    <p className="text-sm text-slate-200 leading-relaxed">那是老爷的家传匕首，一直放在书房的收藏柜里...</p>
                                </div>
                            </div>
                        </div>

                        <div className="flex gap-3">
                            <div
                                className="w-9 h-9 rounded-lg bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-sm font-bold shrink-0">
                                C
                            </div>
                            <div className="flex-1">
                                <div className="flex items-baseline gap-2 mb-1">
                                    <span className="text-sm font-bold text-white">女仆C</span>
                                    <span className="text-xs text-slate-500">22:38</span>
                                </div>
                                <div
                                    className="bg-slate-700/60 rounded-lg rounded-tl-none px-3 py-2.5 border border-slate-600/30">
                                    <p className="text-sm text-slate-200 leading-relaxed">我听到的争吵声，其中一个是女人的声音！</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 输入框 */}
                    <div className="flex-none p-3 border-t border-slate-700/50 bg-slate-900/30">
                        <div className="flex gap-2">
                            <input
                                type="text"
                                placeholder="发表你的推理..."
                                className="flex-1 px-4 py-2.5 rounded-lg bg-slate-900/80 border border-slate-600/50 text-white text-sm placeholder-slate-500 focus:outline-none focus:border-purple-500/50 focus:ring-1 focus:ring-purple-500/30 transition-all"
                            />
                            <button
                                className="px-4 py-2.5 rounded-lg bg-gradient-to-r from-purple-500 to-pink-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-purple-500/30 transition-all duration-300 flex items-center gap-2">
                                <Send className="w-4 h-4"/>
                                <span className="hidden sm:inline">发送</span>
                            </button>
                        </div>
                    </div>
                </motion.div>

                {/* 右侧：私人线索 */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.25}}
                    className="w-56 flex-none"
                >
                    <ClueList
                        clues={privateClues}
                        title="私人线索"
                        isPrivate={true}
                        emptyText="暂无私人线索"
                    />
                </motion.div>
            </div>

            {/* 底部信息栏 */}
            <motion.div
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{delay: 0.3}}
                className="flex-none mt-4 pt-4 border-t border-slate-700/50"
            >
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-6">
                        <div className="flex items-center gap-2 text-slate-400">
                            <Clock className="w-4 h-4"/>
                            <span>剩余时间: <span className="text-purple-400 font-mono font-bold">15:00</span></span>
                        </div>
                        <div className="flex items-center gap-2 text-slate-400 text-sm">
                            <span>公开线索: <span
                                className="text-emerald-400 font-bold">{publicClues.length}</span></span>
                            <span className="text-slate-600">|</span>
                            <span>私人线索: <span
                                className="text-purple-400 font-bold">{privateClues.length}</span></span>
                        </div>
                    </div>
                    <button
                        className="px-5 py-2 rounded-lg bg-gradient-to-r from-purple-500 to-pink-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-purple-500/30 transition-all duration-300">
                        进入投票
                    </button>
                </div>
            </motion.div>
        </div>
    )
}

export default PhaseDiscussion
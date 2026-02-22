import {motion} from 'framer-motion'
import {FileText, Home, RefreshCw, Star, Target, Trophy, Users} from 'lucide-react'

/**
 * 结束阶段组件 - 重新设计
 * 采用三栏布局：真相揭晓 | 投票结果 | 玩家评分
 */
function PhaseEnding({gameData}) {
    // Mock 数据
    const result = {
        winner: {
            name: '玩家B',
            role: '管家',
            avatar: 'B',
            votes: 3,
            isCorrect: true
        },
        voteResults: [
            {name: '玩家B', avatar: 'B', votes: 3, isKiller: true},
            {name: '玩家A', avatar: 'A', votes: 1, isKiller: false},
            {name: '玩家C', avatar: 'C', votes: 0, isKiller: false},
            {name: '玩家D', avatar: 'D', votes: 0, isKiller: false},
        ],
        gameStats: {
            rating: 4,
            maxRating: 5,
            cluesFound: 8,
            totalClues: 10,
            gameTime: '45分钟'
        }
    }

    return (
        <div className="h-full flex flex-col p-4">
            {/* 顶部标题区 - 更紧凑 */}
            <motion.div
                initial={{opacity: 0, y: -10}}
                animate={{opacity: 1, y: 0}}
                className="flex-none text-center mb-3"
            >
                <motion.div
                    initial={{scale: 0}}
                    animate={{scale: 1}}
                    transition={{delay: 0.2, type: 'spring', stiffness: 200}}
                    className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-gradient-to-br from-emerald-500 to-teal-500 mb-2 shadow-lg shadow-emerald-500/30"
                >
                    <Trophy className="w-6 h-6 text-white"/>
                </motion.div>
                <h2 className="text-xl font-bold text-white mb-0.5">游戏结束</h2>
                <p className="text-xs text-slate-400">真相大白，正义得到伸张</p>
            </motion.div>

            {/* 主要内容区 - 三栏布局，使用 overflow-hidden 防止溢出 */}
            <div className="flex-1 min-h-0 grid grid-cols-3 gap-3 overflow-hidden">
                {/* 左栏：真相揭晓 */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="flex flex-col h-full"
                >
                    {/* 卡片头部 */}
                    <div className="flex items-center gap-2 mb-2 px-1">
                        <div
                            className="w-6 h-6 rounded-lg bg-gradient-to-br from-emerald-500/20 to-teal-500/20 border border-emerald-500/30 flex items-center justify-center">
                            <Target className="w-3 h-3 text-emerald-400"/>
                        </div>
                        <h3 className="text-sm font-bold text-white">真相揭晓</h3>
                    </div>

                    {/* 卡片内容 */}
                    <div
                        className="flex-1 rounded-xl bg-slate-800/60 border border-slate-700/50 p-3 flex flex-col overflow-hidden">
                        {/* 凶手信息 */}
                        <div className="text-center mb-3 pb-3 border-b border-slate-700/50">
                            <p className="text-[10px] text-emerald-400 font-medium uppercase tracking-wider mb-2">凶手是</p>
                            <motion.div
                                initial={{scale: 0.8}}
                                animate={{scale: 1}}
                                transition={{delay: 0.4, type: 'spring'}}
                                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-emerald-500/20 to-teal-500/20 border border-emerald-500/30"
                            >
                                <div
                                    className="w-10 h-10 rounded-full bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center text-white font-bold text-base shadow-lg shadow-emerald-500/20">
                                    {result.winner.avatar}
                                </div>
                                <div className="text-left">
                                    <p className="font-bold text-white text-sm">{result.winner.name}</p>
                                    <p className="text-[10px] text-slate-400">{result.winner.role}</p>
                                </div>
                            </motion.div>
                        </div>

                        {/* 案情回顾 */}
                        <div className="space-y-2 flex-1 overflow-y-auto">
                            <div>
                                <div className="flex items-center gap-1.5 mb-1">
                                    <FileText className="w-3 h-3 text-slate-400"/>
                                    <span className="text-[10px] font-semibold text-slate-300">作案动机</span>
                                </div>
                                <p className="text-[10px] text-slate-400 leading-relaxed pl-4">
                                    长期遭受不公正待遇，管家决定报复。在晚宴后潜入书房，用家传匕首行凶。
                                </p>
                            </div>

                            <div>
                                <div className="flex items-center gap-1.5 mb-1">
                                    <Star className="w-3 h-3 text-slate-400"/>
                                    <span className="text-[10px] font-semibold text-slate-300">关键证据</span>
                                </div>
                                <ul className="text-[10px] text-slate-400 space-y-1 pl-4">
                                    <li className="flex items-start gap-1.5">
                                        <span className="text-emerald-400 mt-0.5">▸</span>
                                        <span>管家房间的作案工具</span>
                                    </li>
                                    <li className="flex items-start gap-1.5">
                                        <span className="text-emerald-400 mt-0.5">▸</span>
                                        <span>日记本中的怨恨记录</span>
                                    </li>
                                    <li className="flex items-start gap-1.5">
                                        <span className="text-emerald-400 mt-0.5">▸</span>
                                        <span>时间线上的矛盾证词</span>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </motion.div>

                {/* 中栏：投票结果 */}
                <motion.div
                    initial={{opacity: 0, y: 10}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.2}}
                    className="flex flex-col h-full"
                >
                    {/* 卡片头部 */}
                    <div className="flex items-center gap-2 mb-2 px-1">
                        <div
                            className="w-6 h-6 rounded-lg bg-gradient-to-br from-amber-500/20 to-orange-500/20 border border-amber-500/30 flex items-center justify-center">
                            <Users className="w-3 h-3 text-amber-400"/>
                        </div>
                        <h3 className="text-sm font-bold text-white">投票结果</h3>
                    </div>

                    {/* 卡片内容 */}
                    <div
                        className="flex-1 rounded-xl bg-slate-800/60 border border-slate-700/50 p-3 overflow-hidden flex flex-col">
                        <div className="space-y-2 overflow-y-auto">
                            {result.voteResults.map((player, index) => (
                                <motion.div
                                    key={player.name}
                                    initial={{opacity: 0, x: -10}}
                                    animate={{opacity: 1, x: 0}}
                                    transition={{delay: 0.3 + index * 0.05}}
                                    className={`
                                        flex items-center justify-between p-2.5 rounded-lg border
                                        ${player.isKiller
                                        ? 'bg-gradient-to-r from-emerald-500/15 to-teal-500/15 border-emerald-500/30'
                                        : 'bg-slate-700/30 border-slate-600/30'
                                    }
                                    `}
                                >
                                    <div className="flex items-center gap-2">
                                        <div className={`
                                            w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold
                                            ${player.isKiller
                                            ? 'bg-gradient-to-br from-emerald-500 to-teal-500 text-white shadow-lg shadow-emerald-500/20'
                                            : 'bg-slate-600 text-slate-300'
                                        }
                                        `}>
                                            {player.avatar}
                                        </div>
                                        <span className={`
                                            font-medium text-xs
                                            ${player.isKiller ? 'text-white' : 'text-slate-400'}
                                        `}>
                                            {player.name}
                                        </span>
                                    </div>
                                    <div className="flex items-center gap-1">
                                        <span className={`
                                            text-base font-bold
                                            ${player.isKiller ? 'text-emerald-400' : 'text-slate-500'}
                                        `}>
                                            {player.votes}
                                        </span>
                                        <span className="text-[10px] text-slate-500">票</span>
                                    </div>
                                </motion.div>
                            ))}
                        </div>

                        {/* 投票统计 */}
                        <div className="mt-3 pt-3 border-t border-slate-700/50">
                            <div className="grid grid-cols-2 gap-2 text-center">
                                <div className="p-2 rounded-lg bg-slate-700/30">
                                    <p className="text-base font-bold text-white">{result.winner.votes}</p>
                                    <p className="text-[10px] text-slate-500 uppercase">最高票数</p>
                                </div>
                                <div className="p-2 rounded-lg bg-slate-700/30">
                                    <p className="text-base font-bold text-emerald-400">正确</p>
                                    <p className="text-[10px] text-slate-500 uppercase">判决结果</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </motion.div>

                {/* 右栏：游戏评分 */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.3}}
                    className="flex flex-col h-full"
                >
                    {/* 卡片头部 */}
                    <div className="flex items-center gap-2 mb-2 px-1">
                        <div
                            className="w-6 h-6 rounded-lg bg-gradient-to-br from-yellow-500/20 to-amber-500/20 border border-yellow-500/30 flex items-center justify-center">
                            <Star className="w-3 h-3 text-yellow-400"/>
                        </div>
                        <h3 className="text-sm font-bold text-white">游戏评分</h3>
                    </div>

                    {/* 卡片内容 */}
                    <div
                        className="flex-1 rounded-xl bg-slate-800/60 border border-slate-700/50 p-3 flex flex-col overflow-hidden">
                        {/* 星级评分 */}
                        <div className="text-center mb-3 pb-3 border-b border-slate-700/50">
                            <p className="text-[10px] text-slate-500 uppercase tracking-wider mb-2">总体评价</p>
                            <div className="flex items-center justify-center gap-1 mb-1">
                                {[...Array(result.gameStats.maxRating)].map((_, i) => (
                                    <motion.div
                                        key={i}
                                        initial={{scale: 0}}
                                        animate={{scale: 1}}
                                        transition={{delay: 0.5 + i * 0.08, type: 'spring', stiffness: 200}}
                                    >
                                        <Star
                                            className={`w-5 h-5 ${
                                                i < result.gameStats.rating
                                                    ? 'fill-yellow-400 text-yellow-400 drop-shadow-lg shadow-yellow-400/30'
                                                    : 'text-slate-600'
                                            }`}
                                        />
                                    </motion.div>
                                ))}
                            </div>
                            <p className="text-xs text-slate-400">
                                <span className="text-yellow-400 font-bold text-base">{result.gameStats.rating}</span>
                                <span className="text-slate-600"> / {result.gameStats.maxRating}</span>
                            </p>
                        </div>

                        {/* 统计数据 */}
                        <div className="space-y-2 flex-1 overflow-y-auto">
                            <div className="p-2 rounded-lg bg-slate-700/30">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="text-[10px] text-slate-400">线索收集</span>
                                    <span className="text-[10px] font-bold text-cyan-400">
                                        {result.gameStats.cluesFound}/{result.gameStats.totalClues}
                                    </span>
                                </div>
                                <div className="h-1 bg-slate-600/50 rounded-full overflow-hidden">
                                    <motion.div
                                        initial={{width: 0}}
                                        animate={{width: `${(result.gameStats.cluesFound / result.gameStats.totalClues) * 100}%`}}
                                        transition={{delay: 0.6, duration: 0.8}}
                                        className="h-full bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full"
                                    />
                                </div>
                            </div>

                            <div className="p-2 rounded-lg bg-slate-700/30">
                                <div className="flex items-center justify-between">
                                    <span className="text-[10px] text-slate-400">游戏时长</span>
                                    <span className="text-xs font-bold text-white">{result.gameStats.gameTime}</span>
                                </div>
                            </div>

                            <div className="p-2 rounded-lg bg-slate-700/30">
                                <div className="flex items-center justify-between">
                                    <span className="text-[10px] text-slate-400">投票正确率</span>
                                    <span className="text-xs font-bold text-emerald-400">75%</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </motion.div>
            </div>

            {/* 底部按钮 - 更紧凑 */}
            <motion.div
                initial={{opacity: 0, y: 10}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.5}}
                className="flex-none mt-3 pt-3 border-t border-slate-700/50"
            >
                <div className="flex items-center justify-center gap-3">
                    <button
                        className="flex items-center gap-1.5 px-5 py-2 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-emerald-500/30 transition-all duration-300">
                        <RefreshCw className="w-3.5 h-3.5"/>
                        再来一局
                    </button>
                    <button
                        className="flex items-center gap-1.5 px-5 py-2 rounded-lg bg-slate-700/50 border border-slate-600/50 text-white text-sm font-medium cursor-pointer hover:bg-slate-600/50 transition-all duration-300">
                        <Home className="w-3.5 h-3.5"/>
                        返回大厅
                    </button>
                </div>
            </motion.div>
        </div>
    )
}

export default PhaseEnding
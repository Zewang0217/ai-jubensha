import {motion} from 'framer-motion'
import {AlertTriangle, CheckCircle, Clock, Users} from 'lucide-react'

/**
 * 投票阶段组件
 * 紧凑布局，一屏显示
 */
function PhaseVoting({gameData}) {
    return (
        <div className="h-full flex flex-col p-6">
            {/* 阶段标题 */}
            <motion.div
                initial={{opacity: 0, y: -10}}
                animate={{opacity: 1, y: 0}}
                className="flex-none text-center mb-4"
            >
                <h2 className="text-2xl font-bold text-white mb-1">投票阶段</h2>
                <p className="text-slate-400 text-sm">选出你心中的凶手</p>
            </motion.div>

            {/* 主内容区 */}
            <div className="flex-1 min-h-0 flex gap-4">
                {/* 投票区 */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="flex-1 rounded-xl bg-slate-800/50 border border-slate-700/50 p-4"
                >
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-2">
                            <Users className="w-4 h-4 text-red-400"/>
                            <h3 className="font-semibold text-white">玩家投票</h3>
                        </div>
                        <span className="px-2 py-0.5 text-xs rounded-full bg-red-500/20 text-red-300">
                            2/4
                        </span>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                        {gameData?.players?.map((player, index) => (
                            <motion.button
                                key={player.id}
                                initial={{opacity: 0, scale: 0.9}}
                                animate={{opacity: 1, scale: 1}}
                                transition={{delay: 0.15 + index * 0.05}}
                                whileHover={{scale: 1.02}}
                                whileTap={{scale: 0.98}}
                                className={`p-4 rounded-xl border transition-all duration-300 text-left ${
                                    index < 2
                                        ? 'bg-gradient-to-r from-red-500/20 to-rose-500/20 border-red-500/50'
                                        : 'bg-slate-700/30 border-slate-600/30 hover:border-red-500/50'
                                }`}
                            >
                                <div className="flex items-start gap-3">
                                    <div
                                        className="w-12 h-12 rounded-xl bg-gradient-to-br from-red-500 to-rose-500 flex items-center justify-center text-white font-bold text-lg">
                                        {player.name?.[0] || '?'}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <h4 className="font-semibold text-white text-sm truncate">{player.name}</h4>
                                        <p className="text-xs text-slate-400 truncate">{player.characterName || '未知'}</p>
                                    </div>
                                    {index < 2 && (
                                        <CheckCircle className="w-5 h-5 text-red-300 shrink-0"/>
                                    )}
                                </div>
                                <div className="mt-3 pt-3 border-t border-slate-600/30">
                                    <div className="flex items-center justify-between">
                                        <span className="text-xs text-slate-400">票数</span>
                                        <span
                                            className={`text-xl font-bold ${index < 2 ? 'text-red-400' : 'text-slate-600'}`}>
                                            {index === 0 ? '3' : index === 1 ? '1' : '0'}
                                        </span>
                                    </div>
                                </div>
                            </motion.button>
                        )) || (
                            <div className="col-span-full flex items-center justify-center py-8 text-slate-500">
                                <Users className="w-12 h-12 mb-2 opacity-50"/>
                                <p>暂无玩家</p>
                            </div>
                        )}
                    </div>
                </motion.div>

                {/* 规则说明 */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.15}}
                    className="w-64 flex-none rounded-xl bg-slate-800/50 border border-slate-700/50 p-4"
                >
                    <div className="flex items-center gap-2 mb-4">
                        <AlertTriangle className="w-4 h-4 text-amber-400"/>
                        <h3 className="font-semibold text-white">投票规则</h3>
                    </div>

                    <ul className="space-y-3 text-sm text-slate-300">
                        <li className="flex items-start gap-2">
                            <span className="text-red-400 mt-1">•</span>
                            <span>每位玩家只能投一票</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <span className="text-red-400 mt-1">•</span>
                            <span>可以投给自己</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <span className="text-red-400 mt-1">•</span>
                            <span>票数最多者被指认为凶手</span>
                        </li>
                        <li className="flex items-start gap-2">
                            <span className="text-red-400 mt-1">•</span>
                            <span>投票结果决定游戏结局</span>
                        </li>
                    </ul>

                    <div className="mt-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
                        <p className="text-xs text-red-300">
                            ⚠️ 投票不可更改，请仔细思考
                        </p>
                    </div>
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
                    <div className="flex items-center gap-2 text-slate-400">
                        <Clock className="w-4 h-4"/>
                        <span>剩余时间: <span className="text-red-300 font-mono font-bold">05:00</span></span>
                    </div>
                    <button
                        className="px-4 py-2 rounded-lg bg-gradient-to-r from-red-500 to-rose-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-red-500/30 transition-all duration-300">
                        确认投票
                    </button>
                </div>
            </motion.div>
        </div>
    )
}

export default PhaseVoting
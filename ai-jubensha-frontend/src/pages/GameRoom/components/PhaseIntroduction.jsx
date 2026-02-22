import {motion} from 'framer-motion'
import {Clock, UserCircle} from 'lucide-react'

/**
 * 自我介绍阶段组件
 * 紧凑布局，一屏显示
 */
function PhaseIntroduction({gameData}) {
    return (
        <div className="h-full flex flex-col p-6">
            {/* 阶段标题 */}
            <motion.div
                initial={{opacity: 0, y: -10}}
                animate={{opacity: 1, y: 0}}
                className="flex-none text-center mb-4"
            >
                <h2 className="text-2xl font-bold text-white mb-1">自我介绍</h2>
                <p className="text-slate-400 text-sm">请各位玩家介绍自己的角色</p>
            </motion.div>

            {/* 玩家列表 */}
            <motion.div
                initial={{opacity: 0, y: 10}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.1}}
                className="flex-1 min-h-0"
            >
                <div className="h-full grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                    {gameData?.players?.map((player, index) => (
                        <motion.div
                            key={player.id}
                            initial={{opacity: 0, scale: 0.9}}
                            animate={{opacity: 1, scale: 1}}
                            transition={{delay: 0.1 + index * 0.05}}
                            className="flex flex-col items-center justify-center p-4 rounded-xl bg-slate-800/50 border border-slate-700/50 hover:border-blue-500/50 transition-all duration-300"
                        >
                            <div
                                className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center text-white font-bold text-xl mb-3">
                                {player.name?.[0] || '?'}
                            </div>
                            <h3 className="font-semibold text-white text-center">{player.name}</h3>
                            <p className="text-xs text-slate-400 text-center mt-1 line-clamp-1">
                                {player.characterName || '未选择角色'}
                            </p>
                            {player.isHost && (
                                <span
                                    className="mt-2 px-2 py-0.5 text-[10px] rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">
                                    房主
                                </span>
                            )}
                        </motion.div>
                    )) || (
                        <div className="col-span-full flex items-center justify-center">
                            <div className="text-center text-slate-500">
                                <UserCircle className="w-12 h-12 mx-auto mb-2 opacity-50"/>
                                <p>等待玩家加入...</p>
                            </div>
                        </div>
                    )}
                </div>
            </motion.div>

            {/* 底部信息栏 */}
            <motion.div
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{delay: 0.2}}
                className="flex-none mt-4 pt-4 border-t border-slate-700/50"
            >
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2 text-slate-400">
                        <Clock className="w-4 h-4"/>
                        <span>剩余时间: <span className="text-blue-300 font-mono font-bold">05:00</span></span>
                    </div>
                    <button
                        className="px-4 py-2 rounded-lg bg-gradient-to-r from-blue-500 to-cyan-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-300">
                        准备就绪
                    </button>
                </div>
            </motion.div>
        </div>
    )
}

export default PhaseIntroduction
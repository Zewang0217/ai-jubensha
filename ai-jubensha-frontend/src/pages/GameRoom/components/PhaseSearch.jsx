import {motion} from 'framer-motion'
import {Clock, Lightbulb, MapPin, Search} from 'lucide-react'

/**
 * 搜证阶段组件
 * 紧凑布局，一屏显示
 */
function PhaseSearch({gameData}) {
    // 模拟线索位置
    const searchSpots = [
        {id: 1, name: '书房', x: 20, y: 30},
        {id: 2, name: '客厅', x: 50, y: 50},
        {id: 3, name: '卧室', x: 80, y: 25},
        {id: 4, name: '厨房', x: 35, y: 70},
        {id: 5, name: '花园', x: 70, y: 75},
    ]

    return (
        <div className="h-full flex flex-col p-6">
            {/* 阶段标题 */}
            <motion.div
                initial={{opacity: 0, y: -10}}
                animate={{opacity: 1, y: 0}}
                className="flex-none text-center mb-4"
            >
                <h2 className="text-2xl font-bold text-white mb-1">搜证阶段</h2>
                <p className="text-slate-400 text-sm">点击场景中的标记进行搜证</p>
            </motion.div>

            {/* 主内容区 */}
            <div className="flex-1 min-h-0 flex gap-4">
                {/* 场景地图 */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="flex-1 relative rounded-xl bg-slate-800/50 border border-slate-700/50 overflow-hidden"
                >
                    {/* 网格背景 */}
                    <div
                        className="absolute inset-0 bg-[linear-gradient(rgba(245,158,11,0.1)_1px,transparent_1px),linear-gradient(90deg,rgba(245,158,11,0.1)_1px,transparent_1px)] bg-[size:40px_40px]"/>

                    {/* 搜证点 */}
                    {searchSpots.map((spot, index) => (
                        <motion.button
                            key={spot.id}
                            initial={{opacity: 0, scale: 0}}
                            animate={{opacity: 1, scale: 1}}
                            transition={{delay: 0.2 + index * 0.1}}
                            whileHover={{scale: 1.1}}
                            whileTap={{scale: 0.95}}
                            className="absolute w-12 h-12 -ml-6 -mt-6 rounded-full bg-amber-500/20 border-2 border-amber-500/50 flex items-center justify-center hover:bg-amber-500/40 transition-all duration-300 group"
                            style={{left: `${spot.x}%`, top: `${spot.y}%`}}
                        >
                            <MapPin className="w-5 h-5 text-amber-400 group-hover:text-amber-300"/>
                            {/* 提示文字 */}
                            <span
                                className="absolute -bottom-6 left-1/2 -translate-x-1/2 text-xs text-amber-400 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity">
                                {spot.name}
                            </span>
                        </motion.button>
                    ))}

                    {/* 中心提示 */}
                    <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                        <div className="text-center text-slate-600">
                            <Search className="w-8 h-8 mx-auto mb-2 opacity-50"/>
                            <p className="text-sm">选择地点进行搜证</p>
                        </div>
                    </div>
                </motion.div>

                {/* 线索列表 */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.15}}
                    className="w-64 flex-none rounded-xl bg-slate-800/50 border border-slate-700/50 p-4 flex flex-col"
                >
                    <div className="flex items-center gap-2 mb-4">
                        <Lightbulb className="w-4 h-4 text-amber-400"/>
                        <h3 className="font-semibold text-white">已发现线索</h3>
                        <span className="ml-auto px-2 py-0.5 text-xs rounded-full bg-amber-500/20 text-amber-300">
                            0/5
                        </span>
                    </div>

                    <div className="flex-1 flex items-center justify-center">
                        <div className="text-center text-slate-400">
                            <Lightbulb className="w-10 h-10 mx-auto mb-2 opacity-40"/>
                            <p className="text-sm">暂无线索</p>
                            <p className="text-xs mt-1">点击地图标记搜证</p>
                        </div>
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
                        <span>剩余时间: <span className="text-amber-300 font-mono font-bold">10:00</span></span>
                    </div>
                    <button
                        className="px-4 py-2 rounded-lg bg-gradient-to-r from-amber-500 to-orange-500 text-white text-sm font-bold cursor-pointer hover:shadow-lg hover:shadow-amber-500/30 transition-all duration-300">
                        结束搜证
                    </button>
                </div>
            </motion.div>
        </div>
    )
}

export default PhaseSearch
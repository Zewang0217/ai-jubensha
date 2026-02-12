import {motion} from 'framer-motion'
import {BookOpen, ChevronRight, Clock, Users} from 'lucide-react'

/**
 * ScriptCard 组件 - 剧本卡片
 * 展示剧本的基本信息，支持悬停动画效果
 *
 * @param {Object} props
 * @param {number} props.id - 剧本ID
 * @param {string} props.name - 剧本名称
 * @param {string} props.description - 剧本描述
 * @param {string} props.author - 作者
 * @param {'EASY'|'MEDIUM'|'HARD'} props.difficulty - 难度等级
 * @param {number} props.playerCount - 玩家数量
 * @param {number} props.duration - 游戏时长(分钟)
 * @param {string} props.coverImageUrl - 封面图片URL
 * @param {number} props.index - 索引用于交错动画
 * @param {Function} props.onClick - 点击回调
 */
const ScriptCard = ({
                        id,
                        name,
                        description,
                        author,
                        difficulty,
                        playerCount,
                        duration,
                        coverImageUrl,
                        index = 0,
                        onClick,
                    }) => {
    // 难度映射
    const difficultyMap = {
        EASY: {text: '简单', color: 'bg-emerald-100 text-emerald-700'},
        MEDIUM: {text: '中等', color: 'bg-amber-100 text-amber-700'},
        HARD: {text: '困难', color: 'bg-rose-100 text-rose-700'},
    }

    const difficultyInfo = difficultyMap[difficulty] || difficultyMap.MEDIUM

    return (
        <motion.div
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            transition={{
                duration: 0.5,
                delay: index * 0.1,
                ease: [0.25, 0.46, 0.45, 0.94],
            }}
            whileHover={{y: -8, scale: 1.02}}
            whileTap={{scale: 0.98}}
            onClick={() => onClick?.(id)}
            className="group relative cursor-pointer"
        >
            {/* 卡片主体 */}
            <div
                className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-white/90 to-white/60 backdrop-blur-xl border border-white/70 shadow-lg shadow-blue-900/5 transition-all duration-500 group-hover:shadow-xl group-hover:shadow-blue-500/15 group-hover:border-blue-200/80">
                {/* 封面图片区域 */}
                <div className="relative h-48 overflow-hidden">
                    {/* 图片 */}
                    <div
                        className="absolute inset-0 bg-gradient-to-br from-blue-400/30 to-blue-600/30 transition-transform duration-700 group-hover:scale-110"
                        style={
                            coverImageUrl
                                ? {
                                    backgroundImage: `url(${coverImageUrl})`,
                                    backgroundSize: 'cover',
                                    backgroundPosition: 'center',
                                }
                                : {}
                        }
                    >
                        {!coverImageUrl && (
                            <div className="absolute inset-0 flex items-center justify-center">
                                <BookOpen className="w-16 h-16 text-blue-300/50" strokeWidth={1.5}/>
                            </div>
                        )}
                    </div>

                    {/* 渐变遮罩 */}
                    <div className="absolute inset-0 bg-gradient-to-t from-white via-white/20 to-transparent"/>

                    {/* 难度标签 */}
                    <div className="absolute top-4 right-4">
            <span
                className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${difficultyInfo.color}`}
            >
              {difficultyInfo.text}
            </span>
                    </div>

                    {/* 悬浮光效 */}
                    <div
                        className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 bg-gradient-to-t from-blue-500/10 to-transparent"/>
                </div>

                {/* 内容区域 */}
                <div className="p-6">
                    {/* 标题 */}
                    <h3 className="text-xl font-bold text-slate-800 mb-1 line-clamp-1 group-hover:text-blue-600 transition-colors duration-300">
                        {name}
                    </h3>

                    {/* 作者 */}
                    {author && (
                        <p className="text-xs text-slate-400 mb-2">by {author}</p>
                    )}

                    {/* 描述 */}
                    <p className="text-slate-500 text-sm leading-relaxed mb-4 line-clamp-2">
                        {description}
                    </p>

                    {/* 元信息 */}
                    <div className="flex items-center gap-4 text-sm text-slate-400">
                        <div className="flex items-center gap-1.5">
                            <Users className="w-4 h-4"/>
                            <span>{playerCount} 人</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <Clock className="w-4 h-4"/>
                            <span>{duration} 分钟</span>
                        </div>
                    </div>
                </div>

                {/* 底部操作栏 */}
                <div className="px-6 pb-6">
                    <div className="flex items-center justify-between pt-4 border-t border-slate-100">
                        <span className="text-sm font-medium text-blue-600">查看详情</span>
                        <motion.div
                            className="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center group-hover:bg-blue-500 transition-colors duration-300"
                            whileHover={{x: 4}}
                        >
                            <ChevronRight
                                className="w-4 h-4 text-blue-500 group-hover:text-white transition-colors duration-300"/>
                        </motion.div>
                    </div>
                </div>
            </div>

            {/* 外发光效果 */}
            <div
                className="absolute -inset-0.5 bg-gradient-to-r from-blue-500 to-blue-700 rounded-2xl opacity-0 group-hover:opacity-20 blur transition-opacity duration-500 -z-10"/>
        </motion.div>
    )
}

export default ScriptCard

import {useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'
import {BookOpen, ChevronDown, Lock, Scroll, User} from 'lucide-react'

/**
 * CharacterCard 组件 - 角色卡片
 * 展示角色的基本信息，支持展开查看详细背景故事
 *
 * @param {Object} props
 * @param {number} props._id - 角色ID
 * @param {string} props.name - 角色名称
 * @param {string} props.description - 角色描述
 * @param {string} props.backgroundStory - 角色背景故事
 * @param {string} props._secret - 角色秘密（不直接显示）
 * @param {string} props.avatarUrl - 头像URL
 * @param {number} props.index - 索引用于交错动画
 */
const CharacterCard = ({
                           _id,
                           name,
                           description,
                           backgroundStory,
                           _secret,
                           avatarUrl,
                           index = 0,
                       }) => {
    const [isExpanded, setIsExpanded] = useState(false)
    const [activeTab, setActiveTab] = useState('background')

    // 获取文本的前N行（按换行符分割）
    const getFirstLines = (text, maxLines = 3) => {
        if (!text) return ''
        const lines = text.split('\n').filter((line) => line.trim())
        if (lines.length <= maxLines) return text
        return lines.slice(0, maxLines).join('\n') + '...'
    }

    // 截断文本（按字符数）
    const truncateText = (text, maxLength = 100) => {
        if (!text || text.length <= maxLength) return text
        return text.substring(0, maxLength) + '...'
    }

    // 处理背景故事显示 - 仅显示前几行
    const displayBackgroundStory = isExpanded
        ? getFirstLines(backgroundStory, 5)
        : getFirstLines(backgroundStory, 3)

    return (
        <motion.div
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            transition={{
                duration: 0.5,
                delay: index * 0.1,
                ease: [0.25, 0.46, 0.45, 0.94],
            }}
            className="group"
        >
            {/* 卡片主体 */}
            <div
                className={`relative overflow-hidden rounded-2xl bg-gradient-to-br from-white/90 to-white/60 backdrop-blur-xl border border-white/70 shadow-lg shadow-blue-900/5 transition-all duration-500 hover:shadow-xl hover:shadow-blue-500/10 hover:border-blue-200/80 ${
                    isExpanded ? 'ring-2 ring-blue-500/20' : ''
                }`}
            >
                {/* 头部信息 */}
                <div className="p-6">
                    <div className="flex items-start gap-4">
                        {/* 头像 */}
                        <div className="relative flex-shrink-0">
                            <div
                                className="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center overflow-hidden shadow-lg shadow-blue-500/30">
                                {avatarUrl ? (
                                    <img
                                        src={avatarUrl}
                                        alt={name}
                                        className="w-full h-full object-cover"
                                    />
                                ) : (
                                    <User className="w-10 h-10 text-white/80" strokeWidth={1.5}/>
                                )}
                            </div>
                            {/* 序号徽章 */}
                            <div
                                className="absolute -top-2 -right-2 w-7 h-7 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white text-xs font-bold shadow-md">
                                {index + 1}
                            </div>
                        </div>

                        {/* 基本信息 */}
                        <div className="flex-1 min-w-0">
                            <h3 className="text-xl font-bold text-slate-800 mb-2 group-hover:text-blue-600 transition-colors duration-300">
                                {name}
                            </h3>
                            <p className="text-slate-500 text-sm leading-relaxed line-clamp-3">
                                {truncateText(description, 120)}
                            </p>
                        </div>
                    </div>
                </div>

                {/* 展开内容 */}
                <AnimatePresence>
                    {isExpanded && (
                        <motion.div
                            initial={{height: 0, opacity: 0}}
                            animate={{height: 'auto', opacity: 1}}
                            exit={{height: 0, opacity: 0}}
                            transition={{duration: 0.3, ease: [0.25, 0.46, 0.45, 0.94]}}
                            className="overflow-hidden"
                        >
                            {/* 标签切换 */}
                            <div className="px-6 pb-4">
                                <div className="flex gap-2 p-1 rounded-xl bg-slate-100/80">
                                    <button
                                        onClick={() => setActiveTab('background')}
                                        className={`flex-1 flex items-center justify-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-300 ${
                                            activeTab === 'background'
                                                ? 'bg-white text-blue-600 shadow-sm'
                                                : 'text-slate-500 hover:text-slate-700'
                                        }`}
                                    >
                                        <BookOpen className="w-4 h-4"/>
                                        背景故事
                                    </button>
                                    <button
                                        onClick={() => setActiveTab('secret')}
                                        className={`flex-1 flex items-center justify-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-300 ${
                                            activeTab === 'secret'
                                                ? 'bg-white text-rose-600 shadow-sm'
                                                : 'text-slate-500 hover:text-slate-700'
                                        }`}
                                    >
                                        <Lock className="w-4 h-4"/>
                                        角色秘密
                                    </button>
                                </div>
                            </div>

                            {/* 内容区域 */}
                            <div className="px-6 pb-6">
                                <motion.div
                                    key={activeTab}
                                    initial={{opacity: 0, x: 20}}
                                    animate={{opacity: 1, x: 0}}
                                    exit={{opacity: 0, x: -20}}
                                    transition={{duration: 0.2}}
                                    className={`p-5 rounded-xl ${
                                        activeTab === 'background'
                                            ? 'bg-blue-50/50 border border-blue-100'
                                            : 'bg-rose-50/50 border border-rose-100'
                                    }`}
                                >
                                    <div className="flex items-center gap-2 mb-3">
                                        {activeTab === 'background' ? (
                                            <>
                                                <Scroll className="w-5 h-5 text-blue-500"/>
                                                <span className="font-semibold text-blue-700">
                          背景故事
                        </span>
                                            </>
                                        ) : (
                                            <>
                                                <Lock className="w-5 h-5 text-rose-500"/>
                                                <span className="font-semibold text-rose-700">
                          角色秘密
                        </span>
                                            </>
                                        )}
                                    </div>

                                    {/* 背景故事内容 - 仅显示前几行 */}
                                    {activeTab === 'background' ? (
                                        <p className="text-sm leading-relaxed whitespace-pre-wrap text-slate-600">
                                            {displayBackgroundStory}
                                        </p>
                                    ) : (
                                        /* 角色秘密 - 不直接显示 */
                                        <div className="flex flex-col items-center justify-center py-8 text-center">
                                            <div
                                                className="w-16 h-16 rounded-full bg-rose-100 flex items-center justify-center mb-4">
                                                <Lock className="w-8 h-8 text-rose-400"/>
                                            </div>
                                            <p className="text-rose-600 font-medium mb-2">
                                                游玩剧本后可阅读
                                            </p>
                                            <p className="text-slate-400 text-sm">
                                                开始游戏后解锁角色秘密
                                            </p>
                                        </div>
                                    )}
                                </motion.div>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>

                {/* 底部操作栏 */}
                <div className="px-6 pb-6">
                    <button
                        onClick={() => setIsExpanded(!isExpanded)}
                        className="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-slate-100 hover:bg-blue-50 text-slate-600 hover:text-blue-600 transition-all duration-300 group/btn"
                    >
            <span className="font-medium">
              {isExpanded ? '收起详情' : '查看详情'}
            </span>
                        <motion.div
                            animate={{rotate: isExpanded ? 180 : 0}}
                            transition={{duration: 0.3}}
                        >
                            <ChevronDown className="w-5 h-5"/>
                        </motion.div>
                    </button>
                </div>
            </div>
        </motion.div>
    )
}

export default CharacterCard

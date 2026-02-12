import {useEffect, useState} from 'react'
import {motion} from 'framer-motion'
import {useNavigate} from 'react-router-dom'
import {BookOpen, Filter, Search, Sparkles} from 'lucide-react'
import ScriptCard from '../../components/ui/ScriptCard'
import {getScripts} from '../../services/api/script'

/**
 * ScriptList 页面 - 剧本列表
 * 展示所有可用的剧本，支持搜索、筛选功能
 */
const ScriptList = () => {
    const navigate = useNavigate()
    const [scripts, setScripts] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [searchQuery, setSearchQuery] = useState('')
    const [filterDifficulty, setFilterDifficulty] = useState('ALL')

    // 获取剧本列表
    useEffect(() => {
        const fetchScripts = async () => {
            setLoading(true)
            setError(null)
            try {
                const data = await getScripts()
                // 确保数据是数组
                if (Array.isArray(data)) {
                    setScripts(data)
                } else if (data && Array.isArray(data.data)) {
                    // 处理包装在 data 字段的情况
                    setScripts(data.data)
                } else if (data && typeof data === 'object') {
                    // 如果是单个对象，转换为数组
                    setScripts([data])
                } else {
                    setScripts([])
                }
            } catch (err) {
                console.error('获取剧本列表失败:', err)
                setError('获取剧本列表失败，请稍后重试')
                setScripts([])
            } finally {
                setLoading(false)
            }
        }

        fetchScripts()
    }, [])

    // 筛选逻辑 - 添加空值检查
    const filteredScripts = Array.isArray(scripts)
        ? scripts.filter((script) => {
            if (!script) return false
            const matchesSearch =
                script.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                script.description?.toLowerCase().includes(searchQuery.toLowerCase())
            const matchesDifficulty =
                filterDifficulty === 'ALL' || script.difficulty === filterDifficulty
            return matchesSearch && matchesDifficulty
        })
        : []

    // 处理剧本点击
    const handleScriptClick = (id) => {
        navigate(`/scripts/${id}`)
    }

    // 处理创建剧本
    const handleCreateScript = () => {
        navigate('/scripts/create')
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50">
            {/* 背景装饰 */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-20 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"/>
                <div className="absolute top-1/3 -left-20 w-80 h-80 bg-blue-600/10 rounded-full blur-3xl"/>
                <div className="absolute bottom-20 right-1/4 w-72 h-72 bg-blue-300/10 rounded-full blur-3xl"/>
            </div>

            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 pt-24">
                {/* 页面标题 */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6}}
                    className="mb-10"
                >
                    <div className="flex items-center gap-3 mb-4">
                        <div
                            className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center shadow-lg shadow-blue-500/30">
                            <BookOpen className="w-6 h-6 text-white" strokeWidth={1.5}/>
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-slate-800">剧本库</h1>
                            <p className="text-slate-500 mt-1">探索精彩的推理剧本，开启你的侦探之旅</p>
                        </div>
                    </div>
                </motion.div>

                {/* 搜索和筛选栏 */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6, delay: 0.1}}
                    className="mb-8"
                >
                    <div className="flex flex-col sm:flex-row gap-4">
                        {/* 搜索框 */}
                        <div className="relative flex-1">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400"/>
                            <input
                                type="text"
                                placeholder="搜索剧本名称或描述..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="w-full pl-12 pr-4 py-3 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-300 outline-none text-slate-700 placeholder-slate-400"
                            />
                        </div>

                        {/* 难度筛选 */}
                        <div className="relative">
                            <Filter className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400"/>
                            <select
                                value={filterDifficulty}
                                onChange={(e) => setFilterDifficulty(e.target.value)}
                                className="pl-12 pr-10 py-3 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-300 outline-none text-slate-700 appearance-none cursor-pointer min-w-[140px]"
                            >
                                <option value="ALL">全部难度</option>
                                <option value="EASY">简单</option>
                                <option value="MEDIUM">中等</option>
                                <option value="HARD">困难</option>
                            </select>
                            <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
                                <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M19 9l-7 7-7-7"/>
                                </svg>
                            </div>
                        </div>

                        {/* 创建按钮 */}
                        <motion.button
                            whileHover={{scale: 1.02}}
                            whileTap={{scale: 0.98}}
                            onClick={handleCreateScript}
                            className="inline-flex items-center justify-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-xl hover:shadow-blue-500/40 transition-all duration-300"
                        >
                            <Sparkles className="w-5 h-5"/>
                            <span>AI 生成剧本</span>
                        </motion.button>
                    </div>
                </motion.div>

                {/* 剧本列表 */}
                {error ? (
                    // 错误状态
                    <motion.div
                        initial={{opacity: 0, scale: 0.95}}
                        animate={{opacity: 1, scale: 1}}
                        className="flex flex-col items-center justify-center py-20"
                    >
                        <div className="w-24 h-24 rounded-full bg-red-50 flex items-center justify-center mb-4">
                            <svg className="w-10 h-10 text-red-400" fill="none" stroke="currentColor"
                                 viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                            </svg>
                        </div>
                        <h3 className="text-xl font-semibold text-slate-700 mb-2">加载失败</h3>
                        <p className="text-slate-500 mb-6">{error}</p>
                        <button
                            onClick={() => window.location.reload()}
                            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                        >
                            重新加载
                        </button>
                    </motion.div>
                ) : loading ? (
                    // 加载状态
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {[1, 2, 3, 4, 5, 6].map((i) => (
                            <div
                                key={i}
                                className="rounded-2xl bg-white/60 backdrop-blur-sm border border-white/60 p-6 animate-pulse"
                            >
                                <div className="h-48 bg-slate-200 rounded-xl mb-4"/>
                                <div className="h-6 bg-slate-200 rounded w-3/4 mb-2"/>
                                <div className="h-4 bg-slate-200 rounded w-full mb-2"/>
                                <div className="h-4 bg-slate-200 rounded w-2/3"/>
                            </div>
                        ))}
                    </div>
                ) : filteredScripts.length > 0 ? (
                    // 剧本网格
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredScripts.map((script, index) => (
                            <ScriptCard
                                key={script.id}
                                id={script.id}
                                name={script.name}
                                description={script.description}
                                author={script.author}
                                difficulty={script.difficulty}
                                playerCount={script.playerCount}
                                duration={script.duration}
                                coverImageUrl={script.coverImageUrl}
                                index={index}
                                onClick={handleScriptClick}
                            />
                        ))}
                    </div>
                ) : (
                    // 空状态
                    <motion.div
                        initial={{opacity: 0, scale: 0.95}}
                        animate={{opacity: 1, scale: 1}}
                        className="flex flex-col items-center justify-center py-20"
                    >
                        <div className="w-24 h-24 rounded-full bg-slate-100 flex items-center justify-center mb-4">
                            <Search className="w-10 h-10 text-slate-300"/>
                        </div>
                        <h3 className="text-xl font-semibold text-slate-700 mb-2">未找到相关剧本</h3>
                        <p className="text-slate-500">尝试调整搜索条件或筛选条件</p>
                    </motion.div>
                )}

                {/* 统计信息 */}
                {!loading && (
                    <motion.div
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        transition={{delay: 0.5}}
                        className="mt-12 text-center text-sm text-slate-400"
                    >
                        共 {filteredScripts.length} 个剧本
                        {searchQuery && ` · 搜索 "${searchQuery}"`}
                        {filterDifficulty !== 'ALL' && ` · 难度筛选`}
                    </motion.div>
                )}
            </div>
        </div>
    )
}

export default ScriptList
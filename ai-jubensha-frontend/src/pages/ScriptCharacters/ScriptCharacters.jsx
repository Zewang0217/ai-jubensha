import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {motion} from 'framer-motion'
import {AlertCircle, ArrowLeft, BookOpen, RefreshCw, User, Users,} from 'lucide-react'
import CharacterCard from '../../components/ui/CharacterCard'
import {getCharactersByScriptId} from '../../services/api/character'
import {getScriptById} from '../../services/api/script'

/**
 * ScriptCharacters 页面 - 剧本角色列表
 * 展示某剧本的所有角色详情
 */
const ScriptCharacters = () => {
    const {scriptId} = useParams()
    const navigate = useNavigate()
    const [characters, setCharacters] = useState([])
    const [scriptName, setScriptName] = useState('')
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchData = async () => {
            if (!scriptId) return

            try {
                setLoading(true)
                setError(null)

                const [charactersData, scriptData] = await Promise.all([
                    getCharactersByScriptId(scriptId),
                    getScriptById(scriptId).catch(() => null),
                ])

                setCharacters(charactersData || [])
                setScriptName(scriptData?.name || '未知剧本')
            } catch (err) {
                console.error('获取角色列表失败:', err)
                setError(err.message || '获取角色列表失败')
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [scriptId])

    const handleBack = () => navigate(`/scripts/${scriptId}`)
    const handleRetry = () => window.location.reload()

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    <div className="animate-pulse">
                        <div className="h-8 bg-slate-200 rounded w-48 mb-4"/>
                        <div className="h-6 bg-slate-200 rounded w-64 mb-8"/>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {[1, 2, 3, 4].map((i) => (
                                <div
                                    key={i}
                                    className="rounded-2xl bg-white/60 backdrop-blur-sm border border-white/60 p-6"
                                >
                                    <div className="flex items-start gap-4">
                                        <div className="w-20 h-20 bg-slate-200 rounded-2xl"/>
                                        <div className="flex-1 space-y-2">
                                            <div className="h-6 bg-slate-200 rounded w-1/3"/>
                                            <div className="h-4 bg-slate-200 rounded w-full"/>
                                            <div className="h-4 bg-slate-200 rounded w-2/3"/>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    <motion.div
                        initial={{opacity: 0, scale: 0.95}}
                        animate={{opacity: 1, scale: 1}}
                        className="flex flex-col items-center justify-center py-20"
                    >
                        <div className="w-24 h-24 rounded-full bg-red-50 flex items-center justify-center mb-6">
                            <AlertCircle className="w-12 h-12 text-red-400"/>
                        </div>
                        <h3 className="text-2xl font-semibold text-slate-800 mb-2">
                            加载失败
                        </h3>
                        <p className="text-slate-500 mb-6 text-center max-w-md">{error}</p>
                        <div className="flex gap-4">
                            <button
                                onClick={handleRetry}
                                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-xl transition-all duration-300"
                            >
                                <RefreshCw className="w-5 h-5"/>
                                重新加载
                            </button>
                            <button
                                onClick={handleBack}
                                className="inline-flex items-center gap-2 px-6 py-3 bg-white/80 backdrop-blur-sm text-slate-700 font-semibold rounded-xl border border-slate-200 hover:border-blue-300 hover:bg-white transition-all duration-300"
                            >
                                <ArrowLeft className="w-5 h-5"/>
                                返回剧本
                            </button>
                        </div>
                    </motion.div>
                </div>
            </div>
        )
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
            {/* 背景装饰 */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -left-20 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"/>
                <div className="absolute top-1/3 -right-20 w-80 h-80 bg-blue-600/10 rounded-full blur-3xl"/>
                <div className="absolute bottom-20 left-1/4 w-72 h-72 bg-blue-300/10 rounded-full blur-3xl"/>
            </div>

            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* 返回按钮 */}
                <motion.button
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    onClick={handleBack}
                    className="group inline-flex items-center text-slate-500 hover:text-blue-600 transition-colors duration-300 mb-6"
                >
                    <motion.div
                        whileHover={{x: -4}}
                        className="w-10 h-10 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200 flex items-center justify-center mr-3 group-hover:border-blue-300 group-hover:bg-blue-50 transition-all duration-300"
                    >
                        <ArrowLeft className="w-5 h-5"/>
                    </motion.div>
                    <span className="font-medium">返回剧本详情</span>
                </motion.button>

                {/* 页面标题 */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6}}
                    className="mb-10"
                >
                    <div className="flex items-center gap-3 mb-4">
                        <div
                            className="w-12 h-12 rounded-xl bg-gradient-to-br from-violet-500 to-violet-700 flex items-center justify-center shadow-lg shadow-violet-500/30">
                            <Users className="w-6 h-6 text-white" strokeWidth={1.5}/>
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-slate-800">角色列表</h1>
                            <p className="text-slate-500 mt-1 flex items-center gap-2">
                                <BookOpen className="w-4 h-4"/>
                                <span>{scriptName}</span>
                            </p>
                        </div>
                    </div>
                    <p className="text-slate-600 max-w-2xl">
                        了解每个角色的背景故事和秘密，帮助你更好地融入剧情，推理真相。
                    </p>
                </motion.div>

                {/* 角色统计 */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6, delay: 0.1}}
                    className="mb-8"
                >
                    <div
                        className="inline-flex items-center gap-3 px-5 py-3 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200">
                        <div className="flex items-center gap-2">
                            <User className="w-5 h-5 text-blue-500"/>
                            <span className="text-sm text-slate-600">角色数量</span>
                        </div>
                        <div className="w-px h-4 bg-slate-200"/>
                        <span className="text-lg font-bold text-slate-800">
              {characters.length}
            </span>
                    </div>
                </motion.div>

                {/* 角色列表 */}
                {characters.length > 0 ? (
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {characters.map((character, index) => (
                            <CharacterCard
                                key={character.id}
                                _id={character.id}
                                name={character.name}
                                description={character.description}
                                backgroundStory={character.backgroundStory}
                                secret={character.secret}
                                avatarUrl={character.avatarUrl}
                                index={index}
                            />
                        ))}
                    </div>
                ) : (
                    <motion.div
                        initial={{opacity: 0, scale: 0.95}}
                        animate={{opacity: 1, scale: 1}}
                        className="flex flex-col items-center justify-center py-20"
                    >
                        <div className="w-24 h-24 rounded-full bg-slate-100 flex items-center justify-center mb-4">
                            <Users className="w-10 h-10 text-slate-300"/>
                        </div>
                        <h3 className="text-xl font-semibold text-slate-700 mb-2">
                            暂无角色
                        </h3>
                        <p className="text-slate-500">该剧本暂未添加角色</p>
                    </motion.div>
                )}

                {/* 底部提示 */}
                {characters.length > 0 && (
                    <motion.div
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        transition={{delay: 0.8}}
                        className="mt-12 text-center"
                    >
                        <div
                            className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-amber-50 border border-amber-100">
                            <AlertCircle className="w-5 h-5 text-amber-500"/>
                            <span className="text-sm text-amber-700">
                提示：点击"查看详情"可阅读完整背景故事和角色秘密
              </span>
                        </div>
                    </motion.div>
                )}
            </div>
        </div>
    )
}

export default ScriptCharacters
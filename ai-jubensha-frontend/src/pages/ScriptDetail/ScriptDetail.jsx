import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {motion} from 'framer-motion'
import {ArrowLeft, BookOpen, Calendar, ChevronRight, Clock, Play, Sparkles, User, Users,} from 'lucide-react'
import {getScriptById} from '../../services/api/script'

/**
 * ScriptDetail 页面 - 剧本详情
 * 展示剧本的详细信息，包括封面、描述、难度、时长等
 * 提供"从此剧本开始游戏"按钮
 */
const ScriptDetail = () => {
    const {id} = useParams()
    const navigate = useNavigate()
    const [script, setScript] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    // 获取剧本详情
    useEffect(() => {
        const fetchScript = async () => {
            if (!id) return

            try {
                setLoading(true)
                setError(null)
                const data = await getScriptById(id)
                setScript(data)
            } catch (err) {
                console.error('获取剧本详情失败:', err)
                setError(err.message || '获取剧本详情失败')
            } finally {
                setLoading(false)
            }
        }

        fetchScript()
    }, [id])

    // 返回剧本列表
    const handleBack = () => {
        navigate('/scripts')
    }

    // 开始游戏
    const handleStartGame = () => {
        // TODO: 调用创建游戏 API，传入剧本 ID
        console.log('从此剧本开始游戏:', script?.id)
        // navigate(`/games/create?scriptId=${script?.id}`)
    }

    // 难度映射
    const difficultyMap = {
        EASY: {text: '简单', color: 'bg-emerald-100 text-emerald-700 border-emerald-200'},
        MEDIUM: {text: '中等', color: 'bg-amber-100 text-amber-700 border-amber-200'},
        HARD: {text: '困难', color: 'bg-rose-100 text-rose-700 border-rose-200'},
    }

    // 格式化日期
    const formatDate = (dateString) => {
        if (!dateString) return '-'
        const date = new Date(dateString)
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        })
    }

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    {/* 骨架屏 */}
                    <div className="animate-pulse">
                        <div className="h-8 bg-slate-200 rounded w-32 mb-8"/>
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
                            <div className="h-96 bg-slate-200 rounded-2xl"/>
                            <div className="space-y-4">
                                <div className="h-10 bg-slate-200 rounded w-3/4"/>
                                <div className="h-6 bg-slate-200 rounded w-1/2"/>
                                <div className="h-32 bg-slate-200 rounded"/>
                                <div className="h-12 bg-slate-200 rounded w-48"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    if (error || !script) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    <motion.div
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        className="text-center py-20"
                    >
                        <div
                            className="w-20 h-20 mx-auto mb-6 rounded-full bg-rose-100 flex items-center justify-center">
                            <BookOpen className="w-10 h-10 text-rose-500"/>
                        </div>
                        <h3 className="text-xl font-semibold text-slate-800 mb-2">
                            {error || '剧本不存在'}
                        </h3>
                        <p className="text-slate-500 mb-6">请检查剧本ID或返回剧本列表</p>
                        <button
                            onClick={handleBack}
                            className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-xl hover:shadow-blue-500/40 transition-all duration-300"
                        >
                            <ArrowLeft className="w-5 h-5 mr-2"/>
                            返回剧本列表
                        </button>
                    </motion.div>
                </div>
            </div>
        )
    }

    const difficultyInfo = difficultyMap[script.difficulty] || difficultyMap.MEDIUM

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 pt-20">
            {/* 背景装饰 */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-20 w-96 h-96 bg-blue-400/10 rounded-full blur-3xl"/>
                <div className="absolute top-1/2 -left-20 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl"/>
                <div className="absolute bottom-20 right-1/4 w-64 h-64 bg-blue-300/10 rounded-full blur-3xl"/>
            </div>

            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* 返回按钮 */}
                <motion.button
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    onClick={handleBack}
                    className="group inline-flex items-center text-slate-500 hover:text-blue-600 transition-colors duration-300 mb-8"
                >
                    <motion.div
                        whileHover={{x: -4}}
                        className="w-10 h-10 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200 flex items-center justify-center mr-3 group-hover:border-blue-300 group-hover:bg-blue-50 transition-all duration-300"
                    >
                        <ArrowLeft className="w-5 h-5"/>
                    </motion.div>
                    <span className="font-medium">返回剧本列表</span>
                </motion.button>

                {/* 主内容区 */}
                <div className="grid grid-cols-1 lg:grid-cols-5 gap-8 lg:gap-12">
                    {/* 左侧：封面图片 */}
                    <motion.div
                        initial={{opacity: 0, x: -30}}
                        animate={{opacity: 1, x: 0}}
                        transition={{duration: 0.6, ease: [0.25, 0.46, 0.45, 0.94]}}
                        className="lg:col-span-2"
                    >
                        <div
                            className="relative aspect-[3/4] rounded-2xl overflow-hidden shadow-2xl shadow-blue-900/10">
                            {/* 封面图片 */}
                            <div
                                className="absolute inset-0 bg-gradient-to-br from-blue-400 to-blue-600"
                                style={
                                    script.coverImageUrl
                                        ? {
                                            backgroundImage: `url(${script.coverImageUrl})`,
                                            backgroundSize: 'cover',
                                            backgroundPosition: 'center',
                                        }
                                        : {}
                                }
                            >
                                {!script.coverImageUrl && (
                                    <div className="absolute inset-0 flex items-center justify-center">
                                        <BookOpen className="w-32 h-32 text-white/30" strokeWidth={1}/>
                                    </div>
                                )}
                            </div>

                            {/* 渐变遮罩 */}
                            <div
                                className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"/>

                            {/* 难度标签 */}
                            <div className="absolute top-6 left-6">
                <span
                    className={`inline-flex items-center px-4 py-2 rounded-full text-sm font-semibold border ${difficultyInfo.color}`}
                >
                  {difficultyInfo.text}
                </span>
                            </div>

                            {/* 底部信息 */}
                            <div className="absolute bottom-0 left-0 right-0 p-6 text-white">
                                <div className="flex items-center gap-4 text-sm">
                                    <div className="flex items-center gap-2">
                                        <Users className="w-5 h-5"/>
                                        <span>{script.playerCount} 人</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Clock className="w-5 h-5"/>
                                        <span>{script.duration} 分钟</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </motion.div>

                    {/* 右侧：剧本信息 */}
                    <motion.div
                        initial={{opacity: 0, x: 30}}
                        animate={{opacity: 1, x: 0}}
                        transition={{duration: 0.6, delay: 0.1, ease: [0.25, 0.46, 0.45, 0.94]}}
                        className="lg:col-span-3"
                    >
                        {/* 标题区域 */}
                        <div className="mb-8">
                            <motion.h1
                                initial={{opacity: 0, y: 20}}
                                animate={{opacity: 1, y: 0}}
                                transition={{delay: 0.2}}
                                className="text-4xl md:text-5xl font-bold text-slate-800 mb-4 leading-tight"
                            >
                                {script.name}
                            </motion.h1>

                            {/* 作者信息 */}
                            <motion.div
                                initial={{opacity: 0, y: 20}}
                                animate={{opacity: 1, y: 0}}
                                transition={{delay: 0.3}}
                                className="flex items-center gap-6 text-slate-500"
                            >
                                <div className="flex items-center gap-2">
                                    <User className="w-4 h-4"/>
                                    <span>作者：{script.author || '未知'}</span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4"/>
                                    <span>创建时间：{formatDate(script.createTime)}</span>
                                </div>
                            </motion.div>
                        </div>

                        {/* 描述 */}
                        <motion.div
                            initial={{opacity: 0, y: 20}}
                            animate={{opacity: 1, y: 0}}
                            transition={{delay: 0.4}}
                            className="mb-8"
                        >
                            <h2 className="text-lg font-semibold text-slate-800 mb-4 flex items-center gap-2">
                                <BookOpen className="w-5 h-5 text-blue-500"/>
                                剧本简介
                            </h2>
                            <div
                                className="p-6 rounded-2xl bg-gradient-to-br from-white/90 to-white/60 backdrop-blur-xl border border-white/70 shadow-lg shadow-blue-900/5">
                                <p className="text-slate-600 leading-relaxed whitespace-pre-wrap">
                                    {script.description || '暂无描述'}
                                </p>
                            </div>
                        </motion.div>

                        {/* 信息卡片 */}
                        <motion.div
                            initial={{opacity: 0, y: 20}}
                            animate={{opacity: 1, y: 0}}
                            transition={{delay: 0.5}}
                            className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8"
                        >
                            {[
                                {icon: Users, label: '玩家人数', value: `${script.playerCount} 人`},
                                {icon: Clock, label: '游戏时长', value: `${script.duration} 分钟`},
                                {icon: Sparkles, label: '难度等级', value: difficultyInfo.text},
                                {icon: Calendar, label: '更新时间', value: formatDate(script.updateTime)},
                            ].map((item, index) => (
                                <motion.div
                                    key={item.label}
                                    initial={{opacity: 0, y: 20}}
                                    animate={{opacity: 1, y: 0}}
                                    transition={{delay: 0.5 + index * 0.1}}
                                    className="p-4 rounded-xl bg-white/80 backdrop-blur-sm border border-slate-200 text-center group hover:border-blue-300 hover:bg-blue-50/50 transition-all duration-300"
                                >
                                    <item.icon className="w-6 h-6 mx-auto mb-2 text-blue-500"/>
                                    <p className="text-xs text-slate-400 mb-1">{item.label}</p>
                                    <p className="text-sm font-semibold text-slate-700">{item.value}</p>
                                </motion.div>
                            ))}
                        </motion.div>

                        {/* 操作按钮 */}
                        <motion.div
                            initial={{opacity: 0, y: 20}}
                            animate={{opacity: 1, y: 0}}
                            transition={{delay: 0.8}}
                            className="flex flex-col sm:flex-row gap-4"
                        >
                            {/* 开始游戏按钮 */}
                            <motion.button
                                whileHover={{scale: 1.02, y: -2}}
                                whileTap={{scale: 0.98}}
                                onClick={handleStartGame}
                                className="group inline-flex items-center justify-center gap-3 px-8 py-4 bg-gradient-to-r from-blue-500 to-blue-700 text-white text-lg font-bold rounded-2xl shadow-xl shadow-blue-500/30 hover:shadow-2xl hover:shadow-blue-500/40 transition-all duration-300"
                            >
                                <Play className="w-6 h-6 group-hover:scale-110 transition-transform duration-300"/>
                                <span>从此剧本开始游戏</span>
                                <ChevronRight
                                    className="w-5 h-5 group-hover:translate-x-1 transition-transform duration-300"/>
                            </motion.button>

                            {/* 返回按钮 */}
                            <motion.button
                                whileHover={{scale: 1.02}}
                                whileTap={{scale: 0.98}}
                                onClick={handleBack}
                                className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-white/80 backdrop-blur-sm text-slate-700 text-lg font-semibold rounded-2xl border border-slate-200 hover:border-blue-300 hover:bg-white hover:text-blue-600 shadow-lg shadow-slate-200/50 transition-all duration-300"
                            >
                                <ArrowLeft className="w-5 h-5"/>
                                <span>返回列表</span>
                            </motion.button>
                        </motion.div>
                    </motion.div>
                </div>
            </div>
        </div>
    )
}

export default ScriptDetail

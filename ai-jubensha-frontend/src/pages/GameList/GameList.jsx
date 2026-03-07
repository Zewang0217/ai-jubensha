/**
 * @fileoverview GameList 组件 - 科技感游戏大厅界面
 * @description 展示游戏房间列表，支持筛选和创建新房间
 * @author zewang
 * 
 * 设计特点：
 * - 科技感游戏大厅风格
 * - 玻璃态卡片设计
 * - 动态背景装饰
 * - 流畅的交互动画
 */

import {useMemo, useState} from 'react'
import {Link} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {AnimatePresence, motion} from 'framer-motion'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'
import {Clock, Gamepad2, Plus, Search, Sparkles, Users} from 'lucide-react'

function GameList() {
    const [filter, setFilter] = useState('all')
    const [searchQuery, setSearchQuery] = useState('')

    const {data: games, isLoading, error} = useQuery({
        queryKey: ['games', filter],
        queryFn: () => gameApi.getGames({status: filter === 'all' ? undefined : filter}),
    })

    const filters = [
        {key: 'all', label: '全部', color: 'blue'},
        {key: 'waiting', label: '等待中', color: 'emerald'},
        {key: 'playing', label: '游戏中', color: 'blue'},
        {key: 'finished', label: '已结束', color: 'slate'},
    ]

    const getStatusConfig = (status) => {
        const configs = {
            waiting: {
                text: '等待中',
                bgColor: 'bg-emerald-100',
                textColor: 'text-emerald-700',
                borderColor: 'border-emerald-200',
                dotColor: 'bg-emerald-500',
            },
            playing: {
                text: '游戏中',
                bgColor: 'bg-blue-100',
                textColor: 'text-blue-700',
                borderColor: 'border-blue-200',
                dotColor: 'bg-blue-500',
            },
            finished: {
                text: '已结束',
                bgColor: 'bg-slate-100',
                textColor: 'text-slate-600',
                borderColor: 'border-slate-200',
                dotColor: 'bg-slate-400',
            },
        }
        return configs[status] || configs.waiting
    }

    const filteredGames = useMemo(() => {
        if (!games?.data) return []
        if (!searchQuery) return games.data
        
        return games.data.filter(game => 
            (game.name?.toLowerCase().includes(searchQuery.toLowerCase())) ||
            (game.scriptName?.toLowerCase().includes(searchQuery.toLowerCase()))
        )
    }, [games?.data, searchQuery])

    const gameCounts = useMemo(() => {
        const all = games?.data?.length || 0
        const waiting = games?.data?.filter(g => g.status === 'waiting').length || 0
        const playing = games?.data?.filter(g => g.status === 'playing').length || 0
        const finished = games?.data?.filter(g => g.status === 'finished').length || 0
        return {all, waiting, playing, finished}
    }, [games?.data])

    if (isLoading) {
        return (
            <Loading
                fullScreen
                text="正在加载游戏大厅..."
                className="bg-gradient-to-br from-[#EFF6FF] via-white to-[#DBEAFE]/30"
            />
        )
    }

    if (error) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-[#EFF6FF] via-white to-[#DBEAFE]/30 flex items-center justify-center p-4 pt-24 pb-12">
                <motion.div
                    initial={{opacity: 0, scale: 0.95}}
                    animate={{opacity: 1, scale: 1}}
                    className="max-w-md w-full bg-white/80 backdrop-blur-xl border border-[#E2E8F0] rounded-2xl shadow-xl p-8 text-center"
                >
                    <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-100 flex items-center justify-center">
                        <Gamepad2 className="w-8 h-8 text-red-500"/>
                    </div>
                    <h2 className="text-2xl font-bold text-[#0F172A] mb-2">加载失败</h2>
                    <p className="text-[#64748B] mb-6">无法获取游戏列表，请稍后重试</p>
                    <button
                        onClick={() => window.location.reload()}
                        className="px-6 py-3 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white rounded-xl font-medium hover:shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] transition-all"
                    >
                        重新加载
                    </button>
                </motion.div>
            </div>
        )
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#EFF6FF] via-[#FFFFFF] to-[#F8FAFC] relative overflow-hidden">
            <BackgroundDecoration/>

            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-12">
                <HeroSection/>
                
                <FilterSection
                    filters={filters}
                    activeFilter={filter}
                    onFilterChange={setFilter}
                    searchQuery={searchQuery}
                    onSearchChange={setSearchQuery}
                    gameCounts={gameCounts}
                />
                
                <GameGrid games={filteredGames} getStatusConfig={getStatusConfig}/>
            </div>
        </div>
    )
}

function BackgroundDecoration() {
    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-[#EFF6FF] via-[#FFFFFF] to-[#F8FAFC]"/>
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5}}
                className="absolute top-0 -left-32 w-96 h-96 bg-[#2563EB]/20 rounded-full blur-3xl"
            />
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5, delay: 0.2}}
                className="absolute bottom-0 -right-32 w-96 h-96 bg-[#08D9D6]/20 rounded-full blur-3xl"
            />
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5, delay: 0.4}}
                className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-[#2563EB]/10 to-[#08D9D6]/10 rounded-full blur-3xl"
            />
            
            <div className="absolute inset-0 bg-[linear-gradient(rgba(37,99,235,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(37,99,235,0.03)_1px,transparent_1px)] bg-[size:60px_60px]"/>
            
            <motion.div
                animate={{y: [0, -20, 0], rotate: [0, 5, 0]}}
                transition={{duration: 6, repeat: Infinity, ease: "easeInOut"}}
                className="absolute top-32 right-[15%] w-16 h-16 rounded-2xl bg-gradient-to-br from-[#2563EB]/30 to-[#08D9D6]/30 backdrop-blur-sm border border-[#2563EB]/30"
            />
            
            <motion.div
                animate={{y: [0, 15, 0], rotate: [0, -5, 0]}}
                transition={{duration: 5, repeat: Infinity, ease: "easeInOut", delay: 1}}
                className="absolute bottom-40 left-[10%] w-12 h-12 rounded-xl bg-gradient-to-br from-[#2563EB]/30 to-[#08D9D6]/30 backdrop-blur-sm border border-[#2563EB]/30"
            />
            
            <motion.div
                animate={{y: [0, -10, 0], x: [0, 10, 0]}}
                transition={{duration: 4, repeat: Infinity, ease: "easeInOut", delay: 0.5}}
                className="absolute top-1/3 left-[20%] w-8 h-8 rounded-lg bg-gradient-to-br from-[#2563EB]/40 to-[#08D9D6]/40 backdrop-blur-sm"
            />
        </div>
    )
}

function HeroSection() {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
            className="text-center mb-12"
        >
            <motion.div
                initial={{opacity: 0, scale: 0.9}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.5}}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-[#EFF6FF]/80 backdrop-blur-sm border border-[#DBEAFE] mb-6"
            >
                <Sparkles className="w-4 h-4 text-[#2563EB]"/>
                <span className="text-[#2563EB] text-sm font-medium">AI 驱动的智能游戏大厅</span>
            </motion.div>
            
            <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold mb-4 tracking-tight">
                <span className="bg-gradient-to-r from-[#2563EB] to-[#08D9D6] bg-clip-text text-transparent">
                    游戏大厅
                </span>
            </h1>
            
            <p className="text-lg text-[#64748B] max-w-2xl mx-auto mb-8">
                选择房间加入或创建新游戏，开启你的推理之旅
            </p>
            
            <motion.button
                whileHover={{scale: 1.02}}
                whileTap={{scale: 0.98}}
                className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white text-lg font-semibold rounded-2xl shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] hover:shadow-[0_6px_20px_0_rgba(37,99,235,0.5)] transition-all duration-300"
            >
                <Plus className="w-5 h-5"/>
                创建房间
            </motion.button>
        </motion.div>
    )
}

function FilterSection({filters, activeFilter, onFilterChange, searchQuery, onSearchChange, gameCounts}) {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6, delay: 0.1}}
            className="mb-8"
        >
            <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
                <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
                    {filters.map((f, index) => (
                        <motion.button
                            key={f.key}
                            initial={{opacity: 0, scale: 0.9}}
                            animate={{opacity: 1, scale: 1}}
                            transition={{duration: 0.3, delay: index * 0.05}}
                            onClick={() => onFilterChange(f.key)}
                            className={`
                                relative px-5 py-2.5 rounded-xl font-medium text-sm transition-all duration-300
                                ${activeFilter === f.key
                                    ? 'bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white shadow-[0_4px_14px_0_rgba(37,99,235,0.39)]'
                                    : 'bg-white/80 backdrop-blur-sm text-[#64748B] border border-[#E2E8F0] hover:border-[#2563EB] hover:text-[#2563EB] hover:shadow-[0_4px_14px_0_rgba(37,99,235,0.39)]'
                                }
                            `}
                        >
                            <span className="flex items-center gap-2">
                                {f.label}
                                <span className={`
                                    px-2 py-0.5 rounded-full text-xs font-bold
                                    ${activeFilter === f.key
                                        ? 'bg-white/20 text-white'
                                        : 'bg-[#F1F5F9] text-[#64748B]'
                                    }
                                `}>
                                    {gameCounts[f.key]}
                                </span>
                            </span>
                        </motion.button>
                    ))}
                </div>
                
                <div className="relative w-full sm:w-64">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-[#94A3B8]"/>
                    <input
                        type="text"
                        placeholder="搜索房间或剧本..."
                        value={searchQuery}
                        onChange={(e) => onSearchChange(e.target.value)}
                        className="w-full pl-10 pr-4 py-2.5 bg-white/80 backdrop-blur-sm border border-[#E2E8F0] rounded-xl text-[#0F172A] placeholder-[#94A3B8] focus:outline-none focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/20 transition-all"
                    />
                </div>
            </div>
        </motion.div>
    )
}

function GameGrid({games, getStatusConfig}) {
    if (!games || games.length === 0) {
        return <EmptyState/>
    }

    return (
        <motion.div
            initial={{opacity: 0}}
            animate={{opacity: 1}}
            transition={{duration: 0.6, delay: 0.2}}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
        >
            <AnimatePresence mode="popLayout">
                {games.map((game, index) => (
                    <GameCard
                        key={game.id}
                        game={game}
                        statusConfig={getStatusConfig(game.status)}
                        index={index}
                    />
                ))}
            </AnimatePresence>
        </motion.div>
    )
}

/**
 * 游戏卡片组件
 * @param {Object} game - 游戏数据对象
 * @param {Object} statusConfig - 状态配置对象
 * @param {number} index - 卡片索引，用于动画延迟
 * @returns {JSX.Element} 游戏卡片组件
 */
function GameCard({game, statusConfig, index}) {
    const playerCount = game.currentPlayers || 0
    const maxPlayers = game.maxPlayers || 8
    const playerPercentage = (playerCount / maxPlayers) * 100

    return (
        <motion.div
            layout
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            exit={{opacity: 0, scale: 0.9}}
            transition={{
                duration: 0.4,
                delay: index * 0.05,
                layout: {duration: 0.3}
            }}
            whileHover={{y: -4}}
            className="group relative"
        >
            {/* 毛玻璃卡片背景 + 悬浮发光阴影 */}
            <div className="absolute inset-0 bg-white/80 backdrop-blur-[10px] rounded-2xl border border-[#E2E8F0] shadow-lg transition-all duration-500 group-hover:shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] group-hover:border-[#2563EB]/30"/>
            
            {/* 悬浮时的渐变光晕效果 */}
            <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-[#2563EB]/0 via-[#08D9D6]/0 to-[#2563EB]/0 opacity-0 group-hover:opacity-5 transition-opacity duration-500"/>
            
            <Link to={`/game/${game.id}`} className="relative block p-6">
                <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                        <h3 className="text-lg font-bold text-[#0F172A] mb-1 group-hover:text-[#2563EB] transition-colors">
                            {game.name || `游戏房间 #${game.id}`}
                        </h3>
                        <p className="text-sm text-[#64748B]">
                            {game.scriptName || '未选择剧本'}
                        </p>
                    </div>
                    
                    <div className={`px-3 py-1.5 rounded-lg ${statusConfig.bgColor} ${statusConfig.textColor} border ${statusConfig.borderColor} flex items-center gap-1.5`}>
                        <span className={`w-2 h-2 rounded-full ${statusConfig.dotColor} ${game.status === 'playing' ? 'animate-pulse' : ''}`}/>
                        <span className="text-xs font-medium">{statusConfig.text}</span>
                    </div>
                </div>
                
                <div className="space-y-3 mb-4">
                    <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-2 text-[#64748B]">
                            <Users className="w-4 h-4"/>
                            <span>玩家</span>
                        </div>
                        <span className="font-medium text-[#0F172A]">
                            {playerCount} / {maxPlayers} 人
                        </span>
                    </div>
                    
                    {/* 玩家进度条 - 使用科技电光蓝到极光青渐变 */}
                    <div className="h-1.5 bg-[#F1F5F9] rounded-full overflow-hidden">
                        <motion.div
                            initial={{width: 0}}
                            animate={{width: `${playerPercentage}%`}}
                            transition={{duration: 0.8, delay: index * 0.05}}
                            className="h-full bg-gradient-to-r from-[#2563EB] to-[#08D9D6] rounded-full"
                        />
                    </div>
                    
                    <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-2 text-[#64748B]">
                            <Clock className="w-4 h-4"/>
                            <span>时长</span>
                        </div>
                        <span className="font-medium text-[#0F172A]">
                            {game.duration || 120} 分钟
                        </span>
                    </div>
                </div>
                
                {/* 等待中状态 - 使用科技电光蓝到极光青渐变 */}
                {game.status === 'waiting' && (
                    <motion.div
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                        className="flex items-center justify-center gap-2 py-3 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white rounded-xl font-medium text-sm shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] group-hover:shadow-[0_6px_20px_0_rgba(37,99,235,0.5)] transition-all"
                    >
                        <Gamepad2 className="w-4 h-4"/>
                        点击加入游戏
                    </motion.div>
                )}
                
                {/* 游戏中状态 */}
                {game.status === 'playing' && (
                    <div className="flex items-center justify-center gap-2 py-3 bg-[#F1F5F9] text-[#64748B] rounded-xl font-medium text-sm">
                        <span className="w-2 h-2 rounded-full bg-[#94A3B8] animate-pulse"/>
                        游戏进行中
                    </div>
                )}
                
                {/* 已结束状态 */}
                {game.status === 'finished' && (
                    <div className="flex items-center justify-center gap-2 py-3 bg-[#F1F5F9] text-[#64748B] rounded-xl font-medium text-sm">
                        查看游戏记录
                    </div>
                )}
            </Link>
            
            {/* 右上角装饰光效 */}
            <div className="absolute top-0 right-0 w-20 h-20 overflow-hidden rounded-tr-2xl">
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-[#2563EB]/10 to-transparent transform translate-x-8 -translate-y-8 group-hover:translate-x-4 group-hover:-translate-y-4 transition-transform duration-500"/>
            </div>
        </motion.div>
    )
}

/**
 * 空状态组件 - 当没有游戏房间时显示
 * @returns {JSX.Element} 空状态展示组件
 */
function EmptyState() {
    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
            className="text-center py-16"
        >
            <motion.div
                initial={{scale: 0}}
                animate={{scale: 1}}
                transition={{duration: 0.5, delay: 0.2}}
                className="w-24 h-24 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-[#EFF6FF] to-[#DBEAFE] flex items-center justify-center"
            >
                <Gamepad2 className="w-12 h-12 text-[#2563EB]"/>
            </motion.div>
            
            <h3 className="text-2xl font-bold text-[#0F172A] mb-2">
                暂无游戏房间
            </h3>
            <p className="text-[#64748B] mb-8 max-w-md mx-auto">
                当前没有符合条件的游戏房间，成为第一个创建房间的人吧！
            </p>
            
            <motion.button
                whileHover={{scale: 1.02}}
                whileTap={{scale: 0.98}}
                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white rounded-xl font-medium shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] hover:shadow-[0_6px_20px_0_rgba(37,99,235,0.5)] transition-all"
            >
                <Plus className="w-5 h-5"/>
                创建第一个房间
            </motion.button>
        </motion.div>
    )
}

export default GameList

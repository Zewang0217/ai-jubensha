import {useState} from 'react'
import {Link} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
// eslint-disable-next-line no-unused-vars
import {motion} from 'framer-motion'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function GameList() {
    const [filter, setFilter] = useState('all')

    // 获取游戏列表
    const {data: games, isLoading, error} = useQuery({
        queryKey: ['games', filter],
        queryFn: () => gameApi.getGames({status: filter === 'all' ? undefined : filter}),
    })

    const filters = [
        {key: 'all', label: '全部', icon: '📋'},
        {key: 'waiting', label: '等待中', icon: '⏳'},
        {key: 'playing', label: '游戏中', icon: '🎮'},
        {key: 'finished', label: '已结束', icon: '✅'},
    ]

    const getStatusColor = (status) => {
        const colors = {
            waiting: 'bg-[var(--color-warning)]',
            playing: 'bg-[var(--color-success)]',
            finished: 'bg-[var(--color-secondary-400)]',
        }
        return colors[status] || 'bg-[var(--color-secondary-400)]'
    }

    const getStatusText = (status) => {
        const texts = {
            waiting: '等待中',
            playing: '游戏中',
            finished: '已结束',
        }
        return texts[status] || status
    }

    if (isLoading) {
        return <Loading text="加载游戏列表..."/>
    }

    if (error) {
        return (
            <div className="card text-center py-12">
                <div className="text-4xl mb-4">😵</div>
                <h3 className="text-lg font-semibold text-(--color-secondary-800) mb-2">
                    加载失败
                </h3>
                <p className="text-(--color-secondary-600)">
                    无法获取游戏列表，请稍后重试
                </p>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-(--color-secondary-800)">
                        游戏大厅
                    </h1>
                    <p className="text-(--color-secondary-600)">
                        选择房间加入或创建新游戏
                    </p>
                </div>
                <button
                    className="btn-accent"
                    onClick={() => {
                        // TODO: 实现创建房间逻辑
                        console.log('创建房间')
                    }}
                >
                    <span className="mr-2">+</span>
                    创建房间
                </button>
            </div>

            {/* Filters */}
            <div className="flex flex-wrap gap-2">
                {filters.map((f) => (
                    <button
                        key={f.key}
                        onClick={() => setFilter(f.key)}
                        className={`px-4 py-2 rounded-lg font-medium transition-all duration-200 ${
                            filter === f.key
                                ? 'bg-(--color-primary-100) text-(--color-primary-700)'
                                : 'bg-white text-(--color-secondary-600) hover:bg-(--color-secondary-100)'
                        }`}
                    >
                        <span className="mr-2">{f.icon}</span>
                        {f.label}
                    </button>
                ))}
            </div>

            {/* Game List */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {games?.data?.map((game, index) => (
                    <motion.div
                        key={game.id}
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.3, delay: index * 0.05}}
                    >
                        <Link
                            to={`/game/${game.id}`}
                            className="card block hover:shadow-lg transition-all duration-200 group"
                        >
                            <div className="flex items-start justify-between mb-4">
                                <div>
                                    <h3 className="font-semibold text-(--color-secondary-800) group-hover:text-(--color-primary-600) transition-colors">
                                        {game.name || `游戏房间 #${game.id}`}
                                    </h3>
                                    <p className="text-sm text-(--color-secondary-500)">
                                        {game.scriptName || '未选择剧本'}
                                    </p>
                                </div>
                                <span
                                    className={`px-2 py-1 rounded-full text-xs font-medium text-white ${getStatusColor(
                                        game.status
                                    )}`}
                                >
                  {getStatusText(game.status)}
                </span>
                            </div>

                            <div
                                className="flex items-center justify-between text-sm text-(--color-secondary-600)">
                                <div className="flex items-center space-x-4">
                  <span>
                    <span className="mr-1">👥</span>
                      {game.currentPlayers || 0}/{game.maxPlayers || 8} 人
                  </span>
                                    <span>
                    <span className="mr-1">⏱️</span>
                                        {game.duration || 120} 分钟
                  </span>
                                </div>
                            </div>

                            {game.status === 'waiting' && (
                                <div className="mt-4 pt-4 border-t border-(--color-secondary-200)">
                  <span className="text-sm text-(--color-accent-600) font-medium">
                    点击加入游戏 →
                  </span>
                                </div>
                            )}
                        </Link>
                    </motion.div>
                ))}
            </div>

            {/* Empty State */}
            {(!games?.data || games.data.length === 0) && (
                <div className="card text-center py-12">
                    <div className="text-4xl mb-4">🎭</div>
                    <h3 className="text-lg font-semibold text-(--color-secondary-800) mb-2">
                        暂无游戏房间
                    </h3>
                    <p className="text-(--color-secondary-600) mb-4">
                        当前没有符合条件的游戏房间
                    </p>
                    <button
                        className="btn-primary"
                        onClick={() => {
                            // TODO: 实现创建房间逻辑
                            console.log('创建房间')
                        }}
                    >
                        创建第一个房间
                    </button>
                </div>
            )}
        </div>
    )
}

export default GameList

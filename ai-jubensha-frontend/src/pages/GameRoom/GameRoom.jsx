import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
// eslint-disable-next-line no-unused-vars
import {motion} from 'framer-motion'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function GameRoom() {
    const {id} = useParams()

    // 获取游戏详情
    const {data: game, isLoading, error} = useQuery({
        queryKey: ['game', id],
        queryFn: () => gameApi.getGame(id),
    })

    if (isLoading) {
        return <Loading fullScreen text="加载游戏房间..."/>
    }

    if (error) {
        return (
            <div className="card text-center py-12">
                <div className="text-4xl mb-4">😵</div>
                <h3 className="text-lg font-semibold text-(--color-secondary-800) mb-2">
                    加载失败
                </h3>
                <p className="text-(--color-secondary-600)">
                    无法获取游戏信息，请稍后重试
                </p>
            </div>
        )
    }

    const gameData = game?.data

    return (
        <div className="space-y-6">
            {/* Header */}
            <motion.div
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 0}}
                className="card"
            >
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-(--color-secondary-800)">
                            {gameData?.name || `游戏房间 #${id}`}
                        </h1>
                        <p className="text-(--color-secondary-600)">
                            {gameData?.scriptName || '未选择剧本'}
                        </p>
                    </div>
                    <div className="flex items-center space-x-2">
            <span
                className="px-3 py-1 rounded-full text-sm font-medium bg-(--color-primary-100) text-(--color-primary-700)">
              {gameData?.status === 'waiting' ? '等待中' :
                  gameData?.status === 'playing' ? '游戏中' : '已结束'}
            </span>
                    </div>
                </div>
            </motion.div>

            {/* Game Content */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Left Panel - Players */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="card"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">👥</span>
                        玩家列表
                        <span className="ml-2 text-sm text-(--color-secondary-500)">
              ({gameData?.currentPlayers || 0}/{gameData?.maxPlayers || 8})
            </span>
                    </h2>

                    <div className="space-y-3">
                        {gameData?.players?.map((player) => (
                            <div
                                key={player.id}
                                className="flex items-center space-x-3 p-3 rounded-lg bg-(--color-secondary-50)"
                            >
                                <div
                                    className="w-10 h-10 rounded-full bg-(--color-primary-100) flex items-center justify-center text-(--color-primary-600) font-semibold">
                                    {player.name?.[0] || '?'}
                                </div>
                                <div className="flex-1">
                                    <p className="font-medium text-(--color-secondary-800)">
                                        {player.name}
                                    </p>
                                    <p className="text-sm text-(--color-secondary-500)">
                                        {player.characterName || '未选择角色'}
                                    </p>
                                </div>
                                {player.isHost && (
                                    <span
                                        className="px-2 py-1 rounded text-xs font-medium bg-(--color-accent-100) text-(--color-accent-700)">
                    房主
                  </span>
                                )}
                            </div>
                        )) || (
                            <p className="text-center text-(--color-secondary-500) py-4">
                                暂无玩家
                            </p>
                        )}
                    </div>

                    {gameData?.status === 'waiting' && (
                        <button className="btn-primary w-full mt-4">
                            加入游戏
                        </button>
                    )}
                </motion.div>

                {/* Center Panel - Game Area */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.2}}
                    className="lg:col-span-2 space-y-6"
                >
                    {/* Scene Info */}
                    <div className="card">
                        <h2 className="text-lg font-semibold mb-4 flex items-center">
                            <span className="mr-2">🎭</span>
                            当前场景
                        </h2>
                        <div className="bg-(--color-secondary-50) rounded-lg p-4">
                            <p className="text-(--color-secondary-600)">
                                游戏尚未开始，等待玩家加入...
                            </p>
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="card">
                        <h2 className="text-lg font-semibold mb-4 flex items-center">
                            <span className="mr-2">🎮</span>
                            游戏操作
                        </h2>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <button
                                className="p-4 rounded-lg bg-(--color-primary-50) hover:bg-(--color-primary-100) transition-colors text-center"
                                onClick={() => console.log('查看剧本')}
                            >
                                <div className="text-2xl mb-2">📖</div>
                                <div className="text-sm font-medium text-(--color-secondary-700)">
                                    查看剧本
                                </div>
                            </button>
                            <button
                                className="p-4 rounded-lg bg-(--color-accent-50) hover:bg-(--color-accent-100) transition-colors text-center"
                                onClick={() => console.log('搜证')}
                            >
                                <div className="text-2xl mb-2">🔍</div>
                                <div className="text-sm font-medium text-(--color-secondary-700)">
                                    搜证
                                </div>
                            </button>
                            <button
                                className="p-4 rounded-lg bg-(--color-success)/10 hover:bg-(--color-success)/20 transition-colors text-center"
                                onClick={() => console.log('角色')}
                            >
                                <div className="text-2xl mb-2">👤</div>
                                <div className="text-sm font-medium text-(--color-secondary-700)">
                                    角色信息
                                </div>
                            </button>
                            <button
                                className="p-4 rounded-lg bg-(--color-secondary-100) hover:bg-(--color-secondary-200) transition-colors text-center"
                                onClick={() => console.log('线索')}
                            >
                                <div className="text-2xl mb-2">📝</div>
                                <div className="text-sm font-medium text-(--color-secondary-700)">
                                    线索记录
                                </div>
                            </button>
                        </div>
                    </div>

                    {/* Chat / Log */}
                    <div className="card">
                        <h2 className="text-lg font-semibold mb-4 flex items-center">
                            <span className="mr-2">💬</span>
                            游戏记录
                        </h2>
                        <div className="h-64 bg-(--color-secondary-50) rounded-lg p-4 overflow-y-auto">
                            <div className="text-center text-(--color-secondary-500) py-8">
                                游戏记录将显示在这里
                            </div>
                        </div>
                    </div>
                </motion.div>
            </div>
        </div>
    )
}

export default GameRoom

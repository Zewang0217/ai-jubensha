import {useState} from 'react'
import {Link} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function GameList() {
    const [filter, setFilter] = useState('all')

    const {data: games, isLoading, error} = useQuery({
        queryKey: ['games', filter],
        queryFn: () => gameApi.getGames({status: filter === 'all' ? undefined : filter}),
    })

    const filters = [
        {key: 'all', label: '全部'},
        {key: 'waiting', label: '等待中'},
        {key: 'playing', label: '游戏中'},
        {key: 'finished', label: '已结束'},
    ]

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
            <div>
                <h3>加载失败</h3>
                <p>无法获取游戏列表，请稍后重试</p>
            </div>
        )
    }

    return (
        <div>
            <div>
                <div>
                    <h1>游戏大厅</h1>
                    <p>选择房间加入或创建新游戏</p>
                </div>
                <button onClick={() => console.log('创建房间')}>
                    创建房间
                </button>
            </div>

            <div>
                {filters.map((f) => (
                    <button key={f.key} onClick={() => setFilter(f.key)}>
                        {f.label}
                    </button>
                ))}
            </div>

            <div>
                {games?.data?.map((game) => (
                    <Link key={game.id} to={`/game/${game.id}`}>
                        <div>
                            <h3>{game.name || `游戏房间 #${game.id}`}</h3>
                            <p>{game.scriptName || '未选择剧本'}</p>
                        </div>
                        <span>{getStatusText(game.status)}</span>
                        <div>
                            <span>{game.currentPlayers || 0}/{game.maxPlayers || 8} 人</span>
                            <span>{game.duration || 120} 分钟</span>
                        </div>
                        {game.status === 'waiting' && <span>点击加入游戏</span>}
                    </Link>
                ))}
            </div>

            {(!games?.data || games.data.length === 0) && (
                <div>
                    <h3>暂无游戏房间</h3>
                    <p>当前没有符合条件的游戏房间</p>
                    <button onClick={() => console.log('创建房间')}>
                        创建第一个房间
                    </button>
                </div>
            )}
        </div>
    )
}

export default GameList

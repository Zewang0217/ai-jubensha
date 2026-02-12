import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {gameApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function GameRoom() {
    const {id} = useParams()

    const {data: game, isLoading, error} = useQuery({
        queryKey: ['game', id],
        queryFn: () => gameApi.getGame(id),
    })

    if (isLoading) {
        return <Loading fullScreen text="加载游戏房间..."/>
    }

    if (error) {
        return (
            <div>
                <h3>加载失败</h3>
                <p>无法获取游戏信息，请稍后重试</p>
            </div>
        )
    }

    const gameData = game?.data

    return (
        <div>
            <div>
                <div>
                    <h1>{gameData?.name || `游戏房间 #${id}`}</h1>
                    <p>{gameData?.scriptName || '未选择剧本'}</p>
                </div>
                <span>
                    {gameData?.status === 'waiting' ? '等待中' :
                        gameData?.status === 'playing' ? '游戏中' : '已结束'}
                </span>
            </div>

            <div>
                <div>
                    <h2>玩家列表 ({gameData?.currentPlayers || 0}/{gameData?.maxPlayers || 8})</h2>
                    <div>
                        {gameData?.players?.map((player) => (
                            <div key={player.id}>
                                <span>{player.name?.[0] || '?'}</span>
                                <div>
                                    <p>{player.name}</p>
                                    <p>{player.characterName || '未选择角色'}</p>
                                </div>
                                {player.isHost && <span>房主</span>}
                            </div>
                        )) || <p>暂无玩家</p>}
                    </div>
                    {gameData?.status === 'waiting' && <button>加入游戏</button>}
                </div>

                <div>
                    <div>
                        <h2>当前场景</h2>
                        <p>游戏尚未开始，等待玩家加入...</p>
                    </div>

                    <div>
                        <h2>游戏操作</h2>
                        <button onClick={() => console.log('查看剧本')}>查看剧本</button>
                        <button onClick={() => console.log('搜证')}>搜证</button>
                        <button onClick={() => console.log('角色')}>角色信息</button>
                        <button onClick={() => console.log('线索')}>线索记录</button>
                    </div>

                    <div>
                        <h2>游戏记录</h2>
                        <div>
                            <p>游戏记录将显示在这里</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default GameRoom

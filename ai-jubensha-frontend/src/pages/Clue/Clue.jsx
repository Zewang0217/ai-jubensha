import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {clueApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Clue() {
    const {id} = useParams()

    const {data: clue, isLoading, error} = useQuery({
        queryKey: ['clue', id],
        queryFn: () => clueApi.getClue(id),
    })

    if (isLoading) {
        return <Loading text="加载线索信息..."/>
    }

    if (error) {
        return (
            <div>
                <h3>加载失败</h3>
                <p>无法获取线索信息，请稍后重试</p>
            </div>
        )
    }

    const clueData = clue?.data

    const getClueTypeText = (type) => {
        const texts = {
            physical: '物证',
            testimony: '证词',
            document: '文件',
            other: '其他',
        }
        return texts[type] || '其他'
    }

    return (
        <div>
            <div>
                <span>{getClueTypeText(clueData?.type)}</span>
                {clueData?.isKey && <span>关键线索</span>}
                <h1>{clueData?.name || '线索详情'}</h1>
            </div>

            <div>
                <h2>线索描述</h2>
                <p>{clueData?.description || '暂无描述'}</p>
            </div>

            <div>
                <div>
                    <h2>线索来源</h2>
                    <div>
                        <span>发现地点</span>
                        <span>{clueData?.sceneName || '未知'}</span>
                    </div>
                    <div>
                        <span>搜证区域</span>
                        <span>{clueData?.areaName || '未知'}</span>
                    </div>
                    <div>
                        <span>发现时间</span>
                        <span>
                            {clueData?.discoveredAt
                                ? new Date(clueData.discoveredAt).toLocaleString('zh-CN')
                                : '未发现'}
                        </span>
                    </div>
                </div>

                <div>
                    <h2>发现者</h2>
                    {clueData?.discoverer ? (
                        <div>
                            <span>{clueData.discoverer.name?.[0] || '?'}</span>
                            <div>
                                <p>{clueData.discoverer.name}</p>
                                <p>{clueData.discoverer.characterName}</p>
                            </div>
                        </div>
                    ) : (
                        <p>该线索尚未被发现</p>
                    )}
                </div>
            </div>

            <div>
                <h2>关联线索</h2>
                <div>
                    {clueData?.relatedClues?.map((relatedClue) => (
                        <div key={relatedClue.id}>
                            <div>
                                <h3>{relatedClue.name}</h3>
                                <p>{relatedClue.description?.substring(0, 50)}...</p>
                            </div>
                            <span>{getClueTypeText(relatedClue.type)}</span>
                        </div>
                    )) || <p>暂无关联线索</p>}
                </div>
            </div>
        </div>
    )
}

export default Clue

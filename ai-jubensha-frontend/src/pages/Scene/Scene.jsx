import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {sceneApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Scene() {
    const {id} = useParams()

    const {data: scene, isLoading, error} = useQuery({
        queryKey: ['scene', id],
        queryFn: () => sceneApi.getScene(id),
    })

    if (isLoading) {
        return <Loading text="加载场景信息..."/>
    }

    if (error) {
        return (
            <div>
                <h3>加载失败</h3>
                <p>无法获取场景信息，请稍后重试</p>
            </div>
        )
    }

    const sceneData = scene?.data

    return (
        <div>
            <div>
                <h1>{sceneData?.name || '场景详情'}</h1>
                <p>{sceneData?.description || '查看场景信息和可搜证区域'}</p>
            </div>

            <div>
                <h2>场景描述</h2>
                <p>{sceneData?.content || '暂无场景描述'}</p>

                <h2>可搜证区域</h2>
                <div>
                    {sceneData?.searchAreas?.map((area) => (
                        <div key={area.id}>
                            <div>
                                <h3>{area.name}</h3>
                                <p>{area.description}</p>
                            </div>
                            <button>搜证</button>
                        </div>
                    )) || <p>暂无可搜证区域</p>}
                </div>
            </div>

            <div>
                <h2>相关线索</h2>
                <p>通过搜证可以发现更多线索</p>
            </div>
        </div>
    )
}

export default Scene

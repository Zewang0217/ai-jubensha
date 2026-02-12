import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {characterApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Character() {
    const {id} = useParams()

    const {data: character, isLoading, error} = useQuery({
        queryKey: ['character', id],
        queryFn: () => characterApi.getCharacter(id),
    })

    if (isLoading) {
        return <Loading text="加载角色信息..."/>
    }

    if (error) {
        return (
            <div>
                <h3>加载失败</h3>
                <p>无法获取角色信息，请稍后重试</p>
            </div>
        )
    }

    const characterData = character?.data

    return (
        <div>
            <div>
                <span>{characterData?.name?.[0] || '?'}</span>
                <div>
                    <h1>{characterData?.name || '角色详情'}</h1>
                    <p>{characterData?.title || '无称号'}</p>
                    <div>
                        {characterData?.tags?.map((tag) => (
                            <span key={tag}>{tag}</span>
                        ))}
                    </div>
                </div>
            </div>

            <div>
                <div>
                    <h2>角色背景</h2>
                    <p>{characterData?.background || '暂无背景信息'}</p>
                </div>

                <div>
                    <h2>性格特点</h2>
                    {characterData?.personality ? (
                        <ul>
                            {characterData.personality.map((trait, index) => (
                                <li key={index}>{trait}</li>
                            ))}
                        </ul>
                    ) : (
                        <p>暂无性格描述</p>
                    )}
                </div>

                <div>
                    <h2>秘密任务</h2>
                    <p>{characterData?.secret || '暂无秘密任务'}</p>
                </div>

                <div>
                    <h2>人物关系</h2>
                    <div>
                        {characterData?.relationships?.map((rel) => (
                            <div key={rel.characterId}>
                                <span>{rel.characterName?.[0] || '?'}</span>
                                <div>
                                    <p>{rel.characterName}</p>
                                    <p>{rel.description}</p>
                                </div>
                            </div>
                        )) || <p>暂无人物关系信息</p>}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Character

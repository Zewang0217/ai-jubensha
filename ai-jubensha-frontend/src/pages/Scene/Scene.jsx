import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {motion} from 'framer-motion'
import {sceneApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Scene() {
    const {id} = useParams()

    // 获取场景详情
    const {data: scene, isLoading, error} = useQuery({
        queryKey: ['scene', id],
        queryFn: () => sceneApi.getScene(id),
    })

    if (isLoading) {
        return <Loading text="加载场景信息..."/>
    }

    if (error) {
        return (
            <div className="card text-center py-12">
                <div className="text-4xl mb-4">😵</div>
                <h3 className="text-lg font-semibold text-[var(--color-secondary-800)] mb-2">
                    加载失败
                </h3>
                <p className="text-[var(--color-secondary-600)]">
                    无法获取场景信息，请稍后重试
                </p>
            </div>
        )
    }

    const sceneData = scene?.data

    return (
        <div className="space-y-6">
            {/* Header */}
            <motion.div
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 0}}
            >
                <h1 className="text-2xl font-bold text-[var(--color-secondary-800)]">
                    {sceneData?.name || '场景详情'}
                </h1>
                <p className="text-[var(--color-secondary-600)]">
                    {sceneData?.description || '查看场景信息和可搜证区域'}
                </p>
            </motion.div>

            {/* Scene Content */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.1}}
                className="card"
            >
                <div className="prose max-w-none">
                    <h2 className="text-lg font-semibold mb-4">场景描述</h2>
                    <div className="bg-[var(--color-secondary-50)] rounded-lg p-6 mb-6">
                        <p className="text-[var(--color-secondary-700)] leading-relaxed">
                            {sceneData?.content || '暂无场景描述'}
                        </p>
                    </div>

                    <h2 className="text-lg font-semibold mb-4">可搜证区域</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {sceneData?.searchAreas?.map((area) => (
                            <div
                                key={area.id}
                                className="p-4 rounded-lg border border-[var(--color-secondary-200)] hover:border-[var(--color-primary-400)] transition-colors cursor-pointer"
                            >
                                <div className="flex items-center justify-between">
                                    <div>
                                        <h3 className="font-medium text-[var(--color-secondary-800)]">
                                            {area.name}
                                        </h3>
                                        <p className="text-sm text-[var(--color-secondary-500)]">
                                            {area.description}
                                        </p>
                                    </div>
                                    <button className="btn-primary text-sm">
                                        搜证
                                    </button>
                                </div>
                            </div>
                        )) || (
                            <div className="col-span-2 text-center py-8 text-[var(--color-secondary-500)]">
                                暂无可搜证区域
                            </div>
                        )}
                    </div>
                </div>
            </motion.div>

            {/* Related Clues */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.2}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4">相关线索</h2>
                <div className="text-center py-8 text-[var(--color-secondary-500)]">
                    通过搜证可以发现更多线索
                </div>
            </motion.div>
        </div>
    )
}

export default Scene

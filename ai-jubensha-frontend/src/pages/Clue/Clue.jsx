import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
import {motion} from 'framer-motion'
import {clueApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Clue() {
    const {id} = useParams()

    // 获取线索详情
    const {data: clue, isLoading, error} = useQuery({
        queryKey: ['clue', id],
        queryFn: () => clueApi.getClue(id),
    })

    if (isLoading) {
        return <Loading text="加载线索信息..."/>
    }

    if (error) {
        return (
            <div className="card text-center py-12">
                <div className="text-4xl mb-4">😵</div>
                <h3 className="text-lg font-semibold text-[var(--color-secondary-800)] mb-2">
                    加载失败
                </h3>
                <p className="text-[var(--color-secondary-600)]">
                    无法获取线索信息，请稍后重试
                </p>
            </div>
        )
    }

    const clueData = clue?.data

    const getClueTypeColor = (type) => {
        const colors = {
            physical: 'bg-[var(--color-primary-100)] text-[var(--color-primary-700)]',
            testimony: 'bg-[var(--color-accent-100)] text-[var(--color-accent-700)]',
            document: 'bg-[var(--color-success)]/20 text-[var(--color-success)]',
            other: 'bg-[var(--color-secondary-200)] text-[var(--color-secondary-700)]',
        }
        return colors[type] || colors.other
    }

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
        <div className="space-y-6">
            {/* Header */}
            <motion.div
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 0}}
            >
                <div className="flex items-center space-x-3 mb-2">
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${getClueTypeColor(clueData?.type)}`}>
            {getClueTypeText(clueData?.type)}
          </span>
                    {clueData?.isKey && (
                        <span
                            className="px-3 py-1 rounded-full text-sm font-medium bg-[var(--color-error)]/20 text-[var(--color-error)]">
              关键线索
            </span>
                    )}
                </div>
                <h1 className="text-2xl font-bold text-[var(--color-secondary-800)]">
                    {clueData?.name || '线索详情'}
                </h1>
            </motion.div>

            {/* Clue Content */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.1}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4">线索描述</h2>
                <div className="bg-[var(--color-secondary-50)] rounded-lg p-6">
                    <p className="text-[var(--color-secondary-700)] leading-relaxed whitespace-pre-wrap">
                        {clueData?.description || '暂无描述'}
                    </p>
                </div>
            </motion.div>

            {/* Clue Details */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Source */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.2}}
                    className="card"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">📍</span>
                        线索来源
                    </h2>
                    <div className="space-y-3">
                        <div
                            className="flex justify-between items-center py-2 border-b border-[var(--color-secondary-200)]">
                            <span className="text-[var(--color-secondary-600)]">发现地点</span>
                            <span className="font-medium text-[var(--color-secondary-800)]">
                {clueData?.sceneName || '未知'}
              </span>
                        </div>
                        <div
                            className="flex justify-between items-center py-2 border-b border-[var(--color-secondary-200)]">
                            <span className="text-[var(--color-secondary-600)]">搜证区域</span>
                            <span className="font-medium text-[var(--color-secondary-800)]">
                {clueData?.areaName || '未知'}
              </span>
                        </div>
                        <div className="flex justify-between items-center py-2">
                            <span className="text-[var(--color-secondary-600)]">发现时间</span>
                            <span className="font-medium text-[var(--color-secondary-800)]">
                {clueData?.discoveredAt
                    ? new Date(clueData.discoveredAt).toLocaleString('zh-CN')
                    : '未发现'}
              </span>
                        </div>
                    </div>
                </motion.div>

                {/* Discoverer */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.3}}
                    className="card"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">🔍</span>
                        发现者
                    </h2>
                    {clueData?.discoverer ? (
                        <div className="flex items-center space-x-4">
                            <div
                                className="w-12 h-12 rounded-full bg-[var(--color-primary-100)] flex items-center justify-center text-[var(--color-primary-600)] text-xl font-semibold">
                                {clueData.discoverer.name?.[0] || '?'}
                            </div>
                            <div>
                                <p className="font-medium text-[var(--color-secondary-800)]">
                                    {clueData.discoverer.name}
                                </p>
                                <p className="text-sm text-[var(--color-secondary-500)]">
                                    {clueData.discoverer.characterName}
                                </p>
                            </div>
                        </div>
                    ) : (
                        <p className="text-[var(--color-secondary-500)] py-4">
                            该线索尚未被发现
                        </p>
                    )}
                </motion.div>
            </div>

            {/* Related Clues */}
            <motion.div
                initial={{opacity: 0, y: 20}}
                animate={{opacity: 1, y: 0}}
                transition={{delay: 0.4}}
                className="card"
            >
                <h2 className="text-lg font-semibold mb-4 flex items-center">
                    <span className="mr-2">🔗</span>
                    关联线索
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {clueData?.relatedClues?.map((relatedClue) => (
                        <div
                            key={relatedClue.id}
                            className="p-4 rounded-lg border border-[var(--color-secondary-200)] hover:border-[var(--color-primary-400)] transition-colors cursor-pointer"
                        >
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="font-medium text-[var(--color-secondary-800)]">
                                        {relatedClue.name}
                                    </h3>
                                    <p className="text-sm text-[var(--color-secondary-500)]">
                                        {relatedClue.description?.substring(0, 50)}...
                                    </p>
                                </div>
                                <span
                                    className={`px-2 py-1 rounded text-xs font-medium ${getClueTypeColor(relatedClue.type)}`}>
                  {getClueTypeText(relatedClue.type)}
                </span>
                            </div>
                        </div>
                    )) || (
                        <div className="col-span-2 text-center py-4 text-[var(--color-secondary-500)]">
                            暂无关联线索
                        </div>
                    )}
                </div>
            </motion.div>
        </div>
    )
}

export default Clue

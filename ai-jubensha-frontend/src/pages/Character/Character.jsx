import {useParams} from 'react-router-dom'
import {useQuery} from '@tanstack/react-query'
// eslint-disable-next-line no-unused-vars
import {motion} from 'framer-motion'
import {characterApi} from '../../services/api'
import Loading from '../../components/common/Loading'

function Character() {
    const {id} = useParams()

    // 获取角色详情
    const {data: character, isLoading, error} = useQuery({
        queryKey: ['character', id],
        queryFn: () => characterApi.getCharacter(id),
    })

    if (isLoading) {
        return <Loading text="加载角色信息..."/>
    }

    if (error) {
        return (
            <div className="card text-center py-12">
                <div className="text-4xl mb-4">😵</div>
                <h3 className="text-lg font-semibold text-[var(--color-secondary-800)] mb-2">
                    加载失败
                </h3>
                <p className="text-[var(--color-secondary-600)]">
                    无法获取角色信息，请稍后重试
                </p>
            </div>
        )
    }

    const characterData = character?.data

    return (
        <div className="space-y-6">
            {/* Header */}
            <motion.div
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 0}}
                className="card"
            >
                <div className="flex flex-col md:flex-row md:items-center gap-6">
                    {/* Avatar */}
                    <div
                        className="w-24 h-24 rounded-full bg-gradient-to-br from-[var(--color-primary-400)] to-[var(--color-accent-400)] flex items-center justify-center text-white text-3xl font-bold">
                        {characterData?.name?.[0] || '?'}
                    </div>

                    {/* Info */}
                    <div className="flex-1">
                        <h1 className="text-2xl font-bold text-[var(--color-secondary-800)]">
                            {characterData?.name || '角色详情'}
                        </h1>
                        <p className="text-[var(--color-secondary-600)] mt-1">
                            {characterData?.title || '无称号'}
                        </p>
                        <div className="flex flex-wrap gap-2 mt-3">
                            {characterData?.tags?.map((tag) => (
                                <span
                                    key={tag}
                                    className="px-3 py-1 rounded-full text-sm bg-[var(--color-primary-100)] text-[var(--color-primary-700)]"
                                >
                  {tag}
                </span>
                            ))}
                        </div>
                    </div>
                </div>
            </motion.div>

            {/* Character Details */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Background */}
                <motion.div
                    initial={{opacity: 0, x: -20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.1}}
                    className="card"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">📖</span>
                        角色背景
                    </h2>
                    <div className="prose prose-sm max-w-none">
                        <p className="text-[var(--color-secondary-700)] leading-relaxed">
                            {characterData?.background || '暂无背景信息'}
                        </p>
                    </div>
                </motion.div>

                {/* Personality */}
                <motion.div
                    initial={{opacity: 0, x: 20}}
                    animate={{opacity: 1, x: 0}}
                    transition={{delay: 0.2}}
                    className="card"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">🎭</span>
                        性格特点
                    </h2>
                    <div className="space-y-3">
                        {characterData?.personality ? (
                            <ul className="space-y-2">
                                {characterData.personality.map((trait, index) => (
                                    <li
                                        key={index}
                                        className="flex items-start space-x-2 text-[var(--color-secondary-700)]"
                                    >
                                        <span className="text-[var(--color-primary-500)] mt-1">•</span>
                                        <span>{trait}</span>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-[var(--color-secondary-500)]">暂无性格描述</p>
                        )}
                    </div>
                </motion.div>

                {/* Secrets */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.3}}
                    className="card lg:col-span-2"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">🔐</span>
                        秘密任务
                    </h2>
                    <div className="bg-[var(--color-accent-50)] rounded-lg p-4 border border-[var(--color-accent-200)]">
                        <p className="text-[var(--color-accent-800)] leading-relaxed">
                            {characterData?.secret || '暂无秘密任务'}
                        </p>
                    </div>
                </motion.div>

                {/* Relationships */}
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{delay: 0.4}}
                    className="card lg:col-span-2"
                >
                    <h2 className="text-lg font-semibold mb-4 flex items-center">
                        <span className="mr-2">👥</span>
                        人物关系
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {characterData?.relationships?.map((rel) => (
                            <div
                                key={rel.characterId}
                                className="p-4 rounded-lg bg-[var(--color-secondary-50)]"
                            >
                                <div className="flex items-center space-x-3">
                                    <div
                                        className="w-10 h-10 rounded-full bg-[var(--color-primary-100)] flex items-center justify-center text-[var(--color-primary-600)] font-semibold">
                                        {rel.characterName?.[0] || '?'}
                                    </div>
                                    <div>
                                        <p className="font-medium text-[var(--color-secondary-800)]">
                                            {rel.characterName}
                                        </p>
                                        <p className="text-sm text-[var(--color-secondary-500)]">
                                            {rel.description}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )) || (
                            <div className="col-span-2 text-center py-4 text-[var(--color-secondary-500)]">
                                暂无人物关系信息
                            </div>
                        )}
                    </div>
                </motion.div>
            </div>
        </div>
    )
}

export default Character

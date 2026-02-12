import {Link} from 'react-router-dom'
import {motion} from 'framer-motion'

function Home() {
    const features = [
        {
            icon: '🎮',
            title: 'AI 主持',
            description: '智能 AI 主持人引导游戏流程，让游戏体验更流畅'
        },
        {
            icon: '🤖',
            title: 'AI 玩家',
            description: '与智能 AI 玩家一起游戏，随时开始精彩对局'
        },
        {
            icon: '📝',
            title: '剧本生成',
            description: 'AI 辅助生成剧本，创造独特的游戏体验'
        },
        {
            icon: '🔍',
            title: '智能搜证',
            description: '沉浸式搜证系统，探索案件真相'
        }
    ]

    return (
        <div className="space-y-12">
            {/* Hero Section */}
            <section className="text-center py-12 md:py-20">
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.5}}
                >
                    <h1 className="text-4xl md:text-6xl font-bold mb-6">
                        <span className="text-gradient">AI 剧本杀</span>
                    </h1>
                    <p className="text-xl text-[var(--color-secondary-600)] mb-8 max-w-2xl mx-auto">
                        与 AI 一起体验沉浸式剧本杀游戏，智能主持、自动推理，
                        让每一局游戏都充满惊喜
                    </p>
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                        <Link
                            to="/games"
                            className="btn-primary text-lg px-8 py-3"
                        >
                            <span className="mr-2">🎮</span>
                            开始游戏
                        </Link>
                        <Link
                            to="/settings"
                            className="btn-secondary text-lg px-8 py-3"
                        >
                            <span className="mr-2">⚙️</span>
                            游戏设置
                        </Link>
                    </div>
                </motion.div>
            </section>

            {/* Features Section */}
            <section>
                <h2 className="text-2xl md:text-3xl font-bold text-center mb-8">
                    游戏特色
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {features.map((feature, index) => (
                        <motion.div
                            key={feature.title}
                            initial={{opacity: 0, y: 20}}
                            animate={{opacity: 1, y: 0}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            className="card text-center hover:shadow-md transition-shadow"
                        >
                            <div className="text-4xl mb-4">{feature.icon}</div>
                            <h3 className="text-lg font-semibold text-[var(--color-secondary-800)] mb-2">
                                {feature.title}
                            </h3>
                            <p className="text-[var(--color-secondary-600)] text-sm">
                                {feature.description}
                            </p>
                        </motion.div>
                    ))}
                </div>
            </section>

            {/* Quick Start Section */}
            <section className="card bg-gradient-to-br from-[var(--color-primary-50)] to-[var(--color-accent-50)]">
                <div className="text-center">
                    <h2 className="text-2xl font-bold mb-4">快速开始</h2>
                    <p className="text-[var(--color-secondary-600)] mb-6">
                        选择一个游戏房间加入，或创建自己的房间邀请好友
                    </p>
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                        <Link
                            to="/games"
                            className="btn-primary"
                        >
                            浏览游戏房间
                        </Link>
                        <button
                            className="btn-accent"
                            onClick={() => {
                                // TODO: 实现创建房间逻辑
                                console.log('创建房间')
                            }}
                        >
                            创建新房间
                        </button>
                    </div>
                </div>
            </section>
        </div>
    )
}

export default Home

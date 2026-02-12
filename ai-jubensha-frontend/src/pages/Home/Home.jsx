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
        <div className="relative min-h-screen flex flex-col justify-center items-center text-center pt-20 px-4">
            {/* Hero Section - Central Visual Area */}
            <section className="py-12 md:py-20 max-w-4xl mx-auto">
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, ease: "easeOut"}}
                >
                    <h1 className="text-5xl md:text-7xl font-bold mb-6 text-white tracking-wide leading-tight">
                        <span className="text-dark-red-500">剧本杀</span>
                        AI 智能推理
                    </h1>
                    <p className="text-xl text-secondary-300 mb-10 max-w-3xl mx-auto opacity-80">
                        深入迷雾，解锁真相。与AI共织悬疑，每一场都是智力与勇气的较量。
                    </p>
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-6">
                        <Link
                            to="/games"
                            className="btn bg-dark-red-500 text-white text-lg px-10 py-4 rounded-xl shadow-lg hover:bg-dark-red-600 transition-all duration-300 transform hover:-translate-y-1"
                        >
                            <span className="mr-3 text-2xl">🔍</span>
                            开始推理
                        </Link>
                        <Link
                            to="/settings"
                            className="btn border-2 border-dark-gold-500 text-dark-gold-500 text-lg px-10 py-4 rounded-xl shadow-lg hover:bg-dark-gold-500 hover:text-white transition-all duration-300 transform hover:-translate-y-1"
                        >
                            <span className="mr-3 text-2xl">⚙️</span>
                            剧本工坊
                        </Link>
                    </div>
                </motion.div>
            </section>

            {/* Features Section */}
            <section className="mt-20 w-full max-w-6xl">
                <h2 className="text-3xl font-bold text-center mb-12 text-white opacity-90">
                    核心特色
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {features.map((feature, index) => (
                        <motion.div
                            key={feature.title}
                            initial={{opacity: 0, y: 50}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true, amount: 0.3}}
                            transition={{duration: 0.6, delay: index * 0.15, ease: "easeOut"}}
                            className="frosted-glass-effect card text-center p-6 cursor-pointer transform hover:scale-105 transition-all duration-300"
                        >
                            <div className="text-5xl mb-4 opacity-80">{feature.icon}</div>
                            <h3 className="text-xl font-semibold text-white mb-2 opacity-95">
                                {feature.title}
                            </h3>
                            <p className="text-secondary-300 text-base opacity-70">
                                {feature.description}
                            </p>
                        </motion.div>
                    ))}
                </div>
            </section>

            {/* Quick Start Section - Renamed to Call to Action */}
            <section className="mt-20 mb-20 w-full max-w-4xl">
                <motion.div
                    initial={{opacity: 0, scale: 0.9}}
                    whileInView={{opacity: 1, scale: 1}}
                    viewport={{once: true, amount: 0.3}}
                    transition={{duration: 0.7, delay: 0.5, ease: "easeOut"}}
                    className="frosted-glass-effect p-10 text-center"
                >
                    <h2 className="text-3xl font-bold text-white mb-4">
                        立即开启你的推理之旅
                    </h2>
                    <p className="text-secondary-300 mb-8 opacity-80">
                        体验前所未有的沉浸式剧本杀乐趣，与AI一同揭开重重迷雾。
                    </p>
                    <Link
                        to="/games"
                        className="btn bg-dark-red-500 text-white text-xl px-12 py-4 rounded-xl shadow-lg hover:bg-dark-red-600 transition-all duration-300 transform hover:-translate-y-1"
                    >
                        <span className="mr-3">🚀</span>
                        探索游戏房间
                    </Link>
                </motion.div>
            </section>
        </div>
    )
}

export default Home

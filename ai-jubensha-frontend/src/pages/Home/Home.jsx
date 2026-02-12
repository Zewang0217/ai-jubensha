import {Link} from 'react-router-dom'
import {motion} from 'framer-motion'
import FeatureCard from '../../components/ui/FeatureCard'

function Home() {
    const features = [
        {
            icon: (
                <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                          d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                </svg>
            ),
            title: 'AI 主持',
            description: '智能 AI 主持人引导游戏流程，自动推进剧情发展，让每一场游戏体验都流畅自然，无需等待真人主持。'
        },
        {
            icon: (
                <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
            ),
            title: 'AI 玩家',
            description: '与智能 AI 玩家一起游戏，它们拥有独特的性格和推理能力，随时为你匹配最佳队友或对手。'
        },
        {
            icon: (
                <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                          d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                </svg>
            ),
            title: '剧本生成',
            description: 'AI 辅助生成原创剧本，从悬疑推理到情感沉浸，多种题材风格任你选择，每次都有新体验。'
        },
        {
            icon: (
                <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7"/>
                </svg>
            ),
            title: '智能搜证',
            description: '沉浸式搜证系统，自由探索场景中的每个角落，发现隐藏线索，通过推理还原案件真相。'
        }
    ]

    return (
        <div className="min-h-screen bg-gradient-to-b from-slate-50 via-white to-blue-50/30">
            {/* Hero Section */}
            <section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-20">
                {/* Background Decorations */}
                <div className="absolute inset-0 overflow-hidden pointer-events-none">
                    {/* Gradient Orbs */}
                    <motion.div
                        initial={{opacity: 0, scale: 0.8}}
                        animate={{opacity: 1, scale: 1}}
                        transition={{duration: 1.5}}
                        className="absolute top-20 -left-20 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl"
                    />
                    <motion.div
                        initial={{opacity: 0, scale: 0.8}}
                        animate={{opacity: 1, scale: 1}}
                        transition={{duration: 1.5, delay: 0.2}}
                        className="absolute bottom-20 -right-20 w-96 h-96 bg-blue-600/20 rounded-full blur-3xl"
                    />
                    <motion.div
                        initial={{opacity: 0, scale: 0.8}}
                        animate={{opacity: 1, scale: 1}}
                        transition={{duration: 1.5, delay: 0.4}}
                        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-blue-300/10 to-blue-500/10 rounded-full blur-3xl"
                    />

                    {/* Grid Pattern */}
                    <div
                        className="absolute inset-0 bg-[linear-gradient(rgba(59,130,246,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(59,130,246,0.03)_1px,transparent_1px)] bg-[size:60px_60px]"/>

                    {/* Floating Elements */}
                    <motion.div
                        animate={{
                            y: [0, -20, 0],
                            rotate: [0, 5, 0]
                        }}
                        transition={{
                            duration: 6,
                            repeat: Infinity,
                            ease: "easeInOut"
                        }}
                        className="absolute top-32 right-[15%] w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-400/30 to-blue-600/30 backdrop-blur-sm border border-blue-300/30"
                    />
                    <motion.div
                        animate={{
                            y: [0, 15, 0],
                            rotate: [0, -5, 0]
                        }}
                        transition={{
                            duration: 5,
                            repeat: Infinity,
                            ease: "easeInOut",
                            delay: 1
                        }}
                        className="absolute bottom-40 left-[10%] w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500/30 to-blue-700/30 backdrop-blur-sm border border-blue-300/30"
                    />
                    <motion.div
                        animate={{
                            y: [0, -10, 0],
                            x: [0, 10, 0]
                        }}
                        transition={{
                            duration: 4,
                            repeat: Infinity,
                            ease: "easeInOut",
                            delay: 0.5
                        }}
                        className="absolute top-1/3 left-[20%] w-8 h-8 rounded-lg bg-gradient-to-br from-blue-300/40 to-blue-500/40 backdrop-blur-sm"
                    />
                </div>

                {/* Hero Content */}
                <div className="relative z-10 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                    {/* Badge */}
                    <motion.div
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.6}}
                        className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-100/80 backdrop-blur-sm border border-blue-200 mb-8"
                    >
                        <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse"/>
                        <span className="text-blue-700 text-sm font-medium">AI 驱动的沉浸式推理体验</span>
                    </motion.div>

                    {/* Main Title */}
                    <motion.h1
                        initial={{opacity: 0, y: 30}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.8, delay: 0.1}}
                        className="text-5xl sm:text-6xl md:text-7xl lg:text-8xl font-bold tracking-tight mb-6"
                    >
                        <span
                            className="bg-gradient-to-r from-blue-600 via-blue-500 to-blue-700 bg-clip-text text-transparent">
                            剧本杀
                        </span>
                        <br/>
                        <span className="text-slate-800">AI 智能推理</span>
                    </motion.h1>

                    {/* Subtitle */}
                    <motion.p
                        initial={{opacity: 0, y: 30}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.8, delay: 0.2}}
                        className="text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto mb-10 leading-relaxed"
                    >
                        深入迷雾，解锁真相。与AI共织悬疑，每一场都是智力与勇气的较量。
                        <br className="hidden sm:block"/>
                        开启前所未有的沉浸式剧本杀体验。
                    </motion.p>

                    {/* CTA Buttons */}
                    <motion.div
                        initial={{opacity: 0, y: 30}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.8, delay: 0.3}}
                        className="flex flex-col sm:flex-row items-center justify-center gap-4"
                    >
                        <motion.div
                            whileHover={{scale: 1.02}}
                            whileTap={{scale: 0.98}}
                        >
                            <Link
                                to="/games"
                                className="group inline-flex items-center px-8 py-4 bg-gradient-to-r from-blue-500 to-blue-700 text-white text-lg font-semibold rounded-2xl shadow-xl shadow-blue-500/30 hover:shadow-2xl hover:shadow-blue-500/40 transition-all duration-300"
                            >
                                开始推理
                                <svg className="w-5 h-5 ml-2 transform group-hover:translate-x-1 transition-transform"
                                     fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M13 7l5 5m0 0l-5 5m5-5H6"/>
                                </svg>
                            </Link>
                        </motion.div>
                        <motion.div
                            whileHover={{scale: 1.02}}
                            whileTap={{scale: 0.98}}
                        >
                            <Link
                                to="/settings"
                                className="inline-flex items-center px-8 py-4 bg-white/80 backdrop-blur-sm text-slate-700 text-lg font-semibold rounded-2xl border border-slate-200 hover:border-blue-300 hover:bg-white hover:text-blue-600 shadow-lg shadow-slate-200/50 transition-all duration-300"
                            >
                                <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/>
                                </svg>
                                剧本工坊
                            </Link>
                        </motion.div>
                    </motion.div>

                    {/* Stats */}
                    <motion.div
                        initial={{opacity: 0, y: 30}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.8, delay: 0.4}}
                        className="mt-16 grid grid-cols-3 gap-8 max-w-lg mx-auto"
                    >
                        {[
                            {value: '10K+', label: '活跃玩家'},
                            {value: '500+', label: '原创剧本'},
                            {value: '50K+', label: '已开局数'},
                        ].map((stat, index) => (
                            <div key={index} className="text-center">
                                <div className="text-2xl sm:text-3xl font-bold text-slate-800">{stat.value}</div>
                                <div className="text-sm text-slate-500 mt-1">{stat.label}</div>
                            </div>
                        ))}
                    </motion.div>
                </div>

                {/* Scroll Indicator */}
                <motion.div
                    initial={{opacity: 0}}
                    animate={{opacity: 1}}
                    transition={{delay: 1, duration: 0.6}}
                    className="absolute bottom-4 sm:bottom-6 left-1/2 -translate-x-1/2"
                >
                    <motion.div
                        animate={{y: [0, 8, 0]}}
                        transition={{duration: 1.5, repeat: Infinity}}
                        className="w-6 h-10 rounded-full border-2 border-slate-300 flex items-start justify-center p-2"
                    >
                        <motion.div className="w-1.5 h-1.5 rounded-full bg-slate-400"/>
                    </motion.div>
                </motion.div>
            </section>

            {/* Features Section */}
            <section className="relative py-32 overflow-hidden">
                {/* Background */}
                <div className="absolute inset-0 bg-gradient-to-b from-transparent via-blue-50/50 to-transparent"/>

                <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    {/* Section Header */}
                    <motion.div
                        initial={{opacity: 0, y: 30}}
                        whileInView={{opacity: 1, y: 0}}
                        viewport={{once: true}}
                        transition={{duration: 0.6}}
                        className="text-center mb-20"
                    >
                        <span
                            className="inline-block px-4 py-1.5 rounded-full bg-blue-100 text-blue-700 text-sm font-medium mb-4">
                            核心特色
                        </span>
                        <h2 className="text-4xl sm:text-5xl font-bold text-slate-800 mb-4 tracking-tight">
                            重新定义剧本杀体验
                        </h2>
                        <p className="text-lg text-slate-600 max-w-2xl mx-auto">
                            融合人工智能技术与经典推理玩法，打造前所未有的沉浸式游戏体验
                        </p>
                    </motion.div>

                    {/* Features Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                        {features.map((feature, index) => (
                            <FeatureCard
                                key={feature.title}
                                icon={feature.icon}
                                title={feature.title}
                                description={feature.description}
                                index={index}
                            />
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="relative py-32 overflow-hidden">
                {/* Background */}
                <div className="absolute inset-0">
                    <div className="absolute inset-0 bg-gradient-to-br from-blue-600 to-blue-800"/>
                    <div
                        className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.1)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.1)_1px,transparent_1px)] bg-[size:40px_40px]"/>
                    <div
                        className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white/30 to-transparent"/>
                    <div
                        className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white/30 to-transparent"/>
                </div>

                {/* Decorative Elements */}
                <motion.div
                    animate={{
                        scale: [1, 1.2, 1],
                        opacity: [0.3, 0.5, 0.3]
                    }}
                    transition={{
                        duration: 8,
                        repeat: Infinity,
                        ease: "easeInOut"
                    }}
                    className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] rounded-full bg-blue-400/20 blur-3xl"
                />

                <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                    <motion.div
                        initial={{opacity: 0, y: 30}}
                        whileInView={{opacity: 1, y: 0}}
                        viewport={{once: true}}
                        transition={{duration: 0.6}}
                    >
                        <h2 className="text-4xl sm:text-5xl font-bold text-white mb-6 tracking-tight">
                            立即开启你的推理之旅
                        </h2>
                        <p className="text-xl text-blue-100 mb-10 max-w-2xl mx-auto leading-relaxed">
                            体验前所未有的沉浸式剧本杀乐趣，与AI一同揭开重重迷雾。
                            现在加入，开启你的第一局游戏。
                        </p>
                        <motion.div
                            whileHover={{scale: 1.02}}
                            whileTap={{scale: 0.98}}
                            className="inline-block"
                        >
                            <Link
                                to="/games"
                                className="group inline-flex items-center px-10 py-5 bg-white text-blue-600 text-lg font-bold rounded-2xl shadow-2xl shadow-blue-900/30 hover:shadow-blue-900/50 transition-all duration-300"
                            >
                                探索游戏房间
                                <svg className="w-5 h-5 ml-2 transform group-hover:translate-x-1 transition-transform"
                                     fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M13 7l5 5m0 0l-5 5m5-5H6"/>
                                </svg>
                            </Link>
                        </motion.div>
                    </motion.div>
                </div>
            </section>
        </div>
    )
}

export default Home
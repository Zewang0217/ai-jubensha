/**
 * @fileoverview Home 组件 - 华丽炫酷的沉浸式首页
 * @description AI剧本杀项目首页，展示核心特色、游戏流程、技术架构等
 * @author zewang
 * 
 * 设计特点：
 * - 科技感 + 悬疑氛围
 * - 粒子背景、霓虹光效
 * - 3D 翻转卡片
 * - 流畅的动画效果
 */

import {useState, useEffect, useMemo} from 'react'
import {Link} from 'react-router-dom'
import {motion, AnimatePresence} from 'framer-motion'
import {
    Sparkles,
    Play,
    BookOpen,
    Users,
    Bot,
    Search,
    MessageCircle,
    Vote,
    Trophy,
    Brain,
    Database,
    Zap,
    Cpu,
    ChevronRight,
    Star,
    Quote,
    Share2,
    Github,
    Twitter
} from 'lucide-react'

/**
 * 将十六进制颜色转换为 RGB 格式
 * @param {string} hex - 十六进制颜色值（如 #2563EB）
 * @returns {string} RGB 格式字符串（如 "37, 99, 235"）
 */
function hexToRgb(hex) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return result 
        ? `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}`
        : '37, 99, 235' // 默认返回科技电光蓝的 RGB 值
}

function Home() {
    const [activePhase, setActivePhase] = useState(0)
    const [stats, setStats] = useState({
        players: 10234,
        scripts: 567,
        games: 52341,
        conversations: 1234567,
        satisfaction: 98,
        accuracy: 85
    })

    useEffect(() => {
        const interval = setInterval(() => {
            setActivePhase(prev => (prev + 1) % 8)
        }, 3000)
        return () => clearInterval(interval)
    }, [])

    /**
     * 游戏流程阶段配置
     * 每个阶段都有独特的强调色和渐变，营造视觉层次感
     */
    const gamePhases = [
        {
            icon: <BookOpen className="w-6 h-6"/>,
            title: '剧本生成',
            desc: 'AI实时创作',
            accentColor: '#2563EB', // 科技电光蓝
            gradient: 'from-[#2563EB] to-[#3B82F6]',
            iconBgGradient: 'from-[#2563EB] to-[#3B82F6]'
        },
        {
            icon: <Users className="w-6 h-6"/>,
            title: '角色分配',
            desc: '智能匹配',
            accentColor: '#08D9D6', // 极光青
            gradient: 'from-[#08D9D6] to-[#06B6D4]',
            iconBgGradient: 'from-[#08D9D6] to-[#06B6D4]'
        },
        {
            icon: <BookOpen className="w-6 h-6"/>,
            title: '剧本阅读',
            desc: '沉浸体验',
            accentColor: '#1E40AF', // 深蓝
            gradient: 'from-[#1E40AF] to-[#2563EB]',
            iconBgGradient: 'from-[#1E40AF] to-[#2563EB]'
        },
        {
            icon: <Play className="w-6 h-6"/>,
            title: '开场介绍',
            desc: 'DM主持开场',
            accentColor: '#0E7490', // 青色
            gradient: 'from-[#0E7490] to-[#08D9D6]',
            iconBgGradient: 'from-[#0E7490] to-[#08D9D6]'
        },
        {
            icon: <Search className="w-6 h-6"/>,
            title: '场景搜证',
            desc: '线索发现',
            accentColor: '#4338CA', // 靛蓝
            gradient: 'from-[#4338CA] to-[#6366F1]',
            iconBgGradient: 'from-[#4338CA] to-[#6366F1]'
        },
        {
            icon: <MessageCircle className="w-6 h-6"/>,
            title: '讨论推理',
            desc: '智慧交锋',
            accentColor: '#155E75', // 蓝绿
            gradient: 'from-[#155E75] to-[#0891B2]',
            iconBgGradient: 'from-[#155E75] to-[#0891B2]'
        },
        {
            icon: <Vote className="w-6 h-6"/>,
            title: '投票指认',
            desc: '民主决策',
            accentColor: '#164E63', // 深青
            gradient: 'from-[#164E63] to-[#0E7490]',
            iconBgGradient: 'from-[#164E63] to-[#0E7490]'
        },
        {
            icon: <Trophy className="w-6 h-6"/>,
            title: '结局揭晓',
            desc: '真相大白',
            accentColor: '#F59E0B', // 金橙色 - 特殊强调
            gradient: 'from-[#F59E0B] to-[#FBBF24]',
            iconBgGradient: 'from-[#F59E0B] to-[#FBBF24]'
        },
    ]

    const agents = [
        {
            name: 'DM Agent',
            role: '主持人',
            icon: <Bot className="w-8 h-8"/>,
            features: ['流程控制', '氛围渲染', '线索发放', '评分仲裁']
        },
        {
            name: 'Player Agent',
            role: '玩家角色',
            icon: <Users className="w-8 h-8"/>,
            features: ['角色扮演', '推理分析', '隐藏秘密', '质疑反驳']
        },
        {
            name: 'Judge Agent',
            role: '裁判',
            icon: <Cpu className="w-8 h-8"/>,
            features: ['逻辑校验', '行为监控', '一致性检查', '规则仲裁']
        },
        {
            name: 'Summary Agent',
            role: '记录员',
            icon: <Database className="w-8 h-8"/>,
            features: ['对话摘要', '信息提取', '进度评估', '记忆管理']
        }
    ]

    const scripts = [
        {
            title: '华灯初上·血染百乐门',
            genre: '民国悬疑',
            players: '5-7人',
            duration: '120分钟',
            difficulty: '★★★★☆'
        },
        {
            title: '迷雾庄园的秘密',
            genre: '现代推理',
            players: '4-6人',
            duration: '90分钟',
            difficulty: '★★★☆☆'
        },
        {
            title: '时空裂隙',
            genre: '科幻悬疑',
            players: '6-8人',
            duration: '150分钟',
            difficulty: '★★★★★'
        },
        {
            title: '古刹惊魂',
            genre: '古风探案',
            players: '5-7人',
            duration: '100分钟',
            difficulty: '★★★☆☆'
        }
    ]

    const features = [
        {
            icon: <Bot className="w-7 h-7"/>,
            title: 'AI 主持',
            description: '智能 AI 主持人引导游戏流程，自动推进剧情发展，让每一场游戏体验都流畅自然。',
            tech: 'LangChain4j + LangGraph4j'
        },
        {
            icon: <Users className="w-7 h-7"/>,
            title: 'AI 玩家',
            description: '与智能 AI 玩家一起游戏，它们拥有独特的性格和推理能力，可撒谎、可伪装。',
            tech: '多Agent协作架构'
        },
        {
            icon: <BookOpen className="w-7 h-7"/>,
            title: '剧本生成',
            description: 'AI 辅助生成原创剧本，从悬疑推理到情感沉浸，多种题材风格任你选择。',
            tech: '流式生成 + RAG'
        },
        {
            icon: <Search className="w-7 h-7"/>,
            title: '智能搜证',
            description: '沉浸式搜证系统，自由探索场景中的每个角落，发现隐藏线索，还原案件真相。',
            tech: '场景图谱 + 线索关联'
        },
        {
            icon: <Brain className="w-7 h-7"/>,
            title: '三级记忆',
            description: '短期/中期/长期记忆系统，让 AI 拥有真实的记忆能力，避免"金鱼记忆"。',
            tech: 'Caffeine + Milvus'
        },
        {
            icon: <Database className="w-7 h-7"/>,
            title: 'RAG 检索',
            description: '向量检索 + 重排序，精准语义搜索，为 AI 提供相关的上下文信息。',
            tech: 'Milvus + BGE Reranker'
        },
        {
            icon: <Zap className="w-7 h-7"/>,
            title: '多轮推理',
            description: 'AI 玩家独立推理引擎，支持多轮思考、逻辑推演、证据关联。',
            tech: 'LangGraph 工作流'
        },
        {
            icon: <Cpu className="w-7 h-7"/>,
            title: '流式生成',
            description: '剧本实时生成，边生成边预览，支持流式输出和实时预览。',
            tech: 'Reactive Streams'
        }
    ]

    const reviews = [
        {name: '推理爱好者', rating: 5, comment: 'AI 玩家太真实了，完全分不出是真人还是 AI！'},
        {name: '剧本杀新手', rating: 5, comment: '剧本生成速度超快，每次都有新体验！'},
        {name: '资深玩家', rating: 5, comment: '搜证系统设计得很棒，线索关联很有意思！'},
        {name: '游戏主播', rating: 5, comment: 'DM 的主持很有氛围感，代入感满分！'}
    ]

    return (
        <div className="min-h-screen bg-[#FFFFFF] text-[#0F172A] overflow-hidden">
            <ParticleBackground/>
            
            <HeroSection stats={stats}/>
            
            <GameFlowSection phases={gamePhases} activePhase={activePhase}/>
            
            <AgentSection agents={agents}/>
            
            <ScriptShowcase scripts={scripts}/>
            
            <FeaturesSection features={features}/>
            
            <ArchitectureSection/>
            
            <StatsSection stats={stats}/>
            
            <ReviewsSection reviews={reviews}/>
            
            <CTASection/>
        </div>
    )
}

function ParticleBackground() {
    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden">
            <div className="absolute inset-0 bg-[#FFFFFF]"/>
            
            {[...Array(20)].map((_, i) => (
                <motion.div
                    key={i}
                    initial={{
                        x: Math.random() * (typeof window !== 'undefined' ? window.innerWidth : 1000),
                        y: Math.random() * (typeof window !== 'undefined' ? window.innerHeight : 1000),
                        opacity: 0
                    }}
                    animate={{
                        y: [null, -20, null],
                        opacity: [0.2, 0.5, 0.2],
                        scale: [1, 1.2, 1]
                    }}
                    transition={{
                        duration: 3 + Math.random() * 2,
                        repeat: Infinity,
                        delay: Math.random() * 2
                    }}
                    className="absolute w-1 h-1 bg-[#2563EB] rounded-full"
                    style={{
                        boxShadow: '0 0 10px rgba(37, 99, 235, 0.5)'
                    }}
                />
            ))}
            
            <motion.div
                animate={{
                    scale: [1, 1.2, 1],
                    opacity: [0.05, 0.1, 0.05]
                }}
                transition={{
                    duration: 8,
                    repeat: Infinity,
                    ease: "easeInOut"
                }}
                className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#2563EB]/10 rounded-full blur-3xl"
            />
            
            <motion.div
                animate={{
                    scale: [1.2, 1, 1.2],
                    opacity: [0.05, 0.1, 0.05]
                }}
                transition={{
                    duration: 10,
                    repeat: Infinity,
                    ease: "easeInOut"
                }}
                className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#08D9D6]/10 rounded-full blur-3xl"
            />
        </div>
    )
}

function HeroSection({stats}) {
    return (
        <section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-20">
            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                <motion.div
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6}}
                    className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-[#F8FAFC] backdrop-blur-sm border border-[#2563EB]/20 mb-8"
                >
                    <Sparkles className="w-5 h-5 text-[#2563EB]"/>
                    <span className="text-[#2563EB] text-sm font-medium">AI 驱动的沉浸式推理体验</span>
                </motion.div>
                
                <motion.h1
                    initial={{opacity: 0, y: 30}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, delay: 0.1}}
                    className="text-6xl sm:text-7xl md:text-8xl lg:text-9xl font-bold tracking-tight mb-6"
                >
                    <span className="relative inline-block">
                        <span className="bg-gradient-to-r from-[#2563EB] to-[#08D9D6] bg-clip-text text-transparent">
                            AI 剧本杀
                        </span>
                        <motion.div
                            animate={{opacity: [0.5, 1, 0.5]}}
                            transition={{duration: 2, repeat: Infinity}}
                            className="absolute inset-0 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] bg-clip-text text-transparent blur-xl"
                            style={{zIndex: -1}}
                        >
                            AI 剧本杀
                        </motion.div>
                    </span>
                </motion.h1>
                
                <motion.p
                    initial={{opacity: 0, y: 30}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, delay: 0.2}}
                    className="text-xl sm:text-2xl text-[#64748B] max-w-3xl mx-auto mb-6 leading-relaxed"
                >
                    当人工智能遇上沉浸式推理游戏
                </motion.p>
                
                <motion.p
                    initial={{opacity: 0, y: 30}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, delay: 0.3}}
                    className="text-lg text-[#64748B] max-w-2xl mx-auto mb-12"
                >
                    每一局都是独一无二的悬疑故事，AI 玩家将化身各种角色与你同台竞技
                </motion.p>
                
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, delay: 0.4}}
                    className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16"
                >
                    <motion.div whileHover={{scale: 1.05}} whileTap={{scale: 0.95}}>
                        <Link
                            to="/games"
                            className="group relative inline-flex items-center px-10 py-5 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] text-white text-lg font-bold rounded-2xl overflow-hidden shadow-[0_4px_14px_0_rgba(37,99,235,0.39)]"
                        >
                            <span className="absolute inset-0 bg-gradient-to-r from-[#08D9D6] to-[#2563EB] opacity-0 group-hover:opacity-100 transition-opacity"/>
                            <span className="relative flex items-center gap-2">
                                <Play className="w-6 h-6"/>
                                立即开始推理
                                <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform"/>
                            </span>
                        </Link>
                    </motion.div>
                    
                    <motion.div whileHover={{scale: 1.05}} whileTap={{scale: 0.95}}>
                        <Link
                            to="/scripts"
                            className="inline-flex items-center px-10 py-5 bg-white/80 backdrop-blur-sm text-[#0F172A] text-lg font-semibold rounded-2xl border border-[#2563EB]/20 hover:border-[#2563EB]/40 hover:bg-white hover:text-[#2563EB] shadow-lg transition-all"
                        >
                            <BookOpen className="w-6 h-6 mr-2"/>
                            探索剧本工坊
                        </Link>
                    </motion.div>
                </motion.div>
                
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.8, delay: 0.5}}
                    className="grid grid-cols-2 md:grid-cols-4 gap-6 max-w-4xl mx-auto"
                >
                    {[
                        {value: stats.players, label: '活跃玩家', suffix: '+'},
                        {value: stats.scripts, label: '原创剧本', suffix: '+'},
                        {value: stats.games, label: '已开局数', suffix: '+'},
                        {value: stats.conversations, label: 'AI对话轮次', suffix: '+'}
                    ].map((stat, index) => (
                        <div key={index} className="text-center">
                            <div className="text-3xl sm:text-4xl font-bold text-[#0F172A] mb-1">
                                {stat.value.toLocaleString()}{stat.suffix}
                            </div>
                            <div className="text-sm text-[#64748B]">{stat.label}</div>
                        </div>
                    ))}
                </motion.div>
            </div>
            
            <motion.div
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{delay: 1, duration: 0.6}}
                className="absolute bottom-8 left-1/2 -translate-x-1/2"
            >
                <motion.div
                    animate={{y: [0, 8, 0]}}
                    transition={{duration: 1.5, repeat: Infinity}}
                    className="w-6 h-10 rounded-full border-2 border-[#2563EB]/30 flex items-start justify-center p-2"
                >
                    <motion.div className="w-1.5 h-1.5 rounded-full bg-[#2563EB]"/>
                </motion.div>
            </motion.div>
        </section>
    )
}

/**
 * 游戏流程展示组件
 * @param {Object} props - 组件属性
 * @param {Array} props.phases - 游戏阶段配置数组
 * @param {number} props.activePhase - 当前激活的阶段索引
 * @returns {JSX.Element} 游戏流程展示区域
 */
function GameFlowSection({phases, activePhase}) {
    return (
        <section className="relative py-32 overflow-hidden bg-[#F8FAFC]">
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        游戏流程
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        完整的游戏体验
                    </h2>
                    <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
                        从剧本生成到结局揭晓，每一步都由 AI 智能驱动
                    </p>
                </motion.div>
                
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {phases.map((phase, index) => {
                        const isActive = activePhase === index
                        const accentColorRgb = hexToRgb(phase.accentColor)
                        
                        return (
                            <motion.div
                                key={index}
                                initial={{opacity: 0, y: 20}}
                                whileInView={{opacity: 1, y: 0}}
                                viewport={{once: true}}
                                transition={{duration: 0.5, delay: index * 0.1}}
                                whileHover={{scale: 1.05, y: -5}}
                                className={`relative group cursor-pointer ${isActive ? 'z-10' : ''}`}
                            >
                                <div 
                                    className={`relative p-6 rounded-2xl border transition-all duration-300 ${
                                        isActive
                                            ? `bg-gradient-to-br ${phase.gradient} border-white/30 text-white`
                                            : 'bg-white border-gray-200/50 text-[#0F172A] hover:border-2'
                                    }`}
                                    style={!isActive ? {
                                        '--accent-color': phase.accentColor,
                                        '--accent-color-rgb': accentColorRgb,
                                        borderColor: 'transparent',
                                    } : {}}
                                    onMouseEnter={(e) => {
                                        if (!isActive) {
                                            e.currentTarget.style.borderColor = phase.accentColor
                                            e.currentTarget.style.boxShadow = `0 4px 14px 0 rgba(${accentColorRgb}, 0.25)`
                                        }
                                    }}
                                    onMouseLeave={(e) => {
                                        if (!isActive) {
                                            e.currentTarget.style.borderColor = 'transparent'
                                            e.currentTarget.style.boxShadow = 'none'
                                        }
                                    }}
                                >
                                    {/* 图标容器 - 使用阶段特定的渐变 */}
                                    <div className={`w-12 h-12 rounded-xl mb-4 flex items-center justify-center transition-all duration-300 ${
                                        isActive
                                            ? 'bg-white/20 text-white'
                                            : `bg-gradient-to-br ${phase.iconBgGradient} text-white group-hover:scale-110`
                                    }`}>
                                        {phase.icon}
                                    </div>
                                    
                                    <h3 className="text-lg font-bold mb-1">{phase.title}</h3>
                                    <p className={`text-sm ${isActive ? 'text-white/80' : 'text-[#64748B]'}`}>
                                        {phase.desc}
                                    </p>
                                    
                                    {/* 激活状态指示器 */}
                                    {isActive && (
                                        <motion.div
                                            layoutId="activeIndicator"
                                            className="absolute -inset-1 rounded-2xl border-2"
                                            style={{
                                                borderColor: phase.accentColor,
                                                boxShadow: `0 0 20px ${phase.accentColor}40`,
                                                zIndex: -1
                                            }}
                                        />
                                    )}
                                </div>
                            </motion.div>
                        )
                    })}
                </div>
            </div>
        </section>
    )
}

function AgentSection({agents}) {
    return (
        <section className="relative py-32 overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#F8FAFC] to-transparent"/>
            
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        AI Agent 系统
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        多Agent协作架构
                    </h2>
                    <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
                        四大 AI Agent 各司其职，共同打造沉浸式推理体验
                    </p>
                </motion.div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {agents.map((agent, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            whileHover={{y: -8}}
                            className="group relative"
                        >
                            <div className="relative p-6 bg-white/80 backdrop-blur-[10px] rounded-2xl border border-[#2563EB]/10 hover:border-[#2563EB]/30 transition-all h-full">
                                <div className="w-16 h-16 rounded-2xl mb-4 flex items-center justify-center bg-gradient-to-br from-[#2563EB] to-[#08D9D6] text-white shadow-lg group-hover:scale-110 transition-transform">
                                    {agent.icon}
                                </div>
                                <h3 className="text-xl font-bold mb-1 text-[#0F172A]">{agent.name}</h3>
                                <p className="text-sm text-[#64748B] mb-4">{agent.role}</p>
                                <ul className="space-y-2">
                                    {agent.features.map((feature, i) => (
                                        <li key={i} className="flex items-center gap-2 text-sm text-[#64748B]">
                                            <div className="w-1.5 h-1.5 rounded-full bg-[#08D9D6]"/>
                                            {feature}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function ScriptShowcase({scripts}) {
    return (
        <section className="relative py-32 overflow-hidden bg-[#F8FAFC]">
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        精选剧本
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        沉浸式剧本体验
                    </h2>
                    <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
                        AI 生成的原创剧本，每次都有新体验
                    </p>
                </motion.div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {scripts.map((script, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            whileHover={{y: -8, rotateY: 5}}
                            className="group relative cursor-pointer"
                        >
                            <div className="relative h-64 rounded-2xl overflow-hidden bg-gradient-to-br from-[#2563EB] to-[#08D9D6] p-6 flex flex-col justify-end shadow-[0_4px_14px_0_rgba(37,99,235,0.39)]">
                                <div className="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors"/>
                                <div className="relative">
                                    <h3 className="text-xl font-bold mb-2 text-white">{script.title}</h3>
                                    <p className="text-sm text-white/80 mb-3">{script.genre}</p>
                                    <div className="flex items-center gap-4 text-sm text-white/90">
                                        <span>{script.players}</span>
                                        <span>{script.duration}</span>
                                        <span>{script.difficulty}</span>
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function FeaturesSection({features}) {
    return (
        <section className="relative py-32 overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#F8FAFC] to-transparent"/>
            
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        核心特色
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        技术驱动的创新体验
                    </h2>
                    <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
                        融合前沿 AI 技术，打造前所未有的沉浸式游戏体验
                    </p>
                </motion.div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {features.map((feature, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.05}}
                            whileHover={{y: -5}}
                            className="group relative"
                        >
                            <div className="relative p-6 bg-white/80 backdrop-blur-sm rounded-2xl border border-[#2563EB]/10 hover:border-[#2563EB]/30 hover:shadow-[0_4px_14px_0_rgba(37,99,235,0.39)] transition-all h-full">
                                <div className="w-14 h-14 rounded-xl mb-4 flex items-center justify-center bg-gradient-to-br from-[#2563EB] to-[#08D9D6] text-white shadow-lg group-hover:scale-110 transition-transform">
                                    {feature.icon}
                                </div>
                                <h3 className="text-lg font-bold mb-2 text-[#0F172A]">{feature.title}</h3>
                                <p className="text-sm text-[#64748B] mb-3">{feature.description}</p>
                                <div className="text-xs text-[#2563EB] font-mono">{feature.tech}</div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function ArchitectureSection() {
    const layers = [
        {
            title: '前端层 (React 19)',
            items: ['剧本大厅', '游戏房间', '搜证界面', '讨论界面']
        },
        {
            title: '后端层 (Spring Boot 3)',
            items: ['AI Agent 系统 (LangGraph4j)', '游戏流程 | 记忆管理 | RAG检索 | 剧本生成']
        },
        {
            title: '数据存储层',
            items: ['MySQL', 'Redis', 'Milvus', 'RabbitMQ']
        }
    ]

    return (
        <section className="relative py-32 overflow-hidden bg-[#F8FAFC]">
            <div className="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        技术架构
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        现代化技术栈
                    </h2>
                    <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
                        前后端分离架构，支持高并发和实时通信
                    </p>
                </motion.div>
                
                <div className="space-y-4">
                    {layers.map((layer, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, x: -20}}
                            whileInView={{opacity: 1, x: 0}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            className="relative"
                        >
                            <div className="relative p-6 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] rounded-2xl border border-[#08D9D6]/30 shadow-[0_4px_14px_0_rgba(37,99,235,0.39)]">
                                <h3 className="text-lg font-bold mb-3 text-white">{layer.title}</h3>
                                <div className="flex flex-wrap gap-2">
                                    {layer.items.map((item, i) => (
                                        <span
                                            key={i}
                                            className="px-3 py-1 bg-white/20 rounded-lg text-sm text-white"
                                        >
                                            {item}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function StatsSection({stats}) {
    const statItems = [
        {value: stats.satisfaction, suffix: '%', label: '玩家满意度', color: 'text-[#2563EB]'},
        {value: stats.accuracy, suffix: '%', label: 'AI推理准确率', color: 'text-[#08D9D6]'},
        {value: 3, suffix: '分钟', label: '平均剧本生成时长', color: 'text-[#2563EB]'},
        {value: 98, suffix: '%', label: '系统可用性', color: 'text-[#08D9D6]'}
    ]

    return (
        <section className="relative py-32 overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-[#F8FAFC] to-transparent"/>
            
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        数据统计
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        实时运营数据
                    </h2>
                </motion.div>
                
                <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                    {statItems.map((stat, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, scale: 0.9}}
                            whileInView={{opacity: 1, scale: 1}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            className="text-center"
                        >
                            <div className={`text-5xl sm:text-6xl font-bold ${stat.color} mb-2`}>
                                {stat.value}{stat.suffix}
                            </div>
                            <div className="text-sm text-[#64748B]">{stat.label}</div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function ReviewsSection({reviews}) {
    return (
        <section className="relative py-32 overflow-hidden bg-[#F8FAFC]">
            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                    className="text-center mb-16"
                >
                    <span className="inline-block px-4 py-1.5 rounded-full bg-[#2563EB]/10 text-[#2563EB] text-sm font-medium mb-4">
                        玩家评价
                    </span>
                    <h2 className="text-4xl sm:text-5xl font-bold mb-4 text-[#0F172A]">
                        真实玩家反馈
                    </h2>
                </motion.div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {reviews.map((review, index) => (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 20}}
                            whileInView={{opacity: 1, y: 0}}
                            viewport={{once: true}}
                            transition={{duration: 0.5, delay: index * 0.1}}
                            whileHover={{y: -5}}
                            className="group"
                        >
                            <div className="relative p-6 bg-white/80 backdrop-blur-[10px] rounded-2xl border border-[#2563EB]/10 hover:border-[#2563EB]/30 transition-all h-full">
                                <div className="flex items-center gap-1 mb-3">
                                    {[...Array(review.rating)].map((_, i) => (
                                        <Star key={i} className="w-4 h-4 text-[#08D9D6] fill-[#08D9D6]"/>
                                    ))}
                                </div>
                                <Quote className="w-8 h-8 text-[#2563EB]/20 mb-2"/>
                                <p className="text-sm text-[#64748B] mb-4">{review.comment}</p>
                                <div className="text-xs text-[#64748B]">— {review.name}</div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}

function CTASection() {
    return (
        <section className="relative py-32 overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-[#2563EB] to-[#08D9D6]"/>
            <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.1)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.1)_1px,transparent_1px)] bg-[size:40px_40px]"/>
            
            <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                <motion.div
                    initial={{opacity: 0, y: 30}}
                    whileInView={{opacity: 1, y: 0}}
                    viewport={{once: true}}
                    transition={{duration: 0.6}}
                >
                    <h2 className="text-4xl sm:text-5xl font-bold mb-6 text-white">
                        立即开启你的推理之旅
                    </h2>
                    <p className="text-xl text-white/90 mb-10 max-w-2xl mx-auto leading-relaxed">
                        体验前所未有的沉浸式剧本杀乐趣，与AI一同揭开重重迷雾。
                        现在加入，开启你的第一局游戏。
                    </p>
                    
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-8">
                        <motion.div whileHover={{scale: 1.05}} whileTap={{scale: 0.95}}>
                            <Link
                                to="/games"
                                className="group inline-flex items-center px-10 py-5 bg-white text-[#2563EB] text-lg font-bold rounded-2xl shadow-2xl hover:shadow-white/25 transition-all"
                            >
                                探索游戏房间
                                <ChevronRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform"/>
                            </Link>
                        </motion.div>
                    </div>
                    
                    <div className="flex items-center justify-center gap-4">
                        <button className="p-3 bg-white/10 backdrop-blur-sm rounded-xl hover:bg-white/20 transition-colors text-white">
                            <Share2 className="w-5 h-5"/>
                        </button>
                        <button className="p-3 bg-white/10 backdrop-blur-sm rounded-xl hover:bg-white/20 transition-colors text-white">
                            <Github className="w-5 h-5"/>
                        </button>
                        <button className="p-3 bg-white/10 backdrop-blur-sm rounded-xl hover:bg-white/20 transition-colors text-white">
                            <Twitter className="w-5 h-5"/>
                        </button>
                    </div>
                </motion.div>
            </div>
        </section>
    )
}

export default Home

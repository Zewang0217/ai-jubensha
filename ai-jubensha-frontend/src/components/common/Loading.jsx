/**
 * @fileoverview Loading 组件 - 科技感加载动画
 * @description 支持全屏模式、自定义文字和样式的加载组件，符合项目科技感+悬疑氛围设计风格
 * @author zewang
 * 
 * 设计特点：
 * - 科技电光蓝 #2563EB + 极光青 #08D9D6 配色
 * - 流畅的旋转动画和脉冲效果
 * - 毛玻璃质感卡片
 * - 漂浮的装饰粒子
 */

import {useMemo} from 'react'
import {motion} from 'framer-motion'
import {Loader2, Sparkles} from 'lucide-react'

const SIZE_CONFIG = {
    small: {icon: 'w-8 h-8', text: 'text-sm', padding: 'p-6'},
    medium: {icon: 'w-12 h-12', text: 'text-base', padding: 'p-8'},
    large: {icon: 'w-16 h-16', text: 'text-lg', padding: 'p-10'}
}

const PARTICLE_PRESETS = [
    {x: 15, y: 25, duration: 3.2, delay: 0.5},
    {x: 75, y: 35, duration: 4.1, delay: 1.2},
    {x: 25, y: 65, duration: 3.8, delay: 0.8},
    {x: 85, y: 55, duration: 4.5, delay: 1.5},
    {x: 45, y: 15, duration: 3.5, delay: 0.3},
    {x: 55, y: 85, duration: 4.2, delay: 1.8}
]

function LoadingContent({config, text}) {
    return (
        <motion.div
            initial={{opacity: 0, scale: 0.9}}
            animate={{opacity: 1, scale: 1}}
            transition={{duration: 0.4}}
            className="flex flex-col items-center justify-center relative z-10"
        >
            <div className={`relative ${config.padding} bg-white/80 backdrop-blur-xl rounded-2xl border border-[#E2E8F0] shadow-xl`}>
                <div className="absolute -top-2 -right-2">
                    <motion.div
                        animate={{rotate: 360}}
                        transition={{duration: 3, repeat: Infinity, ease: 'linear'}}
                    >
                        <Sparkles className="w-5 h-5 text-[#08D9D6]"/>
                    </motion.div>
                </div>
                
                <div className="relative">
                    <motion.div
                        animate={{rotate: 360}}
                        transition={{duration: 1.2, repeat: Infinity, ease: 'linear'}}
                        className={`${config.icon} relative`}
                    >
                        <div className="absolute inset-0 bg-gradient-to-r from-[#2563EB] to-[#08D9D6] rounded-full opacity-20 blur-md"/>
                        <Loader2 className="w-full h-full text-[#2563EB] relative z-10"/>
                    </motion.div>
                    
                    <motion.div
                        animate={{scale: [1, 1.2, 1], opacity: [0.5, 0.8, 0.5]}}
                        transition={{duration: 1.5, repeat: Infinity, ease: 'easeInOut'}}
                        className="absolute inset-0 flex items-center justify-center"
                    >
                        <div className="w-3 h-3 rounded-full bg-gradient-to-r from-[#2563EB] to-[#08D9D6]"/>
                    </motion.div>
                </div>
                
                <motion.p
                    animate={{opacity: [0.6, 1, 0.6]}}
                    transition={{duration: 2, repeat: Infinity, ease: 'easeInOut'}}
                    className={`mt-4 ${config.text} text-[#64748B] font-medium text-center`}
                >
                    {text}
                </motion.p>
            </div>
        </motion.div>
    )
}

function BackgroundDecorations() {
    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden">
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5}}
                className="absolute top-0 -left-32 w-96 h-96 bg-[#2563EB]/20 rounded-full blur-3xl"
            />
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5, delay: 0.2}}
                className="absolute bottom-0 -right-32 w-96 h-96 bg-[#08D9D6]/20 rounded-full blur-3xl"
            />
            
            <motion.div
                initial={{opacity: 0, scale: 0.8}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 1.5, delay: 0.4}}
                className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-[#2563EB]/10 to-[#08D9D6]/10 rounded-full blur-3xl"
            />
            
            {PARTICLE_PRESETS.map((preset, i) => (
                <motion.div
                    key={i}
                    initial={{
                        x: `${preset.x}%`,
                        y: `${preset.y}%`,
                        opacity: 0
                    }}
                    animate={{
                        y: [null, '-30px', null],
                        opacity: [0.2, 0.6, 0.2],
                        scale: [1, 1.5, 1]
                    }}
                    transition={{
                        duration: preset.duration,
                        repeat: Infinity,
                        delay: preset.delay
                    }}
                    className={`absolute w-1.5 h-1.5 rounded-full ${i % 2 === 0 ? 'bg-[#2563EB]' : 'bg-[#08D9D6]'}`}
                    style={{
                        boxShadow: i % 2 === 0 
                            ? '0 0 10px rgba(37, 99, 235, 0.6)' 
                            : '0 0 10px rgba(8, 217, 214, 0.6)'
                    }}
                />
            ))}
            
            <motion.div
                animate={{y: [0, -20, 0], rotate: [0, 5, 0]}}
                transition={{duration: 6, repeat: Infinity, ease: 'easeInOut'}}
                className="absolute top-32 right-[15%] w-12 h-12 rounded-xl bg-gradient-to-br from-[#2563EB]/30 to-[#08D9D6]/30 backdrop-blur-sm border border-[#2563EB]/30"
            />
            
            <motion.div
                animate={{y: [0, 15, 0], rotate: [0, -5, 0]}}
                transition={{duration: 5, repeat: Infinity, ease: 'easeInOut', delay: 1}}
                className="absolute bottom-40 left-[10%] w-10 h-10 rounded-lg bg-gradient-to-br from-[#2563EB]/30 to-[#08D9D6]/30 backdrop-blur-sm border border-[#2563EB]/30"
            />
        </div>
    )
}

/**
 * Loading 组件 - 科技感加载动画
 * @param {Object} props - 组件属性
 * @param {boolean} [props.fullScreen=false] - 是否全屏显示
 * @param {string} [props.text='加载中...'] - 加载提示文字
 * @param {string} [props.className=''] - 自定义背景类名
 * @param {'small'|'medium'|'large'} [props.size='medium'] - 加载器大小
 * @returns {JSX.Element} 加载组件
 */
function Loading({
    fullScreen = false,
    text = '加载中...',
    className = '',
    size = 'medium'
}) {
    const config = useMemo(() => SIZE_CONFIG[size] || SIZE_CONFIG.medium, [size])

    if (fullScreen) {
        return (
            <div className={`min-h-screen flex items-center justify-center relative overflow-hidden ${className}`}>
                <BackgroundDecorations/>
                <LoadingContent config={config} text={text}/>
            </div>
        )
    }

    return (
        <div className="flex items-center justify-center p-8">
            <LoadingContent config={config} text={text}/>
import {motion} from 'framer-motion'

/**
 * Loading 组件 - 科技感神秘加载动画
 * @description 带有层次感的旋转加载器和动态文字效果
 * @param {Object} props - 组件属性
 * @param {string} [props.text='加载中'] - 加载提示文字
 * @param {boolean} [props.fullScreen=false] - 是否全屏显示
 * @param {string} [props.className=''] - 额外的 CSS 类名
 */
function Loading({text = '加载中', fullScreen = false, className = ''}) {
    const containerClasses = fullScreen
        ? `fixed inset-0 z-50 flex flex-col items-center justify-center bg-gradient-to-br from-[#F8FAFC] via-white to-[#F8FAFC] dark:from-[#1A1D26] dark:via-[#222631] dark:to-[#1A1D26] ${className}`
        : `flex flex-col items-center justify-center py-12 ${className}`

    return (
        <div className={containerClasses}>
            {/* 加载动画容器 */}
            <div className="relative w-24 h-24 mb-8">
                {/* 外圈 - 逆时针旋转 */}
                <motion.div
                    className="absolute inset-0 rounded-full border-2 border-dashed"
                    style={{
                        borderColor: 'rgba(124, 140, 214, 0.3)',
                    }}
                    animate={{rotate: -360}}
                    transition={{duration: 8, repeat: Infinity, ease: 'linear'}}
                />

                {/* 中外圈 - 渐变圆弧 */}
                <motion.div
                    className="absolute inset-1 rounded-full"
                    style={{
                        background: 'conic-gradient(from 0deg, transparent 0deg, rgba(124, 140, 214, 0.6) 60deg, transparent 120deg)',
                    }}
                    animate={{rotate: 360}}
                    transition={{duration: 3, repeat: Infinity, ease: 'linear'}}
                />

                {/* 中圈 - 顺时针旋转 */}
                <motion.div
                    className="absolute inset-2 rounded-full border-2"
                    style={{
                        borderColor: 'rgba(167, 139, 250, 0.5)',
                        borderStyle: 'dashed',
                    }}
                    animate={{rotate: 360}}
                    transition={{duration: 6, repeat: Infinity, ease: 'linear'}}
                />

                {/* 内圈 - 渐变填充 */}
                <motion.div
                    className="absolute inset-4 rounded-full"
                    style={{
                        background: 'conic-gradient(from 180deg, rgba(124, 140, 214, 0.8), rgba(167, 139, 250, 0.8), rgba(245, 169, 201, 0.8), rgba(124, 140, 214, 0.8))',
                    }}
                    animate={{rotate: -360}}
                    transition={{duration: 4, repeat: Infinity, ease: 'linear'}}
                />

                {/* 中心核心 - 脉动效果 */}
                <motion.div
                    className="absolute inset-0 flex items-center justify-center"
                    animate={{scale: [1, 1.1, 1]}}
                    transition={{duration: 2, repeat: Infinity, ease: 'easeInOut'}}
                >
                    <div
                        className="w-4 h-4 rounded-full bg-gradient-to-br from-[#7C8CD6] to-[#A78BFA] shadow-lg shadow-[#7C8CD6]/50"/>
                </motion.div>

                {/* 光晕效果 */}
                <motion.div
                    className="absolute inset-0 rounded-full blur-xl opacity-50"
                    style={{
                        background: 'radial-gradient(circle, rgba(124, 140, 214, 0.5) 0%, transparent 70%)',
                    }}
                    animate={{
                        scale: [1, 1.2, 1],
                        opacity: [0.3, 0.5, 0.3]
                    }}
                    transition={{duration: 3, repeat: Infinity, ease: 'easeInOut'}}
                />
            </div>

            {/* 文字区域 */}
            <div className="flex flex-col items-center gap-2">
                {/* 主标题 */}
                <div className="flex items-center gap-1">
                    {text.split('').map((char, index) => (
                        <motion.span
                            key={index}
                            className="text-lg font-bold bg-gradient-to-r from-[#7C8CD6] to-[#A78BFA] bg-clip-text text-transparent"
                            initial={{opacity: 0, y: 10}}
                            animate={{opacity: 1, y: 0}}
                            transition={{
                                delay: index * 0.05,
                                duration: 0.3
                            }}
                        >
                            {char}
                        </motion.span>
                    ))}
                    {/* 动态省略号 */}
                    {[0, 1, 2].map((i) => (
                        <motion.span
                            key={`dot-${i}`}
                            className="text-[#7C8CD6] text-xl font-bold"
                            animate={{
                                opacity: [0.2, 1, 0.2],
                                y: [0, -4, 0]
                            }}
                            transition={{
                                duration: 1.5,
                                repeat: Infinity,
                                delay: i * 0.2
                            }}
                        >
                            .
                        </motion.span>
                    ))}
                </div>

                {/* 副标题 */}
                <motion.p
                    className="text-sm text-[#8C96A5] dark:text-[#6B7788]"
                    animate={{opacity: [0.5, 1, 0.5]}}
                    transition={{duration: 2, repeat: Infinity, ease: 'easeInOut'}}
                >
                    正在准备剧本世界
                </motion.p>
            </div>

            {/* 装饰性粒子 - 预定义位置避免随机数 */}
            {[
                {top: '25%', left: '15%', delay: 0},
                {top: '35%', left: '85%', delay: 0.3},
                {top: '65%', left: '20%', delay: 0.6},
                {top: '75%', left: '80%', delay: 0.9},
                {top: '45%', left: '10%', delay: 1.2},
                {top: '55%', left: '90%', delay: 1.5},
            ].map((pos, i) => (
                <motion.div
                    key={i}
                    className="absolute w-1 h-1 rounded-full"
                    style={{
                        backgroundColor: i % 2 === 0 ? '#7C8CD6' : '#F5A9C9',
                        top: pos.top,
                        left: pos.left,
                    }}
                    animate={{
                        opacity: [0.2, 0.8, 0.2],
                        scale: [0.8, 1.2, 0.8],
                    }}
                    transition={{
                        duration: 2 + (i * 0.2),
                        repeat: Infinity,
                        delay: pos.delay,
                    }}
                />
            ))}
        </div>
    )
}

export default Loading
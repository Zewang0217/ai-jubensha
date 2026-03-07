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
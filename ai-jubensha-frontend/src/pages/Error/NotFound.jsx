import {Link} from 'react-router-dom'
import {motion} from 'framer-motion'

function NotFound() {
    return (
        <div className="min-h-[60vh] flex items-center justify-center">
            <motion.div
                initial={{opacity: 0, scale: 0.9}}
                animate={{opacity: 1, scale: 1}}
                transition={{duration: 0.3}}
                className="card text-center max-w-md w-full"
            >
                <div className="text-6xl mb-4">🎭</div>
                <h1 className="text-4xl font-bold text-(--color-secondary-800) mb-2">
                    404
                </h1>
                <h2 className="text-xl font-semibold text-(--color-secondary-700) mb-4">
                    页面未找到
                </h2>
                <p className="text-(--color-secondary-600) mb-8">
                    您访问的页面不存在或已被移除。
                    <br/>
                    请检查网址是否正确，或返回首页。
                </p>
                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                    <Link
                        to="/"
                        className="btn-primary"
                    >
                        <span className="mr-2">🏠</span>
                        返回首页
                    </Link>
                    <Link
                        to="/games"
                        className="btn-secondary"
                    >
                        <span className="mr-2">🎮</span>
                        游戏大厅
                    </Link>
                </div>
            </motion.div>
        </div>
    )
}

export default NotFound

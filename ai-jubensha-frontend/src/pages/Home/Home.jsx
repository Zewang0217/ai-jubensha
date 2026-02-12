import {Link} from 'react-router-dom'

function Home() {
    const features = [
        {
            title: 'AI 主持',
            description: '智能 AI 主持人引导游戏流程，让游戏体验更流畅'
        },
        {
            title: 'AI 玩家',
            description: '与智能 AI 玩家一起游戏，随时开始精彩对局'
        },
        {
            title: '剧本生成',
            description: 'AI 辅助生成剧本，创造独特的游戏体验'
        },
        {
            title: '智能搜证',
            description: '沉浸式搜证系统，探索案件真相'
        }
    ]

    return (
        <div>
            <section>
                <h1>
                    剧本杀 AI 智能推理
                </h1>
                <p>
                    深入迷雾，解锁真相。与AI共织悬疑，每一场都是智力与勇气的较量。
                </p>
                <div>
                    <Link to="/games">
                        开始推理
                    </Link>
                    <Link to="/settings">
                        剧本工坊
                    </Link>
                </div>
            </section>

            <section>
                <h2>核心特色</h2>
                <div>
                    {features.map((feature) => (
                        <div key={feature.title}>
                            <h3>{feature.title}</h3>
                            <p>{feature.description}</p>
                        </div>
                    ))}
                </div>
            </section>

            <section>
                <h2>立即开启你的推理之旅</h2>
                <p>体验前所未有的沉浸式剧本杀乐趣，与AI一同揭开重重迷雾。</p>
                <Link to="/games">
                    探索游戏房间
                </Link>
            </section>
        </div>
    )
}

export default Home

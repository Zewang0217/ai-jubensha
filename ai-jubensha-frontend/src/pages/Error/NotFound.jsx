import {Link} from 'react-router-dom'

function NotFound() {
    return (
        <div>
            <h1>404</h1>
            <h2>页面未找到</h2>
            <p>您访问的页面不存在或已被移除。请检查网址是否正确，或返回首页。</p>
            <Link to="/">返回首页</Link>
            <Link to="/games">游戏大厅</Link>
        </div>
    )
}

export default NotFound

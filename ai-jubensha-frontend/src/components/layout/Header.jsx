import {Link, useLocation} from 'react-router-dom'
import {useState} from 'react'

const navItems = [
    {path: '/', label: '首页'},
    {path: '/games', label: '游戏大厅'},
    {path: '/settings', label: '设置'},
]

function Header() {
    const location = useLocation()
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

    const isActive = (path) => {
        if (path === '/') {
            return location.pathname === '/'
        }
        return location.pathname.startsWith(path)
    }

    return (
        <header>
            <div>
                <div>
                    <Link to="/">
                        <span>AI剧本杀</span>
                    </Link>

                    <nav>
                        {navItems.map((item) => (
                            <Link
                                key={item.path}
                                to={item.path}
                            >
                                {item.label}
                            </Link>
                        ))}
                    </nav>

                    <button onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}>
                        {isMobileMenuOpen ? '关闭' : '菜单'}
                    </button>
                </div>

                {isMobileMenuOpen && (
                    <nav>
                        <div>
                            {navItems.map((item) => (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setIsMobileMenuOpen(false)}
                                >
                                    {item.label}
                                </Link>
                            ))}
                        </div>
                    </nav>
                )}
            </div>
        </header>
    )
}

export default Header

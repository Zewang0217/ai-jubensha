import {Link, useLocation} from 'react-router-dom'
import {useState} from 'react'

const navItems = [
    {path: '/', label: '首页', icon: '🏠'},
    {path: '/games', label: '游戏大厅', icon: '🎮'},
    {path: '/settings', label: '设置', icon: '⚙️'},
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
        <header className="frosted-glass-effect fixed top-0 left-0 right-0 z-50 py-3">
            <div className="container mx-auto px-4">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <Link to="/" className="flex items-center space-x-2 text-white">
                        <span className="text-2xl">🎭</span>
                        <span className="text-xl font-bold">AI剧本杀</span>
                    </Link>

                    {/* Desktop Navigation */}
                    <nav className="hidden md:flex items-center space-x-1">
                        {navItems.map((item) => (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`px-4 py-2 rounded-lg font-medium transition-all duration-200 ${
                                    isActive(item.path)
                                        ? 'bg-dark-red-500 text-white'
                                        : 'text-secondary-200 hover:text-white hover:bg-white/10'
                                }`}
                            >
                                <span className="mr-2">{item.icon}</span>
                                {item.label}
                            </Link>
                        ))}
                    </nav>

                    {/* Mobile Menu Button */}
                    <button
                        className="md:hidden p-2 rounded-lg text-secondary-200 hover:bg-white/10 transition-colors"
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    >
                        <span className="text-2xl">{isMobileMenuOpen ? '✕' : '☰'}</span>
                    </button>
                </div>

                {/* Mobile Navigation */}
                {isMobileMenuOpen && (
                    <nav className="md:hidden py-4 border-t border-white/10">
                        <div className="flex flex-col space-y-2">
                            {navItems.map((item) => (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    className={`px-4 py-3 rounded-lg font-medium transition-all duration-200 ${
                                        isActive(item.path)
                                            ? 'bg-dark-red-500 text-white'
                                            : 'text-secondary-200 hover:text-white hover:bg-white/10'
                                    }`}
                                    onClick={() => setIsMobileMenuOpen(false)}
                                >
                                    <span className="mr-3">{item.icon}</span>
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

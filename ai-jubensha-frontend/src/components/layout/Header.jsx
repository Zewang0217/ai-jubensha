import {Link, useLocation} from 'react-router-dom'
import {useEffect, useState} from 'react'
import {AnimatePresence, motion} from 'framer-motion'

const navItems = [
    {path: '/', label: '首页'},
    {path: '/games', label: '游戏大厅'},
    {path: '/settings', label: '设置'},
]

function Header() {
    const location = useLocation()
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
    const [isScrolled, setIsScrolled] = useState(false)

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 20)
        }
        window.addEventListener('scroll', handleScroll)
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])

    const isActive = (path) => {
        if (path === '/') {
            return location.pathname === '/'
        }
        return location.pathname.startsWith(path)
    }

    return (
        <motion.header
            initial={{y: -100}}
            animate={{y: 0}}
            transition={{duration: 0.6, ease: [0.25, 0.46, 0.45, 0.94]}}
            className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
                isScrolled
                    ? 'bg-white/90 backdrop-blur-xl shadow-lg shadow-blue-900/5 border-b border-blue-100/50'
                    : 'bg-transparent'
            }`}
        >
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16 md:h-20">
                    {/* Logo */}
                    <motion.div
                        whileHover={{scale: 1.02}}
                        whileTap={{scale: 0.98}}
                    >
                        <Link to="/" className="flex items-center gap-3">
                            {/* Logo Icon */}
                            <div
                                className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center shadow-lg shadow-blue-500/30">
                                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                                </svg>
                            </div>
                            <span className={`text-xl font-bold tracking-tight transition-colors duration-300 ${
                                isScrolled ? 'text-slate-800' : 'text-slate-800'
                            }`}>
                                AI剧本杀
                            </span>
                        </Link>
                    </motion.div>

                    {/* Desktop Navigation */}
                    <nav className="hidden md:flex items-center gap-1">
                        {navItems.map((item) => (
                            <motion.div
                                key={item.path}
                                whileHover={{scale: 1.02}}
                                whileTap={{scale: 0.98}}
                            >
                                <Link
                                    to={item.path}
                                    className={`relative px-4 py-2 text-sm font-medium rounded-lg transition-all duration-300 ${
                                        isActive(item.path)
                                            ? 'text-blue-600'
                                            : isScrolled
                                                ? 'text-slate-600 hover:text-blue-600 hover:bg-blue-50/50'
                                                : 'text-slate-600 hover:text-blue-600 hover:bg-blue-50/50'
                                    }`}
                                >
                                    {item.label}
                                    {isActive(item.path) && (
                                        <motion.div
                                            layoutId="activeNav"
                                            className="absolute inset-0 bg-blue-100/50 rounded-lg -z-10"
                                            transition={{type: "spring", stiffness: 380, damping: 30}}
                                        />
                                    )}
                                </Link>
                            </motion.div>
                        ))}
                    </nav>

                    {/* GitHub Button */}
                    <div className="hidden md:block">
                        <motion.a
                            href="https://github.com/Zewang0217/ai-jubensha"
                            target="_blank"
                            rel="noopener noreferrer"
                            whileHover={{scale: 1.05}}
                            whileTap={{scale: 0.95}}
                            className="inline-flex items-center px-5 py-2.5 bg-gradient-to-r from-blue-500 to-blue-700 text-white text-sm font-semibold rounded-xl shadow-md shadow-blue-500/30 hover:shadow-lg hover:shadow-blue-500/40 transition-all duration-300"
                        >
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                                <path
                                    d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                            </svg>
                            GitHub
                        </motion.a>
                    </div>

                    {/* Mobile Menu Button */}
                    <motion.button
                        whileTap={{scale: 0.95}}
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                        className={`md:hidden p-2 rounded-lg transition-colors ${
                            isScrolled ? 'text-slate-700 hover:bg-slate-100' : 'text-slate-700 hover:bg-white/50'
                        }`}
                    >
                        <div className="w-6 h-6 flex flex-col justify-center items-center">
                            <motion.span
                                animate={{
                                    rotate: isMobileMenuOpen ? 45 : 0,
                                    y: isMobileMenuOpen ? 0 : -4
                                }}
                                className="absolute w-5 h-0.5 bg-current rounded-full"
                                transition={{duration: 0.2}}
                            />
                            <motion.span
                                animate={{
                                    opacity: isMobileMenuOpen ? 0 : 1
                                }}
                                className="absolute w-5 h-0.5 bg-current rounded-full"
                                transition={{duration: 0.2}}
                            />
                            <motion.span
                                animate={{
                                    rotate: isMobileMenuOpen ? -45 : 0,
                                    y: isMobileMenuOpen ? 0 : 4
                                }}
                                className="absolute w-5 h-0.5 bg-current rounded-full"
                                transition={{duration: 0.2}}
                            />
                        </div>
                    </motion.button>
                </div>
            </div>

            {/* Mobile Menu */}
            <AnimatePresence>
                {isMobileMenuOpen && (
                    <motion.div
                        initial={{opacity: 0, height: 0}}
                        animate={{opacity: 1, height: 'auto'}}
                        exit={{opacity: 0, height: 0}}
                        transition={{duration: 0.3, ease: [0.25, 0.46, 0.45, 0.94]}}
                        className="md:hidden bg-white/95 backdrop-blur-xl border-t border-blue-100/50"
                    >
                        <div className="px-4 py-4 space-y-2">
                            {navItems.map((item, index) => (
                                <motion.div
                                    key={item.path}
                                    initial={{opacity: 0, x: -20}}
                                    animate={{opacity: 1, x: 0}}
                                    transition={{delay: index * 0.1}}
                                >
                                    <Link
                                        to={item.path}
                                        onClick={() => setIsMobileMenuOpen(false)}
                                        className={`block px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                                            isActive(item.path)
                                                ? 'bg-blue-50 text-blue-600'
                                                : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                                        }`}
                                    >
                                        {item.label}
                                    </Link>
                                </motion.div>
                            ))}
                            <motion.div
                                initial={{opacity: 0, x: -20}}
                                animate={{opacity: 1, x: 0}}
                                transition={{delay: navItems.length * 0.1}}
                                className="pt-2"
                            >
                                <a
                                    href="https://github.com/Zewang0217/ai-jubensha"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    onClick={() => setIsMobileMenuOpen(false)}
                                    className="flex items-center justify-center w-full px-4 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-semibold rounded-xl shadow-lg shadow-blue-500/30"
                                >
                                    <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                                        <path
                                            d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                                    </svg>
                                    GitHub
                                </a>
                            </motion.div>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </motion.header>
    )
}

export default Header
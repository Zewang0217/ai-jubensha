import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

const Home = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [scrollY, setScrollY] = useState(0);

  useEffect(() => {
    const handleScroll = () => {
      setScrollY(window.scrollY);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#0F0F23] to-[#1A1A2E] text-gray-100 relative overflow-hidden">
      {/* CRT扫描线效果 */}
      <div className="fixed inset-0 pointer-events-none z-50 opacity-10">
        <div className="absolute inset-0 bg-[linear-gradient(to_bottom,transparent_50%,rgba(0,0,0,0.05)_50%)] bg-[size:100%_4px]"></div>
      </div>

      {/* 导航栏 */}
      <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrollY > 50 ? 'bg-[#1A1A2E]/90 backdrop-blur-md border-b border-[#7C3AED]/30' : 'bg-transparent'}`}>
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <div className="w-10 h-10 bg-gradient-to-br from-[#7C3AED] to-[#A78BFA] rounded-lg flex items-center justify-center shadow-lg shadow-[#7C3AED]/30">
              <span className="text-xl font-bold text-white drop-shadow-[0_0_10px_rgba(124,58,237,0.5)]">AI</span>
            </div>
            <h1 className="text-2xl font-bold text-white drop-shadow-[0_0_10px_rgba(124,58,237,0.5)]">剧本杀</h1>
          </div>
          
          {/* 桌面端导航 */}
          <div className="hidden md:flex space-x-8">
            <Link to="/" className="text-white font-medium hover:text-[#A78BFA] transition-colors relative group">
              首页
              <span className="absolute bottom-[-2px] left-0 w-0 h-0.5 bg-[#A78BFA] transition-all duration-300 group-hover:w-full"></span>
            </Link>
            <Link to="/games" className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors relative group">
              游戏
              <span className="absolute bottom-[-2px] left-0 w-0 h-0.5 bg-[#A78BFA] transition-all duration-300 group-hover:w-full"></span>
            </Link>
            <Link to="/settings" className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors relative group">
              设置
              <span className="absolute bottom-[-2px] left-0 w-0 h-0.5 bg-[#A78BFA] transition-all duration-300 group-hover:w-full"></span>
            </Link>
          </div>
          
          {/* 移动端菜单按钮 */}
          <div className="md:hidden flex items-center space-x-4">
            <button 
              className="text-gray-300 hover:text-white transition-colors"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                {isMenuOpen ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>
          
          <button className="bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-6 py-2 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50">
            登录
          </button>
        </div>
        
        {/* 移动端导航菜单 */}
        <AnimatePresence>
          {isMenuOpen && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.3 }}
              className="md:hidden bg-[#1A1A2E]/95 backdrop-blur-md border-t border-[#7C3AED]/30"
            >
              <div className="container mx-auto px-4 py-4 flex flex-col space-y-4">
                <Link 
                  to="/" 
                  className="text-white font-medium hover:text-[#A78BFA] transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  首页
                </Link>
                <Link 
                  to="/games" 
                  className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  游戏
                </Link>
                <Link 
                  to="/settings" 
                  className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  设置
                </Link>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </nav>

      {/* 英雄区域 */}
      <section className="container mx-auto px-4 pt-32 pb-20 md:py-40">
        <div className="max-w-4xl mx-auto text-center relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold mb-6 leading-tight">
              沉浸式<span className="text-transparent bg-clip-text bg-gradient-to-r from-[#7C3AED] to-[#F43F5E]">AI剧本杀</span>体验
            </h1>
            <p className="text-xl md:text-2xl text-gray-300 mb-12 leading-relaxed max-w-3xl mx-auto">
              与AI角色互动，探索神秘场景，收集线索，破解谜题，找出真相
            </p>
            <div className="flex flex-col sm:flex-row justify-center gap-4">
              <Link to="/games">
                <button className="bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-8 py-4 rounded-lg font-medium text-lg transition-all duration-300 transform hover:scale-105 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50">
                  开始游戏
                </button>
              </Link>
              <Link to="/games">
                <button className="bg-[#333344] hover:bg-[#444455] text-white px-8 py-4 rounded-lg font-medium text-lg transition-all duration-300 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 shadow-lg shadow-[#000000]/30">
                  加入游戏
                </button>
              </Link>
            </div>
          </motion.div>
        </div>
      </section>

      {/* 特色功能 */}
      <section className="py-20 relative z-10">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-16 text-white">
            核心特色
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300 shadow-lg shadow-[#000000]/30 hover:shadow-[#7C3AED]/10 group"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-[#7C3AED]/20 to-[#A78BFA]/20 rounded-lg flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <span className="text-3xl">🔍</span>
              </div>
              <h3 className="text-xl font-bold mb-4 text-white group-hover:text-[#A78BFA] transition-colors">沉浸式场景搜证</h3>
              <p className="text-gray-400">
                探索精心设计的场景，与环境互动，发现隐藏的线索和谜题
              </p>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300 shadow-lg shadow-[#000000]/30 hover:shadow-[#7C3AED]/10 group"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-[#7C3AED]/20 to-[#A78BFA]/20 rounded-lg flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <span className="text-3xl">🤖</span>
              </div>
              <h3 className="text-xl font-bold mb-4 text-white group-hover:text-[#A78BFA] transition-colors">智能AI角色</h3>
              <p className="text-gray-400">
                与有独立思考能力的AI角色互动，通过对话获取信息和线索
              </p>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300 shadow-lg shadow-[#000000]/30 hover:shadow-[#7C3AED]/10 group"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-[#7C3AED]/20 to-[#A78BFA]/20 rounded-lg flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <span className="text-3xl">🧩</span>
              </div>
              <h3 className="text-xl font-bold mb-4 text-white group-hover:text-[#A78BFA] transition-colors">挑战性谜题</h3>
              <p className="text-gray-400">
                破解密码、解决逻辑谜题，通过线索组合发现真相
              </p>
            </motion.div>
          </div>
        </div>
      </section>

      {/* 剧本推荐 */}
      <section className="py-20 bg-[#1A1A2E]/50 relative z-10">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center mb-12 gap-4">
            <h2 className="text-3xl md:text-4xl font-bold text-white">热门剧本</h2>
            <Link to="/games" className="text-[#A78BFA] hover:text-white transition-colors flex items-center gap-2">
              查看全部 <span>→</span>
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.1 * i }}
                className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl overflow-hidden border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300 cursor-pointer hover:shadow-lg hover:shadow-[#7C3AED]/10 group"
              >
                <div className="h-52 bg-gradient-to-br from-[#7C3AED]/20 to-[#F43F5E]/20 flex items-center justify-center group-hover:scale-105 transition-transform duration-500">
                  <span className="text-5xl">📜</span>
                </div>
                <div className="p-6">
                  <h3 className="text-xl font-bold mb-3 text-white group-hover:text-[#A78BFA] transition-colors">剧本名称 {i}</h3>
                  <p className="text-gray-400 mb-4 line-clamp-2">
                    这是一个扣人心弦的剧本杀故事，充满悬疑和惊喜
                  </p>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">4-8人</span>
                    <span className="bg-[#7C3AED]/20 text-[#A78BFA] text-sm px-3 py-1 rounded-full">
                      热门
                    </span>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* 底部 */}
      <footer className="border-t border-[#7C3AED]/30 py-12 relative z-10">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-2 mb-6 md:mb-0">
              <div className="w-10 h-10 bg-gradient-to-br from-[#7C3AED] to-[#A78BFA] rounded-lg flex items-center justify-center shadow-lg shadow-[#7C3AED]/30">
                <span className="text-xl font-bold text-white drop-shadow-[0_0_10px_rgba(124,58,237,0.5)]">AI</span>
              </div>
              <h1 className="text-2xl font-bold text-white drop-shadow-[0_0_10px_rgba(124,58,237,0.5)]">剧本杀</h1>
            </div>
            <div className="flex flex-wrap justify-center gap-6 mb-6 md:mb-0">
              <Link to="/" className="text-gray-400 hover:text-[#A78BFA] transition-colors">首页</Link>
              <Link to="/games" className="text-gray-400 hover:text-[#A78BFA] transition-colors">游戏</Link>
              <Link to="/settings" className="text-gray-400 hover:text-[#A78BFA] transition-colors">设置</Link>
              <Link to="/" className="text-gray-400 hover:text-[#A78BFA] transition-colors">关于我们</Link>
            </div>
            <div className="text-gray-500 text-sm">
              © 2026 AI剧本杀. 保留所有权利
            </div>
          </div>
        </div>
      </footer>

      {/* 背景装饰 */}
      <div className="fixed top-0 left-0 w-full h-full pointer-events-none overflow-hidden z-0">
        <motion.div 
          className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#7C3AED]/20 rounded-full blur-[100px]"
          animate={{ 
            x: [0, 30, 0],
            y: [0, 20, 0],
          }} 
          transition={{ 
            duration: 20, 
            repeat: Infinity,
            repeatType: "reverse"
          }}
        />
        <motion.div 
          className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-[#F43F5E]/20 rounded-full blur-[100px]"
          animate={{ 
            x: [0, -20, 0],
            y: [0, 30, 0],
          }} 
          transition={{ 
            duration: 15, 
            repeat: Infinity,
            repeatType: "reverse"
          }}
        />
      </div>
    </div>
  );
};

export default Home;
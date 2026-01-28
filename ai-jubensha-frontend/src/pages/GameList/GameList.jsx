import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

const GameList = () => {
  const navigate = useNavigate();
  const [gameCode, setGameCode] = useState('');
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [scrollY, setScrollY] = useState(0);
  const [games] = useState([
    {
      id: 1,
      name: '神秘 mansion 的谋杀案',
      players: 5,
      maxPlayers: 8,
      status: 'waiting',
      scriptName: '豪门恩怨',
      creator: 'Admin'
    },
    {
      id: 2,
      name: '校园怪谈',
      players: 3,
      maxPlayers: 6,
      status: 'waiting',
      scriptName: '青春迷局',
      creator: 'User123'
    },
    {
      id: 3,
      name: '办公室机密',
      players: 4,
      maxPlayers: 7,
      status: 'waiting',
      scriptName: '商业间谍',
      creator: 'GameMaster'
    }
  ]);

  useEffect(() => {
    const handleScroll = () => {
      setScrollY(window.scrollY);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleJoinGame = (e) => {
    e.preventDefault();
    if (gameCode) {
      // 这里应该调用API验证房间码
      navigate(`/game/${gameCode}`);
    }
  };

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
            <Link to="/" className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors relative group">
              首页
              <span className="absolute bottom-[-2px] left-0 w-0 h-0.5 bg-[#A78BFA] transition-all duration-300 group-hover:w-full"></span>
            </Link>
            <Link to="/games" className="text-white font-medium hover:text-[#A78BFA] transition-colors relative group">
              游戏
              <span className="absolute bottom-[-2px] left-0 w-full h-0.5 bg-[#A78BFA]"></span>
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
                  className="text-gray-300 font-medium hover:text-[#A78BFA] transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  首页
                </Link>
                <Link 
                  to="/games" 
                  className="text-white font-medium hover:text-[#A78BFA] transition-colors py-2"
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

      {/* 游戏列表区域 */}
      <div className="container mx-auto px-4 pt-32 pb-20">
        <div className="flex flex-col md:flex-row gap-8">
          {/* 左侧：游戏列表 */}
          <div className="md:w-2/3">
            <h2 className="text-3xl md:text-4xl font-bold mb-8 text-white">游戏列表</h2>
            <div className="grid grid-cols-1 gap-6">
              {games.map((game) => (
                <motion.div
                  key={game.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6 }}
                  className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300 cursor-pointer shadow-lg shadow-[#000000]/30 hover:shadow-[#7C3AED]/10 group"
                  onClick={() => navigate(`/game/${game.id}`)}
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-xl font-bold text-white group-hover:text-[#A78BFA] transition-colors">{game.name}</h3>
                    <span className={`px-3 py-1 rounded-full text-sm ${game.status === 'waiting' ? 'bg-[#22C55E]/20 text-[#22C55E] border border-[#22C55E]/30' : 'bg-[#F59E0B]/20 text-[#F59E0B] border border-[#F59E0B]/30'}`}>
                      {game.status === 'waiting' ? '等待中' : '进行中'}
                    </span>
                  </div>
                  <div className="flex flex-wrap gap-4 mb-4">
                    <div className="flex items-center gap-2">
                      <span className="text-gray-400">剧本：</span>
                      <span className="text-gray-200">{game.scriptName}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-gray-400">玩家：</span>
                      <span className="text-gray-200">{game.players}/{game.maxPlayers}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-gray-400">创建者：</span>
                      <span className="text-gray-200">{game.creator}</span>
                    </div>
                  </div>
                  <div className="flex justify-end">
                    <button className="bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-4 py-2 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50">
                      加入游戏
                    </button>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>

          {/* 右侧：创建/加入游戏 */}
          <div className="md:w-1/3">
            <div className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30">
              <h3 className="text-xl font-bold mb-6 text-white">创建游戏</h3>
              <button className="w-full bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-6 py-3 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50 mb-8">
                新建游戏
              </button>

              <h3 className="text-xl font-bold mb-6 text-white">加入游戏</h3>
              <form onSubmit={handleJoinGame} className="space-y-4">
                <div>
                  <label htmlFor="gameCode" className="block text-gray-300 mb-2">房间码</label>
                  <input
                    type="text"
                    id="gameCode"
                    value={gameCode}
                    onChange={(e) => setGameCode(e.target.value)}
                    className="w-full bg-[#333344] border border-[#7C3AED]/30 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-[#A78BFA] focus:ring-1 focus:ring-[#A78BFA] transition-all duration-300"
                    placeholder="输入房间码"
                  />
                </div>
                <button type="submit" className="w-full bg-[#333344] hover:bg-[#444455] text-white px-6 py-3 rounded-lg font-medium transition-all duration-300 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50">
                  加入游戏
                </button>
              </form>
            </div>

            {/* 剧本推荐 */}
            <div className="mt-8 bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30">
              <h3 className="text-xl font-bold mb-6 text-white">推荐剧本</h3>
              <div className="space-y-4">
                {['豪门恩怨', '青春迷局', '商业间谍', '古装悬疑'].map((script, index) => (
                  <motion.div 
                    key={index} 
                    whileHover={{ scale: 1.02 }} 
                    transition={{ duration: 0.2 }}
                    className="flex items-center justify-between p-3 bg-[#333344]/50 rounded-lg hover:bg-[#444455]/50 transition-colors cursor-pointer border border-[#7C3AED]/20 hover:border-[#A78BFA]/40"
                  >
                    <span className="text-gray-200">{script}</span>
                    <button className="text-[#A78BFA] hover:text-white transition-colors">选择</button>
                  </motion.div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

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

export default GameList;
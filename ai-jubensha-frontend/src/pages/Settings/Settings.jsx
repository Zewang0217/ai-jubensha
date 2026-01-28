import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

const Settings = () => {
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [settings, setSettings] = useState({
    sound: 80,
    music: 60,
    language: 'zh-CN',
    notifications: true,
    darkMode: true
  });

  const handleSettingChange = (key, value) => {
    setSettings(prev => ({ ...prev, [key]: value }));
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-secondary-900 to-secondary-800 text-white">
      {/* 导航栏 */}
      <nav className="bg-secondary-900/80 backdrop-blur-md border-b border-secondary-700 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <div className="w-10 h-10 bg-primary-500 rounded-lg flex items-center justify-center">
              <span className="text-xl font-bold">AI</span>
            </div>
            <h1 className="text-2xl font-bold text-primary-400">剧本杀</h1>
          </div>
          
          {/* 桌面端导航 */}
          <div className="hidden md:flex space-x-8">
            <Link to="/" className="text-secondary-300 font-medium hover:text-primary-300 transition-colors">首页</Link>
            <Link to="/games" className="text-secondary-300 font-medium hover:text-primary-300 transition-colors">游戏</Link>
            <Link to="/settings" className="text-primary-400 font-medium hover:text-primary-300 transition-colors">设置</Link>
          </div>
          
          {/* 移动端菜单按钮 */}
          <div className="md:hidden flex items-center space-x-4">
            <button 
              className="text-white" 
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
          
          <button className="bg-primary-500 hover:bg-primary-600 text-white px-6 py-2 rounded-lg font-medium transition-colors">
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
              className="md:hidden bg-secondary-800 border-t border-secondary-700"
            >
              <div className="container mx-auto px-4 py-4 flex flex-col space-y-4">
                <Link 
                  to="/" 
                  className="text-secondary-300 font-medium hover:text-primary-300 transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  首页
                </Link>
                <Link 
                  to="/games" 
                  className="text-secondary-300 font-medium hover:text-primary-300 transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  游戏
                </Link>
                <Link 
                  to="/settings" 
                  className="text-primary-400 font-medium hover:text-primary-300 transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  设置
                </Link>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </nav>

      {/* 设置页面内容 */}
      <div className="container mx-auto px-4 py-12">
        <h2 className="text-3xl font-bold mb-8 text-white">设置</h2>
        
        <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 mb-8">
          <h3 className="text-xl font-bold mb-6 text-white">音频设置</h3>
          <div className="space-y-6">
            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="text-secondary-300">音效音量</label>
                <span className="text-secondary-400">{settings.sound}%</span>
              </div>
              <input 
                type="range" 
                min="0" 
                max="100" 
                value={settings.sound}
                onChange={(e) => handleSettingChange('sound', parseInt(e.target.value))}
                className="w-full bg-secondary-700 rounded-lg h-2 appearance-none cursor-pointer"
              />
            </div>
            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="text-secondary-300">背景音乐音量</label>
                <span className="text-secondary-400">{settings.music}%</span>
              </div>
              <input 
                type="range" 
                min="0" 
                max="100" 
                value={settings.music}
                onChange={(e) => handleSettingChange('music', parseInt(e.target.value))}
                className="w-full bg-secondary-700 rounded-lg h-2 appearance-none cursor-pointer"
              />
            </div>
          </div>
        </div>

        <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 mb-8">
          <h3 className="text-xl font-bold mb-6 text-white">语言设置</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-secondary-300 mb-2">语言</label>
              <select 
                value={settings.language}
                onChange={(e) => handleSettingChange('language', e.target.value)}
                className="w-full bg-secondary-700 border border-secondary-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-primary-500 transition-colors"
              >
                <option value="zh-CN">简体中文</option>
                <option value="en-US">English</option>
              </select>
            </div>
          </div>
        </div>

        <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 mb-8">
          <h3 className="text-xl font-bold mb-6 text-white">通知设置</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <label className="text-secondary-300">游戏通知</label>
              <button 
                className={`w-12 h-6 rounded-full transition-colors ${settings.notifications ? 'bg-primary-500' : 'bg-secondary-600'}`}
                onClick={() => handleSettingChange('notifications', !settings.notifications)}
              >
                <div className={`w-5 h-5 bg-white rounded-full transform transition-transform ${settings.notifications ? 'translate-x-6' : 'translate-x-1'}`}></div>
              </button>
            </div>
          </div>
        </div>

        <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700">
          <h3 className="text-xl font-bold mb-6 text-white">外观设置</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <label className="text-secondary-300">深色模式</label>
              <button 
                className={`w-12 h-6 rounded-full transition-colors ${settings.darkMode ? 'bg-primary-500' : 'bg-secondary-600'}`}
                onClick={() => handleSettingChange('darkMode', !settings.darkMode)}
              >
                <div className={`w-5 h-5 bg-white rounded-full transform transition-transform ${settings.darkMode ? 'translate-x-6' : 'translate-x-1'}`}></div>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 底部 */}
      <footer className="bg-secondary-900 border-t border-secondary-800 py-12 mt-20">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-2 mb-6 md:mb-0">
              <div className="w-10 h-10 bg-primary-500 rounded-lg flex items-center justify-center">
                <span className="text-xl font-bold">AI</span>
              </div>
              <h1 className="text-2xl font-bold text-primary-400">剧本杀</h1>
            </div>
            <div className="flex space-x-8 mb-6 md:mb-0">
              <Link to="/" className="text-secondary-400 hover:text-primary-400 transition-colors">首页</Link>
              <Link to="/games" className="text-secondary-400 hover:text-primary-400 transition-colors">游戏</Link>
              <Link to="/settings" className="text-secondary-400 hover:text-primary-400 transition-colors">设置</Link>
              <Link to="/" className="text-secondary-400 hover:text-primary-400 transition-colors">关于我们</Link>
            </div>
            <div className="text-secondary-500 text-sm">
              © 2026 AI剧本杀. 保留所有权利
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Settings;
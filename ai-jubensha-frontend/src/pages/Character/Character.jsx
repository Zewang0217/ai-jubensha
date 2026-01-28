import React from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

const Character = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const [character, setCharacter] = React.useState({
    id: id,
    name: '侦探',
    description: '负责调查案件，找出真凶',
    background: '你是一名经验丰富的侦探，被邀请到这个神秘的 mansion 调查一起谋杀案。你需要通过收集线索、询问嫌疑人，找出真正的凶手。',
    relationships: [
      { name: '嫌疑人A', relation: '死者的妻子', description: '与死者有婚姻关系，可能存在感情纠纷' },
      { name: '嫌疑人B', relation: '死者的商业伙伴', description: '与死者有财务纠纷，可能存在利益冲突' },
      { name: '嫌疑人C', relation: '死者的秘书', description: '可能知道死者的一些秘密' },
      { name: '嫌疑人D', relation: '死者的竞争对手', description: '与死者有商业仇恨' }
    ],
    clues: [
      { id: 1, title: '红色封面的书', description: '一本红色封面的书特别突出，里面夹着一张纸条，写着"时间是解开一切的钥匙"' },
      { id: 2, title: '台灯底座的数字', description: '台灯底座上有一串数字："1492"' }
    ],
    notes: ''
  });

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
            <Link to="/settings" className="text-secondary-300 font-medium hover:text-primary-300 transition-colors">设置</Link>
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
                  className="text-secondary-300 font-medium hover:text-primary-300 transition-colors py-2"
                  onClick={() => setIsMenuOpen(false)}
                >
                  设置
                </Link>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </nav>

      {/* 角色信息页面内容 */}
      <div className="container mx-auto px-4 py-12">
        <div className="flex items-center gap-4 mb-8">
          <button 
            className="bg-secondary-700 hover:bg-secondary-600 p-2 rounded-lg transition-colors"
            onClick={() => navigate('/game/1')}
          >
            ← 返回
          </button>
          <h2 className="text-3xl font-bold text-white">角色信息</h2>
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧：角色基本信息 */}
          <div className="lg:col-span-2">
            <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 mb-8">
              <h3 className="text-2xl font-bold mb-6 text-primary-400">{character.name}</h3>
              <p className="text-secondary-300 mb-6 leading-relaxed">{character.description}</p>
              
              <h4 className="text-xl font-bold mb-4 text-white">背景故事</h4>
              <p className="text-secondary-300 mb-6 leading-relaxed">{character.background}</p>
              
              <h4 className="text-xl font-bold mb-4 text-white">人物关系</h4>
              <div className="space-y-4">
                {character.relationships.map((relationship, index) => (
                  <div key={index} className="bg-secondary-700 rounded-lg p-4">
                    <div className="flex justify-between items-start mb-2">
                      <h5 className="text-lg font-bold text-secondary-200">{relationship.name}</h5>
                      <span className="bg-primary-500/20 text-primary-400 text-sm px-3 py-1 rounded-full">
                        {relationship.relation}
                      </span>
                    </div>
                    <p className="text-secondary-300 text-sm">{relationship.description}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
          
          {/* 右侧：角色相关线索和笔记 */}
          <div className="lg:col-span-1">
            <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 mb-8">
              <h3 className="text-xl font-bold mb-6 text-white">相关线索</h3>
              <div className="space-y-4">
                {character.clues.length > 0 ? (
                  character.clues.map((clue) => (
                    <div key={clue.id} className="bg-secondary-700 rounded-lg p-4">
                      <h4 className="text-lg font-bold text-secondary-200 mb-2">{clue.title}</h4>
                      <p className="text-secondary-300 text-sm">{clue.description}</p>
                    </div>
                  ))
                ) : (
                  <div className="flex flex-col items-center justify-center py-8 text-secondary-500">
                    <span className="text-4xl mb-4">🔍</span>
                    <p>暂无相关线索</p>
                  </div>
                )}
              </div>
            </div>
            
            <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700">
              <h3 className="text-xl font-bold mb-6 text-white">笔记</h3>
              <textarea
                value={character.notes}
                onChange={(e) => setCharacter(prev => ({ ...prev, notes: e.target.value }))}
                placeholder="添加笔记..."
                className="w-full bg-secondary-700 border border-secondary-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-primary-500 transition-colors min-h-[200px]"
              />
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

export default Character;
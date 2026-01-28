import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const Scene = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [scene, setScene] = useState({
    id: id,
    name: '书房',
    description: '你走进书房，房间里弥漫着旧书的气味。书架上摆满了各种书籍，书桌上杂乱地放着笔记本电脑、台灯和一些文件。角落里有一个保险箱，墙上挂着一幅画，窗户紧闭着。',
    imageUrl: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mysterious%20study%20room%20with%20bookshelves%20desk%20safe%20and%20painting%20dark%20atmosphere&image_size=landscape_16_9',
    duration: 30,
    elements: [
      { id: 1, name: '书架', position: 'left', description: '摆满了各种书籍的书架' },
      { id: 2, name: '书桌', position: 'center', description: '杂乱地放着笔记本电脑、台灯和一些文件' },
      { id: 3, name: '保险箱', position: 'right', description: '角落里的保险箱，需要密码才能打开' },
      { id: 4, name: '墙上的画', position: 'top', description: '挂在墙上的一幅画' },
      { id: 5, name: '窗户', position: 'right', description: '紧闭着的窗户' }
    ]
  });
  const [clues, setClues] = useState([]);
  const [selectedElement, setSelectedElement] = useState(null);
  const [timeLeft, setTimeLeft] = useState(scene.duration * 60);
  const [showClueModal, setShowClueModal] = useState(false);
  const [newClue, setNewClue] = useState(null);

  // 模拟获取线索
  const getClueFromElement = (elementId) => {
    const cluesMap = {
      1: { id: 1, title: '红色封面的书', description: '一本红色封面的书特别突出，里面夹着一张纸条，写着"时间是解开一切的钥匙"', importance: 3, type: 'document' },
      2: { id: 2, title: '台灯底座的数字', description: '台灯底座上有一串数字："1492"', importance: 4, type: 'physical' },
      3: { id: 3, title: '保险箱', description: '保险箱需要6位数字密码才能打开', importance: 5, type: 'physical' },
      4: { id: 4, title: '墙上的画', description: '画框看起来有些松动，取下画后发现后面有一个小暗格', importance: 4, type: 'physical' },
      5: { id: 5, title: '窗户', description: '窗户从内部反锁，窗外是花园', importance: 2, type: 'physical' }
    };
    return cluesMap[elementId];
  };

  // 处理元素点击
  const handleElementClick = (element) => {
    setSelectedElement(element);
    const clue = getClueFromElement(element.id);
    if (clue && !clues.some(c => c.id === clue.id)) {
      setNewClue(clue);
      setShowClueModal(true);
    }
  };

  // 添加线索到收集列表
  const addClue = () => {
    if (newClue) {
      setClues([...clues, newClue]);
      setShowClueModal(false);
      setNewClue(null);
    }
  };

  // 计时器
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          // 时间到，返回游戏房间
          navigate('/game/1');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [navigate]);

  // 格式化时间
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen bg-secondary-900 text-white">
      {/* 顶部信息栏 */}
      <div className="bg-secondary-800 border-b border-secondary-700 py-4 px-6 flex justify-between items-center sticky top-0 z-50">
        <div className="flex items-center gap-4">
          <button className="bg-secondary-700 hover:bg-secondary-600 p-2 rounded-lg transition-colors" onClick={() => navigate('/game/1')}>
            ← 返回
          </button>
          <h1 className="text-2xl font-bold text-primary-400">{scene.name}</h1>
        </div>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <span className="text-secondary-400">剩余时间：</span>
            <span className="text-xl font-bold text-accent-400">{formatTime(timeLeft)}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-secondary-400">线索：</span>
            <span className="text-xl font-bold text-primary-400">{clues.length}/5</span>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧：场景展示 */}
          <div className="lg:col-span-2">
            <div className="bg-secondary-800 rounded-xl overflow-hidden border border-secondary-700">
              {/* 场景图片 */}
              <div className="relative h-[500px]">
                <img 
                  src={scene.imageUrl} 
                  alt={scene.name} 
                  className="w-full h-full object-cover"
                />
                {/* 可交互元素标记 */}
                {scene.elements.map((element) => (
                  <motion.div
                    key={element.id}
                    className={`absolute cursor-pointer ${element.position === 'left' ? 'left-10 top-1/2 transform -translate-y-1/2' : 
                      element.position === 'center' ? 'left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2' : 
                      element.position === 'right' ? 'right-10 top-1/2 transform -translate-y-1/2' : 
                      'left-1/2 top-10 transform -translate-x-1/2'}`}
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={() => handleElementClick(element)}
                  >
                    <div className="bg-primary-500/80 backdrop-blur-sm text-white px-3 py-2 rounded-lg text-sm font-medium">
                      {element.name}
                    </div>
                  </motion.div>
                ))}
              </div>
              {/* 场景描述 */}
              <div className="p-6">
                <h2 className="text-xl font-bold mb-4 text-white">场景描述</h2>
                <p className="text-secondary-300 leading-relaxed">{scene.description}</p>
              </div>
              {/* 选中元素信息 */}
              {selectedElement && (
                <div className="p-6 border-t border-secondary-700">
                  <h3 className="text-lg font-bold mb-2 text-primary-400">{selectedElement.name}</h3>
                  <p className="text-secondary-300">{selectedElement.description}</p>
                </div>
              )}
            </div>
          </div>

          {/* 右侧：线索栏 */}
          <div className="lg:col-span-1">
            <div className="bg-secondary-800 rounded-xl border border-secondary-700 h-full flex flex-col">
              <div className="p-6 border-b border-secondary-700">
                <h2 className="text-xl font-bold text-white">已收集线索</h2>
              </div>
              <div className="p-4 flex-1 overflow-y-auto">
                {clues.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-full text-secondary-500">
                    <span className="text-4xl mb-4">🔍</span>
                    <p>还没有收集到线索</p>
                    <p className="text-sm mt-2">点击场景中的元素开始搜索</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {clues.map((clue) => (
                      <motion.div
                        key={clue.id}
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.3 }}
                        className="bg-secondary-700 rounded-lg p-4 border border-secondary-600 hover:border-primary-500 transition-colors"
                      >
                        <div className="flex justify-between items-start mb-2">
                          <h3 className="text-lg font-bold text-white">{clue.title}</h3>
                          <span className={`px-2 py-1 rounded text-xs ${clue.importance >= 4 ? 'bg-red-500/20 text-red-400' : clue.importance >= 3 ? 'bg-yellow-500/20 text-yellow-400' : 'bg-green-500/20 text-green-400'}`}>
                            {clue.importance >= 4 ? '核心线索' : clue.importance >= 3 ? '重要线索' : '辅助线索'}
                          </span>
                        </div>
                        <p className="text-secondary-300 text-sm mb-2">{clue.description}</p>
                        <div className="flex justify-end">
                          <span className="text-xs text-secondary-400">类型：{clue.type === 'physical' ? '实物' : clue.type === 'witness' ? '证人' : '文档'}</span>
                        </div>
                      </motion.div>
                    ))}
                  </div>
                )}
              </div>
              <div className="p-6 border-t border-secondary-700">
                <button className="w-full bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg font-medium transition-colors">
                  分析线索
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 线索发现弹窗 */}
      {showClueModal && newClue && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50"
        >
          <div className="bg-secondary-800 rounded-xl p-6 border border-primary-500 max-w-md w-full mx-4">
            <h3 className="text-xl font-bold mb-4 text-primary-400">发现线索！</h3>
            <div className="mb-6">
              <h4 className="text-lg font-bold text-white mb-2">{newClue.title}</h4>
              <p className="text-secondary-300 mb-4">{newClue.description}</p>
              <div className="flex justify-between items-center">
                <span className={`px-2 py-1 rounded text-xs ${newClue.importance >= 4 ? 'bg-red-500/20 text-red-400' : newClue.importance >= 3 ? 'bg-yellow-500/20 text-yellow-400' : 'bg-green-500/20 text-green-400'}`}>
                  {newClue.importance >= 4 ? '核心线索' : newClue.importance >= 3 ? '重要线索' : '辅助线索'}
                </span>
                <span className="text-xs text-secondary-400">类型：{newClue.type === 'physical' ? '实物' : newClue.type === 'witness' ? '证人' : '文档'}</span>
              </div>
            </div>
            <div className="flex gap-4 justify-end">
              <button 
                className="bg-secondary-700 hover:bg-secondary-600 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                onClick={() => {
                  setShowClueModal(false);
                  setNewClue(null);
                }}
              >
                忽略
              </button>
              <button 
                className="bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                onClick={addClue}
              >
                收集线索
              </button>
            </div>
          </div>
        </motion.div>
      )}
    </div>
  );
};

export default Scene;
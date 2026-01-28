import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const Clue = () => {
  const navigate = useNavigate();
  const [clues, setClues] = useState([
    {
      id: 1,
      title: '红色封面的书',
      description: '一本红色封面的书特别突出，里面夹着一张纸条，写着"时间是解开一切的钥匙"',
      importance: 3,
      type: 'document',
      scene: '书房',
      tags: ['时间', '钥匙'],
      notes: '',
      relatedClues: [2, 3]
    },
    {
      id: 2,
      title: '台灯底座的数字',
      description: '台灯底座上有一串数字："1492"',
      importance: 4,
      type: 'physical',
      scene: '书房',
      tags: ['数字', '密码'],
      notes: '',
      relatedClues: [1, 3]
    },
    {
      id: 3,
      title: '保险箱',
      description: '保险箱需要6位数字密码才能打开',
      importance: 5,
      type: 'physical',
      scene: '书房',
      tags: ['保险箱', '密码'],
      notes: '',
      relatedClues: [1, 2]
    },
    {
      id: 4,
      title: '墙上的画',
      description: '画框看起来有些松动，取下画后发现后面有一个小暗格',
      importance: 4,
      type: 'physical',
      scene: '书房',
      tags: ['画', '暗格'],
      notes: '',
      relatedClues: []
    },
    {
      id: 5,
      title: '窗户',
      description: '窗户从内部反锁，窗外是花园',
      importance: 2,
      type: 'physical',
      scene: '书房',
      tags: ['窗户', '花园'],
      notes: '',
      relatedClues: []
    },
    {
      id: 6,
      title: '客厅的日历',
      description: '日历上标记着"结婚纪念日：5月12日"',
      importance: 3,
      type: 'document',
      scene: '客厅',
      tags: ['日期', '纪念日'],
      notes: '',
      relatedClues: [7]
    },
    {
      id: 7,
      title: '卧室的照片',
      description: '照片背面写着"我们的第一个孩子出生于2015年"',
      importance: 3,
      type: 'document',
      scene: '卧室',
      tags: ['孩子', '出生年份'],
      notes: '',
      relatedClues: [6]
    }
  ]);
  const [selectedClue, setSelectedClue] = useState(null);
  const [filterType, setFilterType] = useState('all');
  const [filterImportance, setFilterImportance] = useState('all');
  const [showAnalysis, setShowAnalysis] = useState(false);

  // 过滤线索
  const filteredClues = clues.filter(clue => {
    const typeMatch = filterType === 'all' || clue.type === filterType;
    const importanceMatch = filterImportance === 'all' || clue.importance >= parseInt(filterImportance);
    return typeMatch && importanceMatch;
  });

  // 处理线索点击
  const handleClueClick = (clue) => {
    setSelectedClue(clue);
  };

  // 更新线索笔记
  const updateClueNote = (id, notes) => {
    setClues(clues.map(clue => 
      clue.id === id ? { ...clue, notes } : clue
    ));
  };

  // 分析线索关联
  const analyzeClues = () => {
    // 这里可以实现更复杂的线索关联分析逻辑
    setShowAnalysis(true);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-secondary-900 to-secondary-800 text-white">
      {/* 顶部信息栏 */}
      <div className="bg-secondary-800 border-b border-secondary-700 py-4 px-6 flex justify-between items-center sticky top-0 z-50">
        <div className="flex items-center gap-4">
          <button className="bg-secondary-700 hover:bg-secondary-600 p-2 rounded-lg transition-colors" onClick={() => navigate('/game/1')}>
            ← 返回
          </button>
          <h1 className="text-2xl font-bold text-primary-400">线索管理</h1>
        </div>
        <div className="flex items-center gap-6">
          <span className="text-secondary-300 font-medium">已收集线索：{clues.length}</span>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧：线索列表和筛选 */}
          <div className="lg:col-span-1">
            <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-white">线索列表</h2>
                <button 
                  className="bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg font-medium transition-colors text-sm"
                  onClick={analyzeClues}
                >
                  分析线索
                </button>
              </div>

              {/* 筛选器 */}
              <div className="mb-6 space-y-4">
                <div>
                  <label className="block text-secondary-300 mb-2 text-sm">线索类型</label>
                  <select 
                    value={filterType}
                    onChange={(e) => setFilterType(e.target.value)}
                    className="w-full bg-secondary-700 border border-secondary-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-primary-500 transition-colors"
                  >
                    <option value="all">全部类型</option>
                    <option value="physical">实物</option>
                    <option value="witness">证人</option>
                    <option value="document">文档</option>
                  </select>
                </div>
                <div>
                  <label className="block text-secondary-300 mb-2 text-sm">重要程度</label>
                  <select 
                    value={filterImportance}
                    onChange={(e) => setFilterImportance(e.target.value)}
                    className="w-full bg-secondary-700 border border-secondary-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-primary-500 transition-colors"
                  >
                    <option value="all">全部</option>
                    <option value="3">重要及以上</option>
                    <option value="4">非常重要及以上</option>
                    <option value="5">核心线索</option>
                  </select>
                </div>
              </div>

              {/* 线索列表 */}
              <div className="space-y-3 max-h-[600px] overflow-y-auto">
                {filteredClues.map((clue) => (
                  <motion.div
                    key={clue.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3 }}
                    className={`p-4 rounded-lg border cursor-pointer transition-colors ${selectedClue?.id === clue.id ? 'bg-primary-500/20 border-primary-500' : 'bg-secondary-700 border-secondary-600 hover:border-primary-500'}`}
                    onClick={() => handleClueClick(clue)}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="text-lg font-bold text-white">{clue.title}</h3>
                      <span className={`px-2 py-1 rounded text-xs ${clue.importance >= 4 ? 'bg-red-500/20 text-red-400' : clue.importance >= 3 ? 'bg-yellow-500/20 text-yellow-400' : 'bg-green-500/20 text-green-400'}`}>
                        {clue.importance >= 4 ? '重要' : clue.importance >= 3 ? '一般' : '次要'}
                      </span>
                    </div>
                    <p className="text-secondary-300 text-sm mb-2 line-clamp-2">{clue.description}</p>
                    <div className="flex justify-between items-center">
                      <span className="text-xs text-secondary-400">{clue.scene}</span>
                      <span className="text-xs text-secondary-400">{clue.type === 'physical' ? '实物' : clue.type === 'witness' ? '证人' : '文档'}</span>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>
          </div>

          {/* 右侧：线索详情和分析 */}
          <div className="lg:col-span-2">
            {selectedClue ? (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}
                className="bg-secondary-800 rounded-xl p-6 border border-secondary-700"
              >
                <h2 className="text-2xl font-bold mb-6 text-white">线索详情</h2>
                
                {/* 线索基本信息 */}
                <div className="mb-8">
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-xl font-bold text-primary-400">{selectedClue.title}</h3>
                    <span className={`px-3 py-1 rounded-full text-sm ${selectedClue.importance >= 4 ? 'bg-red-500/20 text-red-400' : selectedClue.importance >= 3 ? 'bg-yellow-500/20 text-yellow-400' : 'bg-green-500/20 text-green-400'}`}>
                      {selectedClue.importance >= 4 ? '重要线索' : selectedClue.importance >= 3 ? '一般线索' : '次要线索'}
                    </span>
                  </div>
                  <p className="text-secondary-300 mb-4 leading-relaxed">{selectedClue.description}</p>
                  <div className="flex flex-wrap gap-4">
                    <div className="flex items-center gap-2">
                      <span className="text-secondary-400">场景：</span>
                      <span className="text-secondary-200">{selectedClue.scene}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-secondary-400">类型：</span>
                      <span className="text-secondary-200">{selectedClue.type === 'physical' ? '实物' : selectedClue.type === 'witness' ? '证人' : '文档'}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-secondary-400">重要程度：</span>
                      <span className="text-secondary-200">{selectedClue.importance}/5</span>
                    </div>
                  </div>
                </div>

                {/* 标签 */}
                <div className="mb-8">
                  <h4 className="text-lg font-bold mb-3 text-white">标签</h4>
                  <div className="flex flex-wrap gap-2">
                    {selectedClue.tags.map((tag, index) => (
                      <span key={index} className="bg-secondary-700 text-secondary-300 px-3 py-1 rounded-full text-sm">
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>

                {/* 相关线索 */}
                {selectedClue.relatedClues.length > 0 && (
                  <div className="mb-8">
                    <h4 className="text-lg font-bold mb-3 text-white">相关线索</h4>
                    <div className="space-y-2">
                      {selectedClue.relatedClues.map((clueId) => {
                        const relatedClue = clues.find(c => c.id === clueId);
                        return relatedClue ? (
                          <div key={clueId} className="flex items-center gap-3 p-3 bg-secondary-700 rounded-lg cursor-pointer hover:bg-secondary-600 transition-colors">
                            <div className="w-2 h-2 rounded-full bg-primary-500"></div>
                            <span className="text-secondary-200" onClick={() => handleClueClick(relatedClue)}>
                              {relatedClue.title}
                            </span>
                          </div>
                        ) : null;
                      })}
                    </div>
                  </div>
                )}

                {/* 笔记 */}
                <div className="mb-8">
                  <h4 className="text-lg font-bold mb-3 text-white">笔记</h4>
                  <textarea
                    value={selectedClue.notes}
                    onChange={(e) => {
                      const updatedClues = clues.map(clue => 
                        clue.id === selectedClue.id ? { ...clue, notes: e.target.value } : clue
                      );
                      setClues(updatedClues);
                      setSelectedClue({ ...selectedClue, notes: e.target.value });
                    }}
                    placeholder="添加笔记..."
                    className="w-full bg-secondary-700 border border-secondary-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-primary-500 transition-colors min-h-[100px]"
                  />
                </div>
              </motion.div>
            ) : (
              <div className="bg-secondary-800 rounded-xl p-6 border border-secondary-700 flex flex-col items-center justify-center h-[500px]">
                <span className="text-4xl mb-4">🔍</span>
                <h3 className="text-xl font-bold mb-2 text-secondary-300">请选择一个线索</h3>
                <p className="text-secondary-400 text-center">从左侧列表中选择一个线索查看详细信息</p>
              </div>
            )}

            {/* 线索分析结果 */}
            {showAnalysis && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}
                className="mt-8 bg-secondary-800 rounded-xl p-6 border border-secondary-700"
              >
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-xl font-bold text-white">线索分析</h2>
                  <button 
                    className="text-secondary-400 hover:text-white transition-colors"
                    onClick={() => setShowAnalysis(false)}
                  >
                    ×
                  </button>
                </div>
                <div className="space-y-6">
                  <div>
                    <h3 className="text-lg font-bold mb-3 text-primary-400">线索关联分析</h3>
                    <div className="bg-secondary-700 rounded-lg p-4">
                      <p className="text-secondary-300 mb-4">根据已收集的线索，我们发现以下关联：</p>
                      <ul className="space-y-2 text-secondary-200 list-disc pl-5">
                        <li>"红色封面的书"中提到的"时间是解开一切的钥匙"可能与"台灯底座的数字"和"保险箱"相关</li>
                        <li>"台灯底座的数字"1492可能是保险箱密码的一部分</li>
                        <li>"墙上的画"后面的暗格可能藏有重要物品</li>
                        <li>"客厅的日历"和"卧室的照片"中的日期信息可能与保险箱密码相关</li>
                      </ul>
                    </div>
                  </div>
                  <div>
                    <h3 className="text-lg font-bold mb-3 text-primary-400">重要线索提示</h3>
                    <div className="bg-secondary-700 rounded-lg p-4">
                      <p className="text-secondary-300 mb-4">基于线索重要性和关联分析，以下线索可能对破案至关重要：</p>
                      <div className="space-y-2">
                        {clues.filter(c => c.importance >= 4).map((clue) => (
                          <div key={clue.id} className="flex items-center gap-3 p-3 bg-secondary-600 rounded-lg">
                            <div className="w-2 h-2 rounded-full bg-red-500"></div>
                            <span className="text-secondary-200">{clue.title}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                  <div>
                    <h3 className="text-lg font-bold mb-3 text-primary-400">密码提示</h3>
                    <div className="bg-secondary-700 rounded-lg p-4">
                      <p className="text-secondary-300">结合"红色封面的书"中的提示和"台灯底座的数字"，保险箱密码可能与时间相关。尝试使用1492作为基础，结合其他日期信息进行组合。</p>
                    </div>
                  </div>
                </div>
              </motion.div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Clue;
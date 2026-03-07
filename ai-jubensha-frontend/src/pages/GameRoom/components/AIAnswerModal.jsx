import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, User, Bot, ScrollText } from 'lucide-react';

/**
 * AI答题展示弹窗组件
 * 用于观察者模式展示每个AI玩家的答题情况
 * 
 * @param {Object} props
 * @param {boolean} props.isOpen - 是否显示弹窗
 * @param {Function} props.onClose - 关闭弹窗回调
 * @param {Array} props.answers - 玩家答案列表
 * @param {string} props.currentPhase - 当前阶段
 */
// eslint-disable-next-line no-unused-vars
function AIAnswerModal({ isOpen, onClose, answers = [], currentPhase }) {
  const [selectedPlayer, setSelectedPlayer] = useState(null);

  // 当新答案到达时，自动选中最新的
  useEffect(() => {
    if (answers.length > 0 && !selectedPlayer) {
      setSelectedPlayer(answers[0]);
    }
  }, [answers, selectedPlayer]);

  // 按提交时间排序
  const sortedAnswers = [...answers].sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4"
        onClick={onClose}
      >
        <motion.div
          initial={{ scale: 0.9, opacity: 0, y: 20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.9, opacity: 0, y: 20 }}
          transition={{ type: 'spring', damping: 25, stiffness: 300 }}
          className="relative w-full max-w-5xl max-h-[85vh] bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 rounded-2xl shadow-2xl border border-slate-600/30 overflow-hidden"
          onClick={(e) => e.stopPropagation()}
        >
          {/* 背景装饰 */}
          <div className="absolute inset-0 overflow-hidden pointer-events-none">
            <div className="absolute top-0 left-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2" />
            <div className="absolute bottom-0 right-0 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl translate-x-1/2 translate-y-1/2" />
          </div>

          {/* 头部 */}
          <div className="relative flex items-center justify-between px-6 py-4 border-b border-slate-700/50 bg-slate-800/50">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg">
                <ScrollText className="w-5 h-5 text-white" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">AI玩家答题情况</h2>
                <p className="text-sm text-slate-400">
                  已提交 {answers.length} 位玩家的答案
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg hover:bg-slate-700/50 transition-colors group"
            >
              <X className="w-5 h-5 text-slate-400 group-hover:text-white transition-colors" />
            </button>
          </div>

          {/* 内容区域 */}
          <div className="relative flex h-[calc(85vh-140px)]">
            {/* 左侧玩家列表 */}
            <div className="w-72 border-r border-slate-700/50 bg-slate-900/30 overflow-y-auto">
              <div className="p-4">
                <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
                  玩家列表
                </h3>
                <div className="space-y-2">
                  {sortedAnswers.map((answer, index) => (
                    <button
                      key={answer.playerId}
                      onClick={() => setSelectedPlayer(answer)}
                      className={`w-full flex items-center gap-3 p-3 rounded-xl transition-all duration-200 text-left ${
                        selectedPlayer?.playerId === answer.playerId
                          ? 'bg-gradient-to-r from-blue-600/30 to-purple-600/30 border border-blue-500/30'
                          : 'hover:bg-slate-800/50 border border-transparent'
                      }`}
                    >
                      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-br from-slate-700 to-slate-600 flex items-center justify-center text-sm font-bold text-white">
                        {index + 1}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="font-medium text-white truncate">
                            {answer.playerName || `玩家${answer.playerId}`}
                          </span>
                          {answer.isAI ? (
                            <Bot className="w-3.5 h-3.5 text-blue-400" />
                          ) : (
                            <User className="w-3.5 h-3.5 text-green-400" />
                          )}
                        </div>
                        <p className="text-xs text-slate-500 truncate">
                          {answer.isAI ? 'AI玩家' : '真人玩家'}
                        </p>
                      </div>
                      <div className="flex-shrink-0 w-2 h-2 rounded-full bg-green-500" />
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* 右侧答案详情 */}
            <div className="flex-1 overflow-y-auto p-6">
              {selectedPlayer ? (
                <motion.div
                  key={selectedPlayer.playerId}
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.3 }}
                  className="space-y-4"
                >
                  {/* 玩家信息卡片 */}
                  <div className="flex items-center gap-4 p-4 bg-slate-800/50 rounded-xl border border-slate-700/50">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                      {selectedPlayer.isAI ? (
                        <Bot className="w-6 h-6 text-white" />
                      ) : (
                        <User className="w-6 h-6 text-white" />
                      )}
                    </div>
                    <div>
                      <h3 className="text-lg font-bold text-white">
                        {selectedPlayer.playerName || `玩家${selectedPlayer.playerId}`}
                      </h3>
                      <p className="text-sm text-slate-400">
                        {selectedPlayer.isAI ? 'AI玩家' : '真人玩家'} · 答案详情
                      </p>
                    </div>
                  </div>

                  {/* 答案内容 */}
                  <div className="p-6 bg-slate-800/30 rounded-xl border border-slate-700/30">
                    <h4 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4">
                      案件分析答案
                    </h4>
                    <div className="prose prose-invert max-w-none">
                      <div className="text-slate-200 leading-relaxed whitespace-pre-wrap">
                        {selectedPlayer.answer || '暂无答案内容'}
                      </div>
                    </div>
                  </div>

                  {/* 提交时间 */}
                  {selectedPlayer.timestamp && (
                    <div className="text-xs text-slate-500 text-right">
                      提交时间: {new Date(selectedPlayer.timestamp).toLocaleString('zh-CN')}
                    </div>
                  )}
                </motion.div>
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-slate-500">
                  <ScrollText className="w-16 h-16 mb-4 opacity-30" />
                  <p>请选择左侧玩家查看答案</p>
                </div>
              )}
            </div>
          </div>

          {/* 底部 */}
          <div className="relative flex items-center justify-between px-6 py-3 border-t border-slate-700/50 bg-slate-800/30">
            <p className="text-xs text-slate-500">
              观察者模式 · 实时查看AI答题进度
            </p>
            <button
              onClick={onClose}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white text-sm font-medium rounded-lg transition-colors"
            >
              关闭
            </button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}

export default AIAnswerModal;

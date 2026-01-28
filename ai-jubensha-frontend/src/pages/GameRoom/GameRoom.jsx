import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useWebSocket } from '../../context/WebSocketContext';

const GameRoom = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isConnected, connect, sendMessage, onMessage, offMessage } = useWebSocket();
  const [game, setGame] = useState({
    id: id,
    name: '神秘 mansion 的谋杀案',
    scriptName: '豪门恩怨',
    status: 'waiting',
    maxPlayers: 8,
    creator: 'Admin',
    roomCode: 'ABC123',
    scenes: [
      { id: 1, name: '客厅', orderIndex: 1 },
      { id: 2, name: '书房', orderIndex: 2 },
      { id: 3, name: '卧室', orderIndex: 3 },
      { id: 4, name: '花园', orderIndex: 4 }
    ]
  });
  const [players, setPlayers] = useState([
    { id: 1, name: 'Admin', role: '侦探', status: 'online' },
    { id: 2, name: 'User123', role: '嫌疑人A', status: 'online' },
    { id: 3, name: 'GameMaster', role: '嫌疑人B', status: 'online' },
    { id: 4, name: 'Player4', role: '', status: 'online' }
  ]);
  const [characters, setCharacters] = useState([
    { id: 1, name: '侦探', description: '负责调查案件，找出真凶', selected: true },
    { id: 2, name: '嫌疑人A', description: '死者的妻子，有动机杀害死者', selected: true },
    { id: 3, name: '嫌疑人B', description: '死者的商业伙伴，有财务纠纷', selected: true },
    { id: 4, name: '嫌疑人C', description: '死者的秘书，可能知道秘密', selected: false },
    { id: 5, name: '嫌疑人D', description: '死者的竞争对手，有商业仇恨', selected: false },
    { id: 6, name: '管家', description: '在死者家工作多年，了解家庭情况', selected: false },
    { id: 7, name: '园丁', description: '负责花园维护，可能看到可疑情况', selected: false },
    { id: 8, name: '厨师', description: '负责饮食，可能在食物中下毒', selected: false }
  ]);
  const [messages, setMessages] = useState([
    { id: 1, sender: '系统', content: '欢迎来到游戏房间！', time: '14:30' },
    { id: 2, sender: 'Admin', content: '大家好，准备开始游戏了！', time: '14:31' },
    { id: 3, sender: 'User123', content: '我已经选择了嫌疑人A的角色', time: '14:32' }
  ]);
  const [newMessage, setNewMessage] = useState('');
  const [showCharacterSelect, setShowCharacterSelect] = useState(false);

  // 建立WebSocket连接
  useEffect(() => {
    // 连接到WebSocket服务器
    const wsUrl = `ws://localhost:8080/ws?gameId=${id}`;
    connect(wsUrl);

    // 清理函数
    return () => {
      // 这里可以添加清理逻辑
    };
  }, [id, connect]);

  // 注册WebSocket消息处理器
  useEffect(() => {
    // 处理聊天消息
    const handleChatMessage = (data) => {
      setMessages(prev => [...prev, {
        id: prev.length + 1,
        sender: data.sender,
        content: data.content,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }]);
    };

    // 处理玩家加入/离开
    const handlePlayerUpdate = (data) => {
      setPlayers(data.players);
    };

    // 处理游戏状态更新
    const handleGameStatusUpdate = (data) => {
      setGame(prev => ({ ...prev, status: data.status }));
    };

    // 注册消息处理器
    onMessage('chat', handleChatMessage);
    onMessage('playerUpdate', handlePlayerUpdate);
    onMessage('gameStatusUpdate', handleGameStatusUpdate);

    // 清理函数
    return () => {
      offMessage('chat', handleChatMessage);
      offMessage('playerUpdate', handlePlayerUpdate);
      offMessage('gameStatusUpdate', handleGameStatusUpdate);
    };
  }, [onMessage, offMessage]);

  // 处理角色选择
  const handleCharacterSelect = (character) => {
    // 这里应该发送角色选择请求到服务器
    console.log('选择角色', character.name);
    setCharacters(characters.map(c => 
      c.id === character.id ? { ...c, selected: true } : c
    ));
    setShowCharacterSelect(false);
  };

  // 发送消息
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (newMessage.trim()) {
      // 使用WebSocket发送消息
      sendMessage({
        type: 'chat',
        data: {
          sender: '我',
          content: newMessage.trim(),
          gameId: id
        }
      });

      // 本地添加消息（乐观更新）
      const message = {
        id: messages.length + 1,
        sender: '我',
        content: newMessage.trim(),
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      };
      setMessages([...messages, message]);
      setNewMessage('');
    }
  };

  // 开始游戏
  const startGame = () => {
    // 这里应该发送开始游戏请求到服务器
    console.log('开始游戏');
    // 跳转到第一个场景
    navigate(`/scene/${game.scenes[0].id}`);
  };

  // 离开游戏
  const leaveGame = () => {
    // 这里应该发送离开游戏请求到服务器
    console.log('离开游戏');
    navigate('/games');
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#0F0F23] to-[#1A1A2E] text-gray-100 relative overflow-hidden">
      {/* CRT扫描线效果 */}
      <div className="fixed inset-0 pointer-events-none z-50 opacity-10">
        <div className="absolute inset-0 bg-[linear-gradient(to_bottom,transparent_50%,rgba(0,0,0,0.05)_50%)] bg-[size:100%_4px]"></div>
      </div>

      {/* 顶部信息栏 */}
      <div className="bg-[#1A1A2E]/90 backdrop-blur-md border-b border-[#7C3AED]/30 py-4 px-6 flex justify-between items-center sticky top-0 z-50">
        <div className="flex items-center gap-4">
          <button className="bg-[#333344] hover:bg-[#444455] p-2 rounded-lg transition-all duration-300 border border-[#7C3AED]/30 hover:border-[#A78BFA]/50" onClick={leaveGame}>
            ← 返回
          </button>
          <h1 className="text-2xl font-bold text-white drop-shadow-[0_0_10px_rgba(124,58,237,0.5)]">{game.name}</h1>
        </div>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <span className="text-gray-400">房间码：</span>
            <span className="text-white font-mono drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">{game.roomCode}</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-gray-400">状态：</span>
            <span className={`${game.status === 'waiting' ? 'text-[#22C55E] drop-shadow-[0_0_5px_rgba(34,197,94,0.5)]' : 'text-[#F59E0B] drop-shadow-[0_0_5px_rgba(245,158,11,0.5)]'}`}>
              {game.status === 'waiting' ? '等待中' : '进行中'}
            </span>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧：游戏信息和场景 */}
          <div className="lg:col-span-1">
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30 mb-8"
            >
              <h2 className="text-xl font-bold mb-6 text-white drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">游戏信息</h2>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-400">剧本：</span>
                  <span className="text-gray-200">{game.scriptName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">玩家：</span>
                  <span className="text-gray-200">{players.length}/{game.maxPlayers}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">创建者：</span>
                  <span className="text-gray-200">{game.creator}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">房间码：</span>
                  <span className="text-white font-mono drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">{game.roomCode}</span>
                </div>
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30"
            >
              <h2 className="text-xl font-bold mb-6 text-white drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">游戏场景</h2>
              <div className="space-y-3">
                {game.scenes.map((scene) => (
                  <motion.div 
                    key={scene.id}
                    whileHover={{ scale: 1.02 }}
                    transition={{ duration: 0.2 }}
                    className="flex items-center gap-3 p-4 bg-[#333344]/50 rounded-lg border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300"
                  >
                    <div className="w-10 h-10 bg-gradient-to-br from-[#7C3AED]/20 to-[#A78BFA]/20 rounded-full flex items-center justify-center text-[#A78BFA] font-medium drop-shadow-[0_0_5px_rgba(167,139,250,0.3)]">
                      {scene.orderIndex}
                    </div>
                    <span className="text-white">{scene.name}</span>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          </div>

          {/* 中间：玩家列表和角色选择 */}
          <div className="lg:col-span-1">
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30 mb-8"
            >
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-white drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">玩家列表</h2>
                <motion.button 
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-4 py-2 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50 text-sm"
                  onClick={() => setShowCharacterSelect(true)}
                >
                  选择角色
                </motion.button>
              </div>
              <div className="space-y-4">
                {players.map((player) => (
                  <motion.div 
                    key={player.id}
                    whileHover={{ scale: 1.02 }}
                    transition={{ duration: 0.2 }}
                    className="flex items-center justify-between p-4 bg-[#333344]/50 rounded-lg border border-[#7C3AED]/30 hover:border-[#A78BFA]/50 transition-all duration-300"
                  >
                    <div className="flex items-center gap-3">
                      <div className={`w-3 h-3 rounded-full ${player.status === 'online' ? 'bg-[#22C55E] drop-shadow-[0_0_5px_rgba(34,197,94,0.5)]' : 'bg-gray-500'}`}></div>
                      <span className="text-white font-medium">{player.name}</span>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-sm ${player.role ? 'bg-[#7C3AED]/20 text-[#A78BFA] border border-[#7C3AED]/30' : 'bg-[#333344] text-gray-400 border border-[#444455]'}`}>
                      {player.role || '未选择'}
                    </span>
                  </motion.div>
                ))}
              </div>
            </motion.div>

            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30"
            >
              <h2 className="text-xl font-bold mb-6 text-white drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">游戏控制</h2>
              <div className="space-y-4">
                <motion.button 
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="w-full bg-gradient-to-r from-[#22C55E] to-[#4ADE80] hover:from-[#16A34A] hover:to-[#22C55E] text-white px-4 py-3 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#22C55E]/30 hover:shadow-[#22C55E]/50"
                  onClick={startGame}
                >
                  开始游戏
                </motion.button>
                <motion.button 
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="w-full bg-gradient-to-r from-[#F43F5E] to-[#FB7185] hover:from-[#E11D48] hover:to-[#F43F5E] text-white px-4 py-3 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#F43F5E]/30 hover:shadow-[#F43F5E]/50"
                  onClick={leaveGame}
                >
                  离开游戏
                </motion.button>
              </div>
            </motion.div>
          </div>

          {/* 右侧：聊天功能 */}
          <div className="lg:col-span-1">
            <motion.div 
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl border border-[#7C3AED]/30 shadow-lg shadow-[#000000]/30 h-full flex flex-col"
            >
              <div className="p-6 border-b border-[#7C3AED]/30">
                <h2 className="text-xl font-bold text-white drop-shadow-[0_0_5px_rgba(124,58,237,0.3)]">聊天</h2>
              </div>
              <div className="flex-1 p-4 overflow-y-auto space-y-4">
                {messages.map((message) => (
                  <motion.div 
                    key={message.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3 }}
                    className={`p-4 rounded-lg border ${message.sender === '系统' ? 'bg-[#F59E0B]/10 border-[#F59E0B]/30' : message.sender === '我' ? 'bg-[#7C3AED]/10 border-[#7C3AED]/30' : 'bg-[#333344]/50 border-[#7C3AED]/30'}`}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <span className={`font-medium ${message.sender === '系统' ? 'text-[#F59E0B] drop-shadow-[0_0_3px_rgba(245,158,11,0.5)]' : message.sender === '我' ? 'text-[#A78BFA] drop-shadow-[0_0_3px_rgba(167,139,250,0.5)]' : 'text-white'}`}>
                        {message.sender}
                      </span>
                      <span className="text-xs text-gray-500">{message.time}</span>
                    </div>
                    <p className="text-gray-200">{message.content}</p>
                  </motion.div>
                ))}
              </div>
              <form onSubmit={handleSendMessage} className="p-6 border-t border-[#7C3AED]/30">
                <div className="flex gap-3">
                  <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="输入消息..."
                    className="flex-1 bg-[#333344] border border-[#7C3AED]/30 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-[#A78BFA] focus:ring-1 focus:ring-[#A78BFA] transition-all duration-300"
                  />
                  <motion.button 
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    type="submit" 
                    className="bg-gradient-to-r from-[#7C3AED] to-[#A78BFA] hover:from-[#6D28D9] hover:to-[#9333EA] text-white px-4 py-3 rounded-lg font-medium transition-all duration-300 shadow-lg shadow-[#7C3AED]/30 hover:shadow-[#7C3AED]/50"
                  >
                    发送
                  </motion.button>
                </div>
              </form>
            </motion.div>
          </div>
        </div>
      </div>

      {/* 角色选择弹窗 */}
      <AnimatePresence>
        {showCharacterSelect && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50"
          >
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              transition={{ type: 'spring', stiffness: 300, damping: 30 }}
              className="bg-gradient-to-br from-[#1A1A2E] to-[#2A2A4E] rounded-xl p-6 border border-[#A78BFA]/50 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto shadow-2xl shadow-[#7C3AED]/20"
            >
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold text-[#A78BFA] drop-shadow-[0_0_5px_rgba(167,139,250,0.5)]">选择角色</h3>
                <button 
                  className="text-gray-400 hover:text-white transition-colors text-xl"
                  onClick={() => setShowCharacterSelect(false)}
                >
                  ×
                </button>
              </div>
              <div className="space-y-4">
                {characters.map((character) => (
                  <motion.div 
                    key={character.id}
                    whileHover={{ scale: 1.02 }}
                    transition={{ duration: 0.2 }}
                    className={`p-4 rounded-lg border ${character.selected ? 'bg-[#7C3AED]/10 border-[#A78BFA]/50' : 'bg-[#333344]/50 border-[#7C3AED]/30'} cursor-pointer hover:border-[#A78BFA]/50 transition-all duration-300`}
                    onClick={() => !character.selected && handleCharacterSelect(character)}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="text-lg font-bold text-white">{character.name}</h4>
                      <span className={`px-3 py-1 rounded-full text-xs ${character.selected ? 'bg-[#7C3AED]/20 text-[#A78BFA] border border-[#A78BFA]/30' : 'bg-[#333344] text-gray-400 border border-[#444455]'}`}>
                        {character.selected ? '已选择' : '可选择'}
                      </span>
                    </div>
                    <p className="text-gray-300">{character.description}</p>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

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

export default GameRoom;
import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import WebSocketService from '../services/websocket/WebSocketService';

// 创建上下文
const WebSocketContext = createContext(null);

// 自定义钩子，用于在组件中使用WebSocket上下文
export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

// WebSocket提供者组件
export const WebSocketProvider = ({ children }) => {
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState(null);

  // 连接到WebSocket服务器
  const connect = useCallback(async (url) => {
    try {
      setError(null);
      await WebSocketService.connect(url);
      setIsConnected(true);
    } catch (err) {
      console.error('WebSocket连接失败:', err);
      setError(err.message);
      setIsConnected(false);
    }
  }, []);

  // 发送消息
  const sendMessage = useCallback((message) => {
    WebSocketService.send(message);
  }, []);

  // 注册消息处理器
  const onMessage = useCallback((type, handler) => {
    WebSocketService.on(type, handler);
  }, []);

  // 移除消息处理器
  const offMessage = useCallback((type, handler) => {
    WebSocketService.off(type, handler);
  }, []);

  // 断开连接
  const disconnect = useCallback(() => {
    WebSocketService.disconnect();
    setIsConnected(false);
  }, []);

  // 注册WebSocket事件处理器
  useEffect(() => {
    // 监听连接状态变化
    const handleConnect = () => {
      setIsConnected(true);
      setError(null);
    };

    const handleDisconnect = () => {
      setIsConnected(false);
    };

    const handleError = (err) => {
      setError(err.message);
    };

    // 这里可以根据实际的WebSocket服务实现来注册这些事件处理器
    // 例如：WebSocketService.on('connect', handleConnect);
    // WebSocketService.on('disconnect', handleDisconnect);
    // WebSocketService.on('error', handleError);

    // 清理函数
    return () => {
      // 移除事件处理器
      // WebSocketService.off('connect', handleConnect);
      // WebSocketService.off('disconnect', handleDisconnect);
      // WebSocketService.off('error', handleError);
    };
  }, []);

  // 断开连接的清理函数
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  // 上下文值
  const contextValue = {
    isConnected,
    error,
    connect,
    sendMessage,
    onMessage,
    offMessage,
    disconnect
  };

  return (
    <WebSocketContext.Provider value={contextValue}>
      {children}
    </WebSocketContext.Provider>
  );
};
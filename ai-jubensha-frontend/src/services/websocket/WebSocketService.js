class WebSocketService {
  constructor() {
    this.socket = null;
    this.messageHandlers = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 1000;
  }

  /**
   * 连接到WebSocket服务器
   * @param {string} url - WebSocket服务器URL
   * @returns {Promise}
   */
  connect(url) {
    return new Promise((resolve, reject) => {
      try {
        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
          console.log('WebSocket连接已建立');
          this.reconnectAttempts = 0;
          resolve();
        };

        this.socket.onmessage = (event) => {
          this.handleMessage(event);
        };

        this.socket.onclose = (event) => {
          console.log('WebSocket连接已关闭:', event.code, event.reason);
          this.attemptReconnect(url);
        };

        this.socket.onerror = (error) => {
          console.error('WebSocket错误:', error);
          reject(error);
        };
      } catch (error) {
        console.error('WebSocket连接失败:', error);
        reject(error);
      }
    });
  }

  /**
   * 尝试重新连接
   * @param {string} url - WebSocket服务器URL
   */
  attemptReconnect(url) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connect(url).catch(error => {
          console.error('重新连接失败:', error);
        });
      }, this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1)); // 指数退避
    } else {
      console.error('达到最大重连尝试次数，停止重连');
    }
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  send(message) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    } else {
      console.error('WebSocket未连接，无法发送消息');
    }
  }

  /**
   * 处理收到的消息
   * @param {Event} event - 消息事件
   */
  handleMessage(event) {
    try {
      const message = JSON.parse(event.data);
      const { type, data } = message;
      
      console.log('收到WebSocket消息:', type, data);
      
      // 调用对应的消息处理器
      if (this.messageHandlers.has(type)) {
        const handlers = this.messageHandlers.get(type);
        handlers.forEach(handler => {
          try {
            handler(data);
          } catch (error) {
            console.error(`处理消息类型 ${type} 时出错:`, error);
          }
        });
      }
    } catch (error) {
      console.error('解析WebSocket消息时出错:', error);
    }
  }

  /**
   * 注册消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 消息处理器
   */
  on(type, handler) {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, []);
    }
    this.messageHandlers.get(type).push(handler);
  }

  /**
   * 移除消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 消息处理器
   */
  off(type, handler) {
    if (this.messageHandlers.has(type)) {
      const handlers = this.messageHandlers.get(type);
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }

  /**
   * 关闭WebSocket连接
   */
  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.messageHandlers.clear();
  }

  /**
   * 检查WebSocket连接状态
   * @returns {boolean}
   */
  isConnected() {
    return this.socket && this.socket.readyState === WebSocket.OPEN;
  }
}

// 导出单例实例
export default new WebSocketService();
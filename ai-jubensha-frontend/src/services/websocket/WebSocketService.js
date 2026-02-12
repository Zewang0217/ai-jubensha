/**
 * WebSocket 服务类
 * 封装 WebSocket 连接管理、消息处理和自动重连功能
 */
class WebSocketService {
  constructor() {
      this.socket = null
      this.messageHandlers = new Map()
      this.reconnectAttempts = 0
      this.maxReconnectAttempts = 5
      this.reconnectDelay = 1000
      this.url = null
      this.isManualClose = false
  }

  /**
   * 连接到 WebSocket 服务器
   * @param {string} url - WebSocket 服务器 URL
   * @returns {Promise}
   */
  connect(url) {
    return new Promise((resolve, reject) => {
        if (this.socket?.readyState === WebSocket.OPEN) {
            console.log('WebSocket 已经连接')
            resolve()
            return
        }

        this.url = url
        this.isManualClose = false

      try {
          this.socket = new WebSocket(url)

        this.socket.onopen = () => {
            console.log('WebSocket 连接已建立')
            this.reconnectAttempts = 0
            this.emit('__connected__')
            resolve()
        }

        this.socket.onmessage = (event) => {
            this.handleMessage(event)
        }

        this.socket.onclose = (event) => {
            console.log('WebSocket 连接已关闭:', event.code, event.reason)
            this.emit('__disconnected__')

            if (!this.isManualClose) {
                this.attemptReconnect()
            }
        }

        this.socket.onerror = (error) => {
            console.error('WebSocket 错误:', error)
            this.emit('__error__', error)
            reject(error)
        }
      } catch (error) {
          console.error('WebSocket 连接失败:', error)
          reject(error)
      }
    })
  }

  /**
   * 尝试重新连接
   */
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
        this.reconnectAttempts++
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1)

        console.log(`尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})，${delay}ms 后重试`)
      
      setTimeout(() => {
          if (this.url) {
              this.connect(this.url).catch(error => {
                  console.error('重新连接失败:', error)
              })
          }
      }, delay)
    } else {
        console.error('达到最大重连尝试次数，停止重连')
    }
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   * @returns {boolean}
   */
  send(message) {
      if (this.socket?.readyState === WebSocket.OPEN) {
          const messageStr = typeof message === 'string' ? message : JSON.stringify(message)
          this.socket.send(messageStr)
          return true
    } else {
          console.error('WebSocket 未连接，无法发送消息')
          return false
    }
  }

  /**
   * 处理收到的消息
   * @param {MessageEvent} event - 消息事件
   */
  handleMessage(event) {
    try {
        const message = JSON.parse(event.data)
        const {type, data} = message

        console.log('收到 WebSocket 消息:', type, data)

        // 触发全局消息事件
        this.emit('__message__', message)

        // 触发特定类型的消息处理器
        if (type && this.messageHandlers.has(type)) {
            const handlers = this.messageHandlers.get(type)
        handlers.forEach(handler => {
          try {
              handler(data)
          } catch (error) {
              console.error(`处理消息类型 ${type} 时出错:`, error)
          }
        })
      }
    } catch (error) {
        console.error('解析 WebSocket 消息时出错:', error)
        // 尝试作为纯文本处理
        this.emit('__message__', {type: 'raw', data: event.data})
    }
  }

  /**
   * 注册消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 消息处理器
   */
  on(type, handler) {
      if (typeof handler !== 'function') {
          console.warn('消息处理器必须是函数')
          return
      }
    
    if (!this.messageHandlers.has(type)) {
        this.messageHandlers.set(type, [])
    }
      this.messageHandlers.get(type).push(handler)
  }

  /**
   * 移除消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 消息处理器
   */
  off(type, handler) {
      if (!this.messageHandlers.has(type)) return

      const handlers = this.messageHandlers.get(type)
      const index = handlers.indexOf(handler)
      if (index > -1) {
          handlers.splice(index, 1)
      }
  }

    /**
     * 触发事件
     * @param {string} type - 事件类型
     * @param {*} data - 事件数据
     */
    emit(type, data) {
        if (this.messageHandlers.has(type)) {
            const handlers = this.messageHandlers.get(type)
            handlers.forEach(handler => {
                try {
                    handler(data)
                } catch (error) {
                    console.error(`触发事件 ${type} 时出错:`, error)
                }
            })
    }
  }

  /**
   * 关闭 WebSocket 连接
   */
  disconnect() {
      this.isManualClose = true
    if (this.socket) {
        this.socket.close()
        this.socket = null
    }
      this.messageHandlers.clear()
      this.reconnectAttempts = 0
  }

  /**
   * 检查 WebSocket 连接状态
   * @returns {boolean}
   */
  isConnected() {
      return this.socket?.readyState === WebSocket.OPEN
  }

    /**
     * 获取当前连接状态
     * @returns {number|null}
     */
    getReadyState() {
        return this.socket?.readyState ?? null
  }
}

// 导出单例实例
export default new WebSocketService()

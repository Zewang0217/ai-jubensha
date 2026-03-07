/**
 * WebSocket 服务类
 * 使用 STOMP over SockJS 协议连接后端
 * @author zewang
 * @author luobo
 */
import {Client} from '@stomp/stompjs'
import SockJS from 'sockjs-client'

class WebSocketService {
    constructor() {
        this.client = null
        this.messageHandlers = new Map()
        this.subscriptions = new Map()
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectDelay = 1000
        this.baseUrl = null
        this.gameId = null
        this.isManualClose = false
        this.connectionPromise = null
    }

    /**
     * 连接到 WebSocket 服务器
     * @param {string} baseUrl - WebSocket 服务器基础 URL
     * @param {number} gameId - 游戏 ID
     * @returns {Promise}
     */
    connect(baseUrl, gameId) {
        return new Promise((resolve, reject) => {
            if (this.client?.connected) {
                console.log('[WebSocketService] 已经连接')
                resolve()
                return
            }

            this.baseUrl = baseUrl
            this.gameId = gameId
            this.isManualClose = false

            const wsUrl = `${baseUrl}/ws?gameId=${gameId}`
            console.log('[WebSocketService] 正在连接:', wsUrl)

            try {
                this.client = new Client({
                    webSocketFactory: () => new SockJS(wsUrl),
                    reconnectDelay: this.reconnectDelay,
                    heartbeatIncoming: 25000,
                    heartbeatOutgoing: 25000,
                    onConnect: (frame) => {
                        console.log('[WebSocketService] STOMP 连接已建立:', frame)
                        this.reconnectAttempts = 0
                        this.emit('__connected__')
                        resolve()
                    },
                    onDisconnect: (frame) => {
                        console.log('[WebSocketService] STOMP 连接已断开:', frame)
                        this.emit('__disconnected__')
                    },
                    onStompError: (frame) => {
                        console.error('[WebSocketService] STOMP 错误:', frame)
                        this.emit('__error__', frame)
                        reject(new Error(frame.headers?.message || 'STOMP error'))
                    },
                    onWebSocketError: (event) => {
                        console.error('[WebSocketService] WebSocket 错误:', event)
                        this.emit('__error__', event)
                    },
                    onWebSocketClose: (event) => {
                        console.log('[WebSocketService] WebSocket 连接已关闭:', event.code, event.reason)
                        if (!this.isManualClose && this.reconnectAttempts < this.maxReconnectAttempts) {
                            this.reconnectAttempts++
                            console.log(`[WebSocketService] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
                        }
                    },
                })

                this.client.activate()
            } catch (error) {
                console.error('[WebSocketService] 连接失败:', error)
                reject(error)
            }
        })
    }

    /**
     * 订阅主题
     * @param {string} destination - 订阅目标
     * @param {Function} handler - 消息处理器
     * @returns {string} 订阅 ID
     */
    subscribe(destination, handler) {
        if (!this.client?.connected) {
            console.error('[WebSocketService] 未连接，无法订阅:', destination)
            return null
        }

        console.log('[WebSocketService] 订阅主题:', destination)

        const subscription = this.client.subscribe(destination, (message) => {
            try {
                const body = JSON.parse(message.body)
                console.log('[WebSocketService] 收到消息:', destination, body)
                handler(body)
            } catch (error) {
                console.error('[WebSocketService] 解析消息失败:', error)
                handler(message.body)
            }
        })

        this.subscriptions.set(subscription.id, subscription)
        return subscription.id
    }

    /**
     * 取消订阅
     * @param {string} subscriptionId - 订阅 ID
     */
    unsubscribe(subscriptionId) {
        const subscription = this.subscriptions.get(subscriptionId)
        if (subscription) {
            subscription.unsubscribe()
            this.subscriptions.delete(subscriptionId)
            console.log('[WebSocketService] 取消订阅:', subscriptionId)
        }
    }

    /**
     * 发送消息
     * @param {string} destination - 发送目标
     * @param {Object} message - 消息对象
     * @returns {boolean}
     */
    send(destination, message) {
        if (!this.client?.connected) {
            console.error('[WebSocketService] 未连接，无法发送消息')
            return false
        }

        console.log('[WebSocketService] 发送消息:', destination, message)
        this.client.publish({
            destination: destination,
            body: JSON.stringify(message),
        })
        return true
    }

    /**
     * 发送聊天消息
     * @param {string|Object} content - 聊天内容或消息对象
     * @returns {boolean}
     */
    sendChatMessage(content) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return false
        }

        // 如果传入的是字符串，包装成消息对象
        const message = typeof content === 'string' ? {
            type: 'CHAT_MESSAGE',
            payload: content,
        } : content

        return this.send(`/app/game/${this.gameId}/chat`, message)
    }

    /**
     * 发送投票
     * @param {number|Object} characterId - 角色ID或投票消息对象
     * @returns {boolean}
     */
    sendVote(characterId) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return false
        }

        // 如果传入的是数字，包装成投票对象
        const message = typeof characterId === 'number' ? {
            type: 'VOTE_SUBMIT',
            payload: {characterId},
        } : characterId

        return this.send(`/app/game/${this.gameId}/vote`, message)
    }

    /**
     * 订阅游戏聊天
     * @param {Function} handler - 消息处理器
     * @returns {string} 订阅 ID
     */
    subscribeToGameChat(handler) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return null
        }

        return this.subscribe(`/topic/game/${this.gameId}/chat`, handler)
    }

    /**
     * 订阅个人消息
     * @param {Function} handler - 消息处理器
     * @returns {string} 订阅 ID
     */
    subscribeToPersonalMessages(handler) {
        return this.subscribe('/user/queue/messages', handler)
    }

    /**
     * 订阅公开线索广播
     * @param {Function} handler - 消息处理器，接收 PublicClueMessage
     * @returns {string} 订阅 ID
     */
    subscribeToPublicClue(handler) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return null
        }
        return this.subscribe(`/topic/game/${this.gameId}/public-clue`, handler)
    }

    /**
     * 订阅投票结果广播
     * @param {Function} handler - 消息处理器，接收 VoteResultMessage
     * @returns {string} 订阅 ID
     */
    subscribeToVoteResult(handler) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return null
        }
        return this.subscribe(`/topic/game/${this.gameId}/vote-result`, handler)
    }

    /**
     * 订阅 AI Agent 操作消息（公屏）
     * @param {Function} handler - 消息处理器，接收 AgentActionMessage
     * @returns {string} 订阅 ID
     */
    subscribeToAgentActions(handler) {
        if (!this.gameId) {
            console.error('[WebSocketService] gameId 未设置')
            return null
        }
        return this.subscribe(`/topic/game/${this.gameId}/agent-actions`, handler)
    }

    /**
     * 注册事件处理器
     * @param {string} type - 事件类型
     * @param {Function} handler - 处理器
     */
    on(type, handler) {
        if (typeof handler !== 'function') {
            console.warn('[WebSocketService] 处理器必须是函数')
            return
        }

        if (!this.messageHandlers.has(type)) {
            this.messageHandlers.set(type, [])
        }
        this.messageHandlers.get(type).push(handler)
    }

    /**
     * 移除事件处理器
     * @param {string} type - 事件类型
     * @param {Function} handler - 处理器
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
            handlers.forEach((handler) => {
                try {
                    handler(data)
                } catch (error) {
                    console.error(`[WebSocketService] 触发事件 ${type} 时出错:`, error)
                }
            })
        }
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.isManualClose = true

        // 取消所有订阅
        this.subscriptions.forEach((subscription) => {
            subscription.unsubscribe()
        })
        this.subscriptions.clear()

        // 断开连接
        if (this.client) {
            this.client.deactivate()
            this.client = null
        }

        this.messageHandlers.clear()
        this.reconnectAttempts = 0
        console.log('[WebSocketService] 已断开连接')
    }

    /**
     * 检查连接状态
     * @returns {boolean}
     */
    isConnected() {
        return this.client?.connected || false
    }

    /**
     * 获取当前游戏 ID
     * @returns {number|null}
     */
    getGameId() {
        return this.gameId
    }
}

// 导出单例实例
export default new WebSocketService()

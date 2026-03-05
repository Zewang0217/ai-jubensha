import {useCallback, useEffect, useRef, useState} from 'react'
import WebSocketService from '../services/websocket/WebSocketService'

/**
 * WebSocket Hook
 * 用于在组件中方便地使用 STOMP WebSocket 功能
 * @author zewang
 */
export const useWebSocket = (baseUrl, gameId) => {
    const [isConnected, setIsConnected] = useState(false)
    const [error, setError] = useState(null)
    const [lastMessage, setLastMessage] = useState(null)
    const wsRef = useRef(WebSocketService)

    /**
     * 连接到 WebSocket 服务器
     * @param {string} connectionUrl - 基础 URL
     * @param {number} connectionGameId - 游戏 ID
     * @returns {Promise<boolean>}
     */
    const connect = useCallback(async (connectionUrl = baseUrl, connectionGameId = gameId) => {
        if (!connectionUrl || !connectionGameId) {
            const errorMsg = 'WebSocket URL 和 gameId 都是必需的'
            console.error('[useWebSocket]', errorMsg)
            setError(errorMsg)
            return false
        }

        try {
            setError(null)
            console.log('[useWebSocket] 正在连接...', {connectionUrl, connectionGameId})
            await wsRef.current.connect(connectionUrl, connectionGameId)
            setIsConnected(true)
            setLastMessage(null)
            return true
        } catch (err) {
            console.error('[useWebSocket] 连接失败:', err)
            setError(err.message)
            setIsConnected(false)
            return false
        }
    }, [baseUrl, gameId])

    /**
     * 断开连接
     */
    const disconnect = useCallback(() => {
        wsRef.current.disconnect()
        setIsConnected(false)
    }, [])

    /**
     * 发送聊天消息
     * @param {string|Object} content - 聊天内容或消息对象
     * @returns {boolean}
     */
    const sendChatMessage = useCallback((content) => {
        return wsRef.current.sendChatMessage(content)
    }, [])

    /**
     * 发送投票
     * @param {number|Object} characterId - 角色ID或投票消息对象
     * @returns {boolean}
     */
    const sendVote = useCallback((characterId) => {
        return wsRef.current.sendVote(characterId)
    }, [])

    /**
     * 发送自定义消息
     * @param {string} destination - 目标路径
     * @param {Object} message - 消息对象
     * @returns {boolean}
     */
    const sendMessage = useCallback((destination, message) => {
        return wsRef.current.send(destination, message)
    }, [])

    /**
     * 订阅游戏聊天
     * @param {Function} handler - 消息处理器
     * @returns {string|null} 订阅 ID
     */
    const subscribeToGameChat = useCallback((handler) => {
        return wsRef.current.subscribeToGameChat(handler)
    }, [])

    /**
     * 订阅个人消息
     * @param {Function} handler - 消息处理器
     * @returns {string|null} 订阅 ID
     */
    const subscribeToPersonalMessages = useCallback((handler) => {
        return wsRef.current.subscribeToPersonalMessages(handler)
    }, [])

    /**
     * 订阅自定义主题
     * @param {string} destination - 订阅目标
     * @param {Function} handler - 消息处理器
     * @returns {string|null} 订阅 ID
     */
    const subscribe = useCallback((destination, handler) => {
        return wsRef.current.subscribe(destination, handler)
    }, [])

    /**
     * 取消订阅
     * @param {string} subscriptionId - 订阅 ID
     */
    const unsubscribe = useCallback((subscriptionId) => {
        wsRef.current.unsubscribe(subscriptionId)
    }, [])

    /**
     * 注册事件处理器
     * @param {string} type - 事件类型
     * @param {Function} handler - 处理器
     */
    const on = useCallback((type, handler) => {
        wsRef.current.on(type, handler)
    }, [])

    /**
     * 移除事件处理器
     * @param {string} type - 事件类型
     * @param {Function} handler - 处理器
     */
    const off = useCallback((type, handler) => {
        wsRef.current.off(type, handler)
    }, [])

    // 自动连接（如果提供了 URL 和 gameId）
    const hasConnectedRef = useRef(false)
    useEffect(() => {
        // 只有当 URL 和 gameId 都有效时才连接
        if (baseUrl && gameId && !hasConnectedRef.current) {
            hasConnectedRef.current = true
            connect(baseUrl, gameId).catch(err => {
                console.error('[useWebSocket] 自动连接失败:', err)
            })
        }

        return () => {
            if (hasConnectedRef.current) {
                disconnect()
                hasConnectedRef.current = false
            }
        }
    }, [baseUrl, gameId])

    // 监听连接状态变化
    useEffect(() => {
        const handleConnect = () => {
            setIsConnected(true)
            setError(null)
        }
        const handleDisconnect = () => setIsConnected(false)
        const handleError = (err) => {
            console.error('[useWebSocket] 连接错误:', err)
            setError(err?.headers?.message || err?.message || '连接错误')
        }

        wsRef.current.on('__connected__', handleConnect)
        wsRef.current.on('__disconnected__', handleDisconnect)
        wsRef.current.on('__error__', handleError)

        return () => {
            wsRef.current.off('__connected__', handleConnect)
            wsRef.current.off('__disconnected__', handleDisconnect)
            wsRef.current.off('__error__', handleError)
        }
    }, [])

    return {
        isConnected,
        error,
        lastMessage,
        connect,
        disconnect,
        sendChatMessage,
        sendVote,
        sendMessage,
        subscribeToGameChat,
        subscribeToPersonalMessages,
        subscribe,
        unsubscribe,
        on,
        off,
    }
}

/**
 * 使用 WebSocket 消息的 Hook
 * @param {string} destination - 订阅目标
 * @param {Function} handler - 消息处理器
 */
export const useWebSocketMessage = (destination, handler) => {
    const wsRef = useRef(WebSocketService)
    const subscriptionIdRef = useRef(null)

    useEffect(() => {
        if (!destination || !handler) return

        // 等待连接后再订阅
        const subscribeWhenConnected = () => {
            if (wsRef.current.isConnected()) {
                subscriptionIdRef.current = wsRef.current.subscribe(destination, handler)
            } else {
                // 如果未连接，等待连接后再订阅
                const handleConnect = () => {
                    subscriptionIdRef.current = wsRef.current.subscribe(destination, handler)
                    wsRef.current.off('__connected__', handleConnect)
                }
                wsRef.current.on('__connected__', handleConnect)
            }
        }

        subscribeWhenConnected()

        return () => {
            if (subscriptionIdRef.current) {
                wsRef.current.unsubscribe(subscriptionIdRef.current)
            }
        }
    }, [destination, handler])
}

/**
 * 使用 WebSocket 连接状态的 Hook
 */
export const useWebSocketStatus = () => {
    const wsRef = useRef(WebSocketService)
    const [isConnected, setIsConnected] = useState(false)

    useEffect(() => {
        // 设置初始状态
        setIsConnected(wsRef.current.isConnected())

        const handleConnect = () => setIsConnected(true)
        const handleDisconnect = () => setIsConnected(false)

        wsRef.current.on('__connected__', handleConnect)
        wsRef.current.on('__disconnected__', handleDisconnect)

        return () => {
            wsRef.current.off('__connected__', handleConnect)
            wsRef.current.off('__disconnected__', handleDisconnect)
        }
    }, [])

    return isConnected
}

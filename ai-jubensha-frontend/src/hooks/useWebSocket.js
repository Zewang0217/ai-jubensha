import {useCallback, useEffect, useRef, useState} from 'react'
import WebSocketService from '../services/websocket/WebSocketService'

/**
 * WebSocket Hook
 * 用于在组件中方便地使用 WebSocket 功能
 */
export const useWebSocket = (url) => {
    const [isConnected, setIsConnected] = useState(false)
    const [error, setError] = useState(null)
    const [lastMessage, setLastMessage] = useState(null)
    const wsRef = useRef(WebSocketService)

    // 连接
    const connect = useCallback(async (connectionUrl = url) => {
        if (!connectionUrl) {
            setError('WebSocket URL is required')
            return false
        }

        try {
            setError(null)
            await wsRef.current.connect(connectionUrl)
            setIsConnected(true)
            return true
        } catch (err) {
            setError(err.message)
            setIsConnected(false)
            return false
        }
    }, [url])

    // 断开连接
    const disconnect = useCallback(() => {
        wsRef.current.disconnect()
        setIsConnected(false)
    }, [])

    // 发送消息
    const sendMessage = useCallback((message) => {
        return wsRef.current.send(message)
    }, [])

    // 注册消息处理器
    const onMessage = useCallback((type, handler) => {
        wsRef.current.on(type, handler)
    }, [])

    // 移除消息处理器
    const offMessage = useCallback((type, handler) => {
        wsRef.current.off(type, handler)
    }, [])

    // 自动连接（如果提供了 URL）
    useEffect(() => {
        if (url) {
            connect()
        }

        return () => {
            disconnect()
        }
    }, [url, connect, disconnect])

    return {
        isConnected,
        error,
        lastMessage,
        connect,
        disconnect,
        sendMessage,
        onMessage,
        offMessage,
    }
}

/**
 * 使用 WebSocket 消息的 Hook
 * @param {string} type - 消息类型
 * @param {Function} handler - 消息处理器
 */
export const useWebSocketMessage = (type, handler) => {
    const wsRef = useRef(WebSocketService)

    useEffect(() => {
        if (!type || !handler) return

        wsRef.current.on(type, handler)

        return () => {
            wsRef.current.off(type, handler)
        }
    }, [type, handler])
}

/**
 * 使用 WebSocket 连接状态的 Hook
 */
export const useWebSocketStatus = () => {
    const [isConnected, setIsConnected] = useState(false)
    const wsRef = useRef(WebSocketService)

    useEffect(() => {
        const handleConnect = () => setIsConnected(true)
        const handleDisconnect = () => setIsConnected(false)

        wsRef.current.on('__connected__', handleConnect)
        wsRef.current.on('__disconnected__', handleDisconnect)

        // 初始状态
        setIsConnected(wsRef.current.isConnected())

        return () => {
            wsRef.current.off('__connected__', handleConnect)
            wsRef.current.off('__disconnected__', handleDisconnect)
        }
    }, [])

    return isConnected
}

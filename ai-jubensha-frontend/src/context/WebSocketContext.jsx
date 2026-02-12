import {useCallback, useEffect, useRef, useState} from 'react'
import WebSocketService from '../services/websocket/WebSocketService'
import {WebSocketContext} from './WebSocketContext.js'

export const WebSocketProvider = ({children}) => {
    const [isConnected, setIsConnected] = useState(false)
    const [error, setError] = useState(null)
    const [lastMessage, setLastMessage] = useState(null)
    const wsServiceRef = useRef(WebSocketService)

    // 连接到 WebSocket 服务器
    const connect = useCallback(async (url) => {
        try {
            setError(null)
            await wsServiceRef.current.connect(url)
            setIsConnected(true)
        } catch (err) {
            console.error('WebSocket 连接失败:', err)
            setError(err.message)
            setIsConnected(false)
        }
    }, [])

    // 断开连接
    const disconnect = useCallback(() => {
        wsServiceRef.current.disconnect()
        setIsConnected(false)
    }, [])

    // 发送消息
    const sendMessage = useCallback((message) => {
        if (!isConnected) {
            console.warn('WebSocket 未连接，无法发送消息')
            return false
        }
        wsServiceRef.current.send(message)
        return true
    }, [isConnected])

    // 注册消息处理器
    const onMessage = useCallback((type, handler) => {
        wsServiceRef.current.on(type, handler)
    }, [])

    // 移除消息处理器
    const offMessage = useCallback((type, handler) => {
        wsServiceRef.current.off(type, handler)
    }, [])

    // 监听连接状态变化
    useEffect(() => {
        const wsService = wsServiceRef.current

        const handleConnect = () => {
            setIsConnected(true)
            setError(null)
        }

        const handleDisconnect = () => {
            setIsConnected(false)
        }

        const handleError = (err) => {
            setError(err?.message || 'WebSocket 错误')
        }

        const handleMessage = (message) => {
            setLastMessage(message)
        }

        // 注册全局事件监听
        wsService.on('__connected__', handleConnect)
        wsService.on('__disconnected__', handleDisconnect)
        wsService.on('__error__', handleError)
        wsService.on('__message__', handleMessage)

        return () => {
            wsService.off('__connected__', handleConnect)
            wsService.off('__disconnected__', handleDisconnect)
            wsService.off('__error__', handleError)
            wsService.off('__message__', handleMessage)
        }
    }, [])

    // 组件卸载时断开连接
    useEffect(() => {
        return () => {
            disconnect()
        }
    }, [disconnect])

    const contextValue = {
        isConnected,
        error,
        lastMessage,
        connect,
        disconnect,
        sendMessage,
        onMessage,
        offMessage,
    }

    return (
        <WebSocketContext.Provider value={contextValue}>
            {children}
        </WebSocketContext.Provider>
    )
}

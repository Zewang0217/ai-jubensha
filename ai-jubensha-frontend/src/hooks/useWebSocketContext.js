import {useContext} from 'react'
import {WebSocketContext} from '../context/WebSocketContext'

/**
 * 使用 WebSocket 上下文的 Hook
 * 必须在 WebSocketProvider 内部使用
 */
export const useWebSocketContext = () => {
    const context = useContext(WebSocketContext)
    if (!context) {
        throw new Error('useWebSocketContext must be used within a WebSocketProvider')
    }
    return context
}

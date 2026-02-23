/**
 * @fileoverview 调试模式 Hook
 * @description 提供前端离线调试功能，无需后端连接
 */

import {useCallback, useEffect, useMemo, useState} from 'react'
import {getMockGameData, mockApiResponses,} from '../services/mockData'

/**
 * 调试模式配置
 */
const DEBUG_CONFIG = {
    /** 默认启用调试模式 */
    DEFAULT_ENABLED: import.meta.env.DEV || import.meta.env.VITE_DEBUG_MODE === 'true',
    /** 模拟延迟（毫秒） */
    MOCK_DELAY: 300,
    /** 模拟 WebSocket 连接 */
    MOCK_WEBSOCKET: true,
}

/**
 * 创建模拟 WebSocket 服务
 *
 * @returns {Object} 模拟 WebSocket 对象
 */
function createMockWebSocket() {
    const listeners = new Map()
    let isConnected = false

    const ws = {
        isConnected: () => isConnected,

        connect: () => {
            setTimeout(() => {
                isConnected = true
                listeners.get('open')?.forEach(cb => cb())
                console.log('[MockWebSocket] 连接成功')
            }, DEBUG_CONFIG.MOCK_DELAY)
        },

        disconnect: () => {
            isConnected = false
            listeners.get('close')?.forEach(cb => cb())
        },

        send: (message) => {
            console.log('[MockWebSocket] 发送消息:', message)
            // 模拟服务器响应
            setTimeout(() => {
                handleMockResponse(message, ws)
            }, DEBUG_CONFIG.MOCK_DELAY)
        },

        on: (event, callback) => {
            if (!listeners.has(event)) {
                listeners.set(event, new Set())
            }
            listeners.get(event).add(callback)

            return () => {
                listeners.get(event)?.delete(callback)
            }
        },

        off: (event, callback) => {
            listeners.get(event)?.delete(callback)
        },

        emit: (event, data) => {
            listeners.get(event)?.forEach(cb => cb(data))
        },

        // 模拟接收消息
        simulateReceive: (type, data) => {
            ws.emit('message', {type, data})
        },
    }

    return ws
}

/**
 * 处理模拟响应
 *
 * @param {Object} message - 发送的消息
 * @param {Object} ws - WebSocket 对象
 */
function handleMockResponse(message, ws) {
    switch (message.type) {
        case 'GAME_LEAVE':
            ws.emit('message', {type: 'GAME_LEFT', data: {success: true}})
            break
        case 'CLUE_SEARCH':
            ws.emit('message', {
                type: 'CLUE_FOUND',
                data: {
                    scene: message.scene,
                    clueId: `clue-${Date.now()}`,
                    timestamp: Date.now(),
                },
            })
            break
        case 'VOTE_CAST':
            ws.emit('message', {
                type: 'VOTE_UPDATED',
                data: {
                    voterId: 'player-001',
                    targetId: message.targetId,
                    timestamp: Date.now(),
                },
            })
            break
        case 'CHAT_MESSAGE':
            ws.emit('message', {
                type: 'CHAT_MESSAGE',
                data: {
                    id: `msg-${Date.now()}`,
                    sender: '你',
                    content: message.message?.content || '',
                    time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}),
                    isAI: false,
                },
            })
            // 模拟 AI 回复
            setTimeout(() => {
                ws.emit('message', {
                    type: 'CHAT_MESSAGE',
                    data: {
                        id: `msg-${Date.now()}-ai`,
                        sender: '苏医生',
                        content: '这是一个有趣的发现，我们需要更多证据。',
                        time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}),
                        isAI: true,
                    },
                })
            }, 2000)
            break
        default:
            console.log('[MockWebSocket] 未处理的消息类型:', message.type)
    }
}

/**
 * 调试模式 Hook
 *
 * @param {Object} options - 配置选项
 * @param {boolean} [options.enabled] - 是否启用调试模式
 * @returns {Object} 调试模式 API
 *
 * @example
 * const {
 *   isDebugMode,
 *   gameData,
 *   playerData,
 *   isLoading,
 *   error,
 *   webSocket,
 *   toggleDebugMode,
 *   forcePhaseChange,
 * } = useDebugMode({ enabled: true })
 */
export function useDebugMode(options = {}) {
    const {enabled = DEBUG_CONFIG.DEFAULT_ENABLED} = options

    // 调试模式状态
    const [isDebugMode, setIsDebugMode] = useState(enabled)
    const [isLoading, setIsLoading] = useState(true)
    const [error] = useState(null)

    // 模拟数据状态
    const [gameData, setGameData] = useState(null)
    const [playerData, setPlayerData] = useState(null)

    // WebSocket 状态
    const [wsState, setWsState] = useState({
        isConnected: false,
        messages: [],
    })

    // 模拟 WebSocket 实例
    const mockWs = useMemo(() => createMockWebSocket(), [])

    // 初始化数据
    useEffect(() => {
        if (!isDebugMode) {
            setGameData(null)
            setPlayerData(null)
            return
        }

        // 模拟加载延迟
        setIsLoading(true)
        const timer = setTimeout(() => {
            setGameData(getMockGameData())
            setPlayerData(mockApiResponses.getPlayer().data)
            setIsLoading(false)

            // 自动连接 WebSocket
            mockWs.connect()
        }, DEBUG_CONFIG.MOCK_DELAY)

        return () => {
            clearTimeout(timer)
            mockWs.disconnect()
        }
    }, [isDebugMode, mockWs])

    // WebSocket 事件监听
    useEffect(() => {
        if (!isDebugMode) return

        const handleOpen = () => {
            setWsState(prev => ({...prev, isConnected: true}))
        }

        const handleClose = () => {
            setWsState(prev => ({...prev, isConnected: false}))
        }

        const handleMessage = (message) => {
            setWsState(prev => ({
                ...prev,
                messages: [...prev.messages, message],
            }))
        }

        const unsubOpen = mockWs.on('open', handleOpen)
        const unsubClose = mockWs.on('close', handleClose)
        const unsubMessage = mockWs.on('message', handleMessage)

        return () => {
            unsubOpen?.()
            unsubClose?.()
            unsubMessage?.()
        }
    }, [isDebugMode, mockWs])

    /**
     * 切换调试模式
     */
    const toggleDebugMode = useCallback(() => {
        setIsDebugMode(prev => !prev)
    }, [])

    /**
     * 强制切换阶段（调试用）
     *
     * @param {string} phase - 目标阶段
     */
    const forcePhaseChange = useCallback((phase) => {
        if (!isDebugMode) return

        mockWs.simulateReceive('PHASE_CHANGE', {phase})
        console.log('[Debug] 强制切换到阶段:', phase)
    }, [isDebugMode, mockWs])

    /**
     * 发送 WebSocket 消息
     *
     * @param {Object} message - 消息对象
     */
    const sendMessage = useCallback((message) => {
        if (!isDebugMode) return

        mockWs.send(message)
    }, [isDebugMode, mockWs])

    /**
     * 监听 WebSocket 消息
     *
     * @param {string} type - 消息类型
     * @param {Function} callback - 回调函数
     * @returns {Function} 取消监听函数
     */
    const onMessage = useCallback((type, callback) => {
        if (!isDebugMode) return () => {
        }

        const handler = (message) => {
            if (message.type === type) {
                callback(message.data)
            }
        }

        return mockWs.on('message', handler)
    }, [isDebugMode, mockWs])

    /**
     * 取消监听
     *
     * @param {string} type - 消息类型
     */
    const offMessage = useCallback((_type) => {
        // 由于我们使用回调包装，这里不需要特别处理
        // 实际取消在 onMessage 返回的函数中处理
    }, [])

    /**
     * 模拟 AI 发送消息
     *
     * @param {string} content - 消息内容
     * @param {string} [sender] - 发送者名称
     */
    const simulateAIMessage = useCallback((content, sender = '苏医生') => {
        if (!isDebugMode) return

        mockWs.simulateReceive('CHAT_MESSAGE', {
            id: `msg-${Date.now()}`,
            sender,
            content,
            time: new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}),
            isAI: true,
        })
    }, [isDebugMode, mockWs])

    /**
     * 更新游戏数据（调试用）
     *
     * @param {Function} updater - 更新函数
     */
    const updateGameData = useCallback((updater) => {
        setGameData(prev => updater(prev))
    }, [])

    return {
        // 状态
        isDebugMode,
        isLoading,
        error,

        // 数据
        gameData,
        playerData,

        // WebSocket
        isConnected: wsState.isConnected,
        sendMessage,
        onMessage,
        offMessage,

        // 控制方法
        toggleDebugMode,
        forcePhaseChange,
        simulateAIMessage,
        updateGameData,
    }
}

export default useDebugMode

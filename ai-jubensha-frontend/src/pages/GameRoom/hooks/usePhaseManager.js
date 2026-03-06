import {useCallback, useMemo, useState} from 'react'
import {DEFAULT_PHASE_SEQUENCE, PHASE_CONFIG} from '../types'
import {confirmPhase} from '../../../services/api'

/**
 * @fileoverview 阶段管理器 Hook
 * @description 管理游戏阶段的切换、数据缓存和历史记录，支持自定义阶段序列和阶段同步
 * 
 * @description 后端主导模式说明：
 * - 前端不主动切换阶段，而是通过调用 confirmPhase API 确认阶段完成
 * - 后端在所有玩家确认后推进阶段，并通过 WebSocket 广播 PHASE_CHANGE 消息
 * - 前端监听 PHASE_CHANGE 消息，被动切换到新阶段
 *
 * @example
 * const {
 *   currentPhase,
 *   phaseState,
 *   goToNext,
 *   goToPrevious,
 *   goToPhase,
 *   updatePhaseData,
 *   getPhaseData,
 *   canGoNext,
 *   canGoBack,
 *   isBackendReady,
 *   waitingMessage,
 *   confirmCurrentPhase,
 *   handlePhaseChange,
 * } = usePhaseManager({
 *   sequence: DEFAULT_PHASE_SEQUENCE,
 *   onPhaseChange: (phase, prevPhase) => console.log(`切换到 ${phase}`),
 * })
 */

/**
 * 阶段管理器 Hook
 *
 * @param {Object} options - 配置选项
 * @param {string[]} [options.sequence] - 自定义阶段序列
 * @param {string} [options.initialPhase] - 初始阶段
 * @param {Function} [options.onPhaseChange] - 阶段变化回调
 * @param {Function} [options.onComplete] - 所有阶段完成回调
 * @returns {Object} 阶段管理器API
 */
export function usePhaseManager(options = {}) {
    const {
        sequence = DEFAULT_PHASE_SEQUENCE,
        initialPhase = sequence[0],
        onPhaseChange,
        onComplete,
    } = options

    // 阶段历史记录（用于返回功能）
    const [history, setHistory] = useState([initialPhase])

    // 各阶段数据缓存
    const [phaseDataMap, setPhaseDataMap] = useState({})

    // 后端同步状态
    const [isBackendReady, setIsBackendReady] = useState(true)
    const [waitingMessage, setWaitingMessage] = useState(null)
    const [isCheckingBackend, setIsCheckingBackend] = useState(false)
    
    // 等待后端确认状态
    const [isWaitingForBackend, setIsWaitingForBackend] = useState(false)

    // 当前阶段索引
    const currentPhase = history[history.length - 1]
    const currentIndex = sequence.indexOf(currentPhase)

    // 当前阶段配置
    const currentConfig = useMemo(() => {
        return PHASE_CONFIG[currentPhase] || null
    }, [currentPhase])

    // 是否可以进入下一阶段
    const canGoNext = useMemo(() => {
        return currentIndex < sequence.length - 1
    }, [currentIndex, sequence.length])

    // 是否可以返回上一阶段
    const canGoBack = useMemo(() => {
        const config = PHASE_CONFIG[currentPhase]
        if (!config?.allowBack) return false
        return history.length > 1
    }, [currentPhase, history.length])

    // 是否可以跳过当前阶段
    const canSkip = useMemo(() => {
        const config = PHASE_CONFIG[currentPhase]
        return config?.allowSkip ?? false
    }, [currentPhase])

    /**
     * 进入下一阶段
     * @description 后端主导模式下，此方法仅用于本地状态管理
     * 实际阶段切换由 handlePhaseChange 方法通过 WebSocket 消息触发
     */
    const goToNext = useCallback(() => {
        if (!canGoNext) {
            onComplete?.()
            return
        }

        const nextPhase = sequence[currentIndex + 1]
        const prevPhase = currentPhase

        setHistory((prev) => [...prev, nextPhase])
        onPhaseChange?.(nextPhase, prevPhase)
    }, [canGoNext, currentIndex, currentPhase, sequence, onPhaseChange, onComplete])

    /**
     * 返回上一阶段
     */
    const goToPrevious = useCallback(() => {
        if (!canGoBack) return

        const prevPhase = history[history.length - 2]
        const current = currentPhase

        setHistory((prev) => prev.slice(0, -1))
        onPhaseChange?.(prevPhase, current)
    }, [canGoBack, history, currentPhase, onPhaseChange])

    /**
     * 跳转到指定阶段
     *
     * @param {string} phaseId - 目标阶段ID
     * @param {boolean} [recordHistory=true] - 是否记录到历史
     */
    const goToPhase = useCallback(
        (phaseId, recordHistory = true) => {
            if (!sequence.includes(phaseId)) {
                console.warn(`[usePhaseManager] 阶段 ${phaseId} 不在序列中`)
                return
            }

            const prevPhase = currentPhase

            // 如果目标阶段与当前阶段相同，不执行任何操作
            if (phaseId === prevPhase) {
                console.log(`[usePhaseManager] 目标阶段与当前阶段相同，跳过: ${phaseId}`)
                return
            }

            if (recordHistory) {
                setHistory((prev) => [...prev, phaseId])
            } else {
                setHistory((prev) => [...prev.slice(0, -1), phaseId])
            }

            onPhaseChange?.(phaseId, prevPhase)
        },
        [sequence, currentPhase, onPhaseChange]
    )

    /**
     * 更新阶段数据
     *
     * @param {string} phaseId - 阶段ID
     * @param {Object} data - 要更新的数据
     * @param {boolean} [merge=true] - 是否合并数据
     */
    const updatePhaseData = useCallback((phaseId, data, merge = true) => {
        setPhaseDataMap((prev) => ({
            ...prev,
            [phaseId]: merge
                ? {...prev[phaseId], ...data}
                : data,
        }))
    }, [])

    /**
     * 获取阶段数据
     *
     * @param {string} phaseId - 阶段ID
     * @returns {Object|undefined} 阶段数据
     */
    const getPhaseData = useCallback(
        (phaseId) => {
            return phaseDataMap[phaseId]
        },
        [phaseDataMap]
    )

    /**
     * 清除阶段数据
     *
     * @param {string} [phaseId] - 阶段ID，不传则清除所有
     */
    const clearPhaseData = useCallback((phaseId) => {
        if (phaseId) {
            setPhaseDataMap((prev) => {
                const {[phaseId]: _, ...rest} = prev
                return rest
            })
        } else {
            setPhaseDataMap({})
        }
    }, [])

    /**
     * 确认当前阶段完成
     * @description 调用后端 API 确认阶段完成，后端会在所有玩家确认后推进阶段
     * @param {string|number} gameId - 游戏ID
     * @param {string|number} playerId - 玩家ID
     * @returns {Promise<Object>} 确认结果
     */
    const confirmCurrentPhase = useCallback(async (gameId, playerId) => {
        try {
            setIsWaitingForBackend(true)
            console.log('[usePhaseManager] 确认阶段完成:', currentPhase, '玩家ID:', playerId)
            
            const response = await confirmPhase(gameId, {
                playerId,
                phase: currentPhase
            })
            
            console.log('[usePhaseManager] 阶段确认响应:', response)
            
            // 检查是否已自动推进阶段
            if (response?.phaseAdvanced && response?.nextPhase) {
                console.log('[usePhaseManager] 后端已自动推进阶段:', response.nextPhase)
                // 后端会通过 WebSocket 广播 PHASE_CHANGE，前端会收到并切换
            }
            
            return response
        } catch (error) {
            console.error('[usePhaseManager] 阶段确认失败:', error)
            throw error
        } finally {
            setIsWaitingForBackend(false)
        }
    }, [currentPhase])

    /**
     * 处理后端发来的阶段变化消息
     * @description 监听 WebSocket PHASE_CHANGE 消息，被动切换到新阶段
     * @param {Object} data - WebSocket 消息数据
     * @param {string} data.type - 消息类型
     * @param {Object} data.payload - 消息负载
     * @param {string} data.payload.newPhase - 新阶段（前端格式）
     * @param {string} data.payload.previousPhase - 上一阶段（前端格式）
     * @param {string} data.payload.message - 提示消息
     */
    const handlePhaseChange = useCallback((data) => {
        console.log('[usePhaseManager] 收到阶段变化消息:', data)
        
        // 处理消息结构：可能是 {payload: {...}} 或直接 {...}
        const payload = data?.payload || data
        const {newPhase, previousPhase, message} = payload || {}
        
        if (!newPhase) {
            console.warn('[usePhaseManager] 阶段变化消息缺少 newPhase 字段, payload:', payload)
            return
        }
        
        // 验证新阶段是否在序列中
        if (!sequence.includes(newPhase)) {
            console.warn('[usePhaseManager] 新阶段不在序列中:', newPhase, '序列:', sequence)
            return
        }
        
        // 清除等待状态
        setIsWaitingForBackend(false)
        setWaitingMessage(null)
        
        // 切换到新阶段
        const prevPhase = currentPhase
        if (newPhase !== currentPhase) {
            setHistory((prev) => [...prev, newPhase])
            onPhaseChange?.(newPhase, prevPhase)
            console.log('[usePhaseManager] 阶段已切换:', prevPhase, '->', newPhase)
        } else {
            console.log('[usePhaseManager] 当前已是目标阶段，无需切换:', currentPhase)
        }
    }, [sequence, currentPhase, onPhaseChange])

    /**
     * 重置到初始阶段
     */
    const reset = useCallback(() => {
        setHistory([initialPhase])
        setPhaseDataMap({})
        setIsWaitingForBackend(false)
        setWaitingMessage(null)
    }, [initialPhase])

    /**
     * 获取阶段进度（0-100）
     */
    const progress = useMemo(() => {
        if (sequence.length <= 1) return 100
        return Math.round((currentIndex / (sequence.length - 1)) * 100)
    }, [currentIndex, sequence.length])

    return {
        // 当前状态
        currentPhase,
        currentIndex,
        currentConfig,
        progress,

        // 阶段序列信息
        sequence,
        totalPhases: sequence.length,

        // 导航能力
        canGoNext,
        canGoBack,
        canSkip,

        // 导航方法
        goToNext,
        goToPrevious,
        goToPhase,

        // 数据管理
        phaseDataMap,
        updatePhaseData,
        getPhaseData,
        clearPhaseData,

        // 后端同步状态
        isBackendReady,
        waitingMessage,
        isCheckingBackend,
        isWaitingForBackend,

        // 后端同步方法
        confirmCurrentPhase,
        handlePhaseChange,
        setWaitingMessage,
        setIsBackendReady,

        // 工具方法
        reset,

        // 历史记录（调试用）
        history,
    }
}

export default usePhaseManager

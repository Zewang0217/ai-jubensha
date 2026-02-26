import {useCallback, useMemo, useState} from 'react'
import {DEFAULT_PHASE_SEQUENCE, PHASE_CONFIG} from '../types'

/**
 * @fileoverview 阶段管理器 Hook
 * @description 管理游戏阶段的切换、数据缓存和历史记录，支持自定义阶段序列
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
     * 重置到初始阶段
     */
    const reset = useCallback(() => {
        setHistory([initialPhase])
        setPhaseDataMap({})
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

        // 工具方法
        reset,

        // 历史记录（调试用）
        history,
    }
}

export default usePhaseManager

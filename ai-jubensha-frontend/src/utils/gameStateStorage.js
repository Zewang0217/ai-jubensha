/**
 * @fileoverview 游戏状态持久化管理
 * @description 使用 localStorage 存储和恢复游戏状态，确保页面刷新后能继续游戏
 */

const GAME_STATE_KEY = 'gameState'

/**
 * 游戏状态数据结构
 * @typedef {Object} GameState
 * @property {string} gameId - 游戏ID
 * @property {string} currentPhase - 当前阶段
 * @property {number} realPlayerCount - 真人玩家数量
 * @property {number} totalPlayerCount - 总玩家数量
 * @property {string} scriptId - 剧本ID
 * @property {number} lastUpdateTime - 最后更新时间
 */

/**
 * 保存游戏状态到 localStorage
 * @param {Object} state - 游戏状态对象
 * @param {string} state.gameId - 游戏ID
 * @param {string} state.currentPhase - 当前阶段
 * @param {number} [state.realPlayerCount] - 真人玩家数量
 * @param {number} [state.totalPlayerCount] - 总玩家数量
 * @param {string} [state.scriptId] - 剧本ID
 * @returns {boolean} 是否保存成功
 */
export const saveGameState = (state) => {
    if (!state?.gameId) {
        console.warn('[gameStateStorage] Cannot save game state without gameId')
        return false
    }

    try {
        const gameState = {
            gameId: state.gameId,
            currentPhase: state.currentPhase,
            realPlayerCount: state.realPlayerCount ?? 1,
            totalPlayerCount: state.totalPlayerCount,
            scriptId: state.scriptId,
            lastUpdateTime: Date.now(),
        }

        localStorage.setItem(GAME_STATE_KEY, JSON.stringify(gameState))
        console.log('[gameStateStorage] Game state saved:', gameState)
        return true
    } catch (error) {
        console.error('[gameStateStorage] Failed to save game state:', error)
        return false
    }
}

/**
 * 从 localStorage 加载游戏状态
 * @returns {GameState|null} 游戏状态对象，如果不存在则返回 null
 */
export const loadGameState = () => {
    try {
        const stored = localStorage.getItem(GAME_STATE_KEY)
        if (!stored) {
            console.log('[gameStateStorage] No game state found in storage')
            return null
        }

        const gameState = JSON.parse(stored)
        console.log('[gameStateStorage] Game state loaded:', gameState)

        const now = Date.now()
        const lastUpdate = gameState.lastUpdateTime || 0
        const expirationTime = 24 * 60 * 60 * 1000

        if (now - lastUpdate > expirationTime) {
            console.log('[gameStateStorage] Game state expired, clearing...')
            clearGameState()
            return null
        }

        return gameState
    } catch (error) {
        console.error('[gameStateStorage] Failed to load game state:', error)
        clearGameState()
        return null
    }
}

/**
 * 清除游戏状态
 * @returns {void}
 */
export const clearGameState = () => {
    try {
        localStorage.removeItem(GAME_STATE_KEY)
        console.log('[gameStateStorage] Game state cleared')
    } catch (error) {
        console.error('[gameStateStorage] Failed to clear game state:', error)
    }
}

/**
 * 更新游戏阶段
 * @param {string} phase - 新阶段名称
 * @returns {boolean} 是否更新成功
 */
export const updateGamePhase = (phase) => {
    try {
        const state = loadGameState()
        if (!state) {
            console.warn('[gameStateStorage] Cannot update phase without existing game state')
            return false
        }

        state.currentPhase = phase
        state.lastUpdateTime = Date.now()
        localStorage.setItem(GAME_STATE_KEY, JSON.stringify(state))
        console.log('[gameStateStorage] Phase updated to:', phase)
        return true
    } catch (error) {
        console.error('[gameStateStorage] Failed to update phase:', error)
        return false
    }
}

/**
 * 检查是否存在保存的游戏状态
 * @returns {boolean} 是否存在游戏状态
 */
export const hasGameState = () => {
    return loadGameState() !== null
}

export default {
    saveGameState,
    loadGameState,
    clearGameState,
    updateGamePhase,
    hasGameState,
}

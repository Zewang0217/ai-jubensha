import apiClient from './client'

/**
 * 游戏玩家关系 API 模块
 * 提供游戏与玩家关联关系的 HTTP 请求接口
 */

// Mock 数据开关 - 开发环境使用
const USE_MOCK = false

// Mock 游戏玩家数据
const mockGamePlayers = [
    {
        id: 1,
        gameId: 1,
        playerId: 1,
        characterId: 1,
        isDm: true,
        status: 'PLAYING',
        joinTime: '2026-03-01T10:00:00Z',
    },
    {
        id: 2,
        gameId: 1,
        playerId: 2,
        characterId: 2,
        isDm: false,
        status: 'PLAYING',
        joinTime: '2026-03-01T10:05:00Z',
    },
    {
        id: 3,
        gameId: 1,
        playerId: 3,
        characterId: 3,
        isDm: false,
        status: 'READY',
        joinTime: '2026-03-01T10:10:00Z',
    },
]

// =============================================================================
// JSDoc 类型定义
// =============================================================================

/**
 * @typedef {Object} GamePlayerResponseDTO
 * @property {number} id - 关系ID
 * @property {number} gameId - 游戏ID
 * @property {number} playerId - 玩家ID
 * @property {number} characterId - 角色ID
 * @property {boolean} isDm - 是否为DM
 * @property {'READY'|'PLAYING'|'LEFT'} status - 状态
 * @property {string} joinTime - 加入时间
 */

// =============================================================================
// API 方法
// =============================================================================

/**
 * 根据ID查询游戏玩家关系
 * @param {number} id - 关系ID
 * @returns {Promise<GamePlayerResponseDTO>} 游戏玩家关系详情
 */
export const getGamePlayerById = async (id) => {
    console.log('[gamePlayerApi] 根据ID查询:', id)
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const gamePlayer = mockGamePlayers.find((gp) => gp.id === Number(id))
                if (!gamePlayer) {
                    reject(new Error('游戏玩家关系不存在'))
                } else {
                    console.log('[gamePlayerApi] Mock 返回:', gamePlayer)
                    resolve(gamePlayer)
                }
            }, 300)
        })
    }
    const response = await apiClient.get(`/game-players/${id}`)
    return response?.data || response
}

/**
 * 根据游戏ID和玩家ID查询游戏玩家关系
 * @param {number} gameId - 游戏ID
 * @param {number} playerId - 玩家ID
 * @returns {Promise<GamePlayerResponseDTO>} 游戏玩家关系
 */
export const getGamePlayerByGameAndPlayer = async (gameId, playerId) => {
    console.log('[gamePlayerApi] 根据游戏ID和玩家ID查询:', {gameId, playerId})
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const gamePlayer = mockGamePlayers.find(
                    (gp) => gp.gameId === Number(gameId) && gp.playerId === Number(playerId)
                )
                if (!gamePlayer) {
                    reject(new Error('游戏玩家关系不存在'))
                } else {
                    console.log('[gamePlayerApi] Mock 返回:', gamePlayer)
                    resolve(gamePlayer)
                }
            }, 300)
        })
    }
    const response = await apiClient.get(`/game-players/game/${gameId}/player/${playerId}`)
    return response?.data || response
}

/**
 * 根据游戏ID查询游戏玩家关系列表
 * @param {number} gameId - 游戏ID
 * @returns {Promise<GamePlayerResponseDTO[]>} 游戏玩家关系列表
 */
export const getGamePlayersByGameId = async (gameId) => {
    console.log('[gamePlayerApi] 根据游戏ID查询:', gameId)
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const list = mockGamePlayers.filter((gp) => gp.gameId === Number(gameId))
                console.log('[gamePlayerApi] Mock 返回列表:', list)
                resolve(list)
            }, 300)
        })
    }
    const response = await apiClient.get(`/game-players/game/${gameId}`)
    return response?.data || response
}

export default {
    getGamePlayerById,
    getGamePlayerByGameAndPlayer,
    getGamePlayersByGameId,
}
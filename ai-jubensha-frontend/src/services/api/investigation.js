import apiClient from './client'

/**
 * 搜证 API 模块
 * 提供游戏中搜证相关的所有 HTTP 请求接口
 */

// Mock 数据开关 - 开发环境使用
const USE_MOCK = false

// Mock 搜证数据
const mockInvestigationData = {
    // 玩家的搜证状态
    playerStatus: new Map(),
    // 搜证历史
    history: new Map(),
}

// 初始化 Mock 数据
const initMockPlayerStatus = (playerId) => {
    if (!mockInvestigationData.playerStatus.has(playerId)) {
        mockInvestigationData.playerStatus.set(playerId, {
            playerId,
            remainingCount: 3,
            totalCount: 3,
            investigationHistory: [],
        })
    }
    return mockInvestigationData.playerStatus.get(playerId)
}

// Mock 线索数据
const mockClues = [
    {
        id: 1,
        name: '破碎的花瓶',
        description: '一个破碎的花瓶，碎片散落在地板上，似乎是被用力摔碎的。',
        type: 'PHYSICAL',
        sceneId: 1,
    },
    {
        id: 2,
        name: '带血的匕首',
        description: '一把匕首，刀刃上有已经干涸的血迹。',
        type: 'PHYSICAL',
        sceneId: 1,
    },
    {
        id: 3,
        name: '神秘的信件',
        description: '一封没有署名的信件，内容是威胁性的文字。',
        type: 'DOCUMENT',
        sceneId: 2,
    },
    {
        id: 4,
        name: '管家证词',
        description: '管家声称在案发当晚看到了可疑的人影。',
        type: 'TESTIMONY',
        sceneId: 2,
    },
]

// =============================================================================
// JSDoc 类型定义
// =============================================================================

/**
 * @typedef {Object} InvestigationRequestDTO
 * @property {number} playerId - 玩家ID
 * @property {number} sceneId - 场景ID
 * @property {number} [clueId] - 可选：指定线索ID（如果不指定则随机返回场景中的一个线索）
 * @property {string} [remark] - 可选：搜证备注或玩家留言
 */

/**
 * @typedef {Object} InvestigationResultDTO
 * @property {boolean} success - 是否成功
 * @property {string} message - 提示消息
 * @property {Object} [clue] - 获得的线索
 * @property {number} remainingCount - 剩余搜证次数
 * @property {string} investigationTime - 搜证时间
 */

/**
 * @typedef {Object} InvestigationStatusDTO
 * @property {number} playerId - 玩家ID
 * @property {number} remainingCount - 剩余搜证次数
 * @property {number} totalCount - 总搜证次数
 * @property {Array} investigationHistory - 搜证历史
 */

/**
 * @typedef {Object} CanInvestigateDTO
 * @property {boolean} canInvestigate - 是否可以搜证
 * @property {string} [reason] - 如果不能搜证，说明原因
 * @property {number} [remainingCount] - 剩余搜证次数
 */

// =============================================================================
// API 方法
// =============================================================================

/**
 * 执行搜证操作
 * @param {number} gameId - 游戏ID
 * @param {InvestigationRequestDTO} data - 搜证请求数据
 * @returns {Promise<InvestigationResultDTO>} 搜证结果
 */
export const investigate = async (gameId, data) => {
    console.log('[investigationApi] 执行搜证:', {gameId, data})
    if (USE_MOCK) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const {playerId, sceneId, clueId} = data
                const playerStatus = initMockPlayerStatus(playerId)

                // 检查是否还有剩余次数
                if (playerStatus.remainingCount <= 0) {
                    reject(new Error('搜证次数已用完'))
                    return
                }

                // 获取该场景的线索
                const sceneClues = mockClues.filter((c) => c.sceneId === Number(sceneId))
                if (sceneClues.length === 0) {
                    reject(new Error('该场景没有线索'))
                    return
                }

                // 如果指定了线索ID，查找对应线索；否则随机返回一个
                let foundClue
                if (clueId) {
                    foundClue = sceneClues.find((c) => c.id === Number(clueId))
                }
                if (!foundClue) {
                    const randomIndex = Math.floor(Math.random() * sceneClues.length)
                    foundClue = sceneClues[randomIndex]
                }

                // 减少剩余次数
                playerStatus.remainingCount--

                // 记录搜证历史
                const investigationRecord = {
                    clueId: foundClue.id,
                    clueName: foundClue.name,
                    sceneId: Number(sceneId),
                    investigationTime: new Date().toISOString(),
                }
                playerStatus.investigationHistory.push(investigationRecord)

                const result = {
                    success: true,
                    message: '搜证成功',
                    clue: foundClue,
                    remainingCount: playerStatus.remainingCount,
                    investigationTime: investigationRecord.investigationTime,
                }
                console.log('[investigationApi] Mock 搜证成功:', result)
                resolve(result)
            }, 500)
        })
    }
    const response = await apiClient.post(`/games/${gameId}/investigation`, data)
    return response?.data || response
}

/**
 * 获取玩家的搜证状态
 * @param {number} gameId - 游戏ID
 * @param {number} playerId - 玩家ID
 * @returns {Promise<InvestigationStatusDTO>} 搜证状态
 */
export const getInvestigationStatus = async (gameId, playerId) => {
    console.log('[investigationApi] 获取搜证状态:', {gameId, playerId})
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const status = initMockPlayerStatus(playerId)
                console.log('[investigationApi] Mock 返回状态:', status)
                resolve({...status})
            }, 300)
        })
    }
    const response = await apiClient.get(`/games/${gameId}/investigation/status`, {
        params: {playerId},
    })
    return response?.data || response
}

/**
 * 检查玩家是否可以搜证
 * @param {number} gameId - 游戏ID
 * @param {number} playerId - 玩家ID
 * @returns {Promise<CanInvestigateDTO>} 是否可以搜证
 */
export const canInvestigate = async (gameId, playerId) => {
    console.log('[investigationApi] 检查是否可以搜证:', {gameId, playerId})
    if (USE_MOCK) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const status = initMockPlayerStatus(playerId)
                const canInvestigate = status.remainingCount > 0
                const result = {
                    canInvestigate,
                    remainingCount: status.remainingCount,
                    reason: canInvestigate ? undefined : '搜证次数已用完',
                }
                console.log('[investigationApi] Mock 返回检查结果:', result)
                resolve(result)
            }, 300)
        })
    }
    const response = await apiClient.get(`/games/${gameId}/investigation/can-investigate`, {
        params: {playerId},
    })
    return response?.data || response
}

export default {
    investigate,
    getInvestigationStatus,
    canInvestigate,
}
